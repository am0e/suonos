/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package suonos.services.player;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.utils.Validate;
import com.github.am0e.jdi.events.EventBus;

import suonos.app.SuonosLib;
import suonos.models.playlist.PlayListMode;
import suonos.models.playlist.Playlist;
import suonos.models.playlist.ShuffleMode;

@Singleton
public class MediaPlayer {

    public static final Logger log = LoggerFactory.getLogger(MediaPlayer.class);

    /**
     * An instance of a player. Eg an instance of an mplayer process.
     */
    PlayerInstance player;

    /**
     * Command execution thread. All requests to the media player are handled by
     * a dedicated thread.
     */
    Thread cmdThread;

    /**
     * Command queue.
     */
    BlockingQueue<Cmd> cmdQueue;

    /**
     * The current playlist ordered as items are added.
     */
    private List<PlayerItem> playlist;

    /**
     * The play queue. If the play mode is shuffle, this list will contain the
     * {@link #playlist} in a randomized order.
     */
    private LinkedList<PlayerItem> playQueue;

    /**
     * As items are played, they are removed from the {@link #playQueue} and
     * added to the {@link #playedQueue}. This occurs when an item is removed
     * from the {@link #playQueue}. It is added to the media player for playing
     * and then added to the {@link #playedQueue}
     */
    private LinkedList<PlayerItem> playedQueue;

    /**
     * current item playing.
     */
    PlayerItem itemPlaying;

    /**
     * Event bus.
     */
    EventBus events;

    /**
     * Player builder.
     */
    PlayerBuilder playerBuilder;

    /**
     * Playlist mode, repeat or play to end.
     */
    int playlistMode = PlayListMode.TO_END;

    /**
     * Shuffle mode.
     */
    int shuffleMode = ShuffleMode.NO_SUFFLE;

    int playMode = PlayerMode.STOPPED;

    /**
     * Player position as a percentage.
     */
    int playerPos;

    boolean playerLock = false;

    // Object playLock = new Object();

    protected PlaybackStartedCmd playStartedCmd;

    /**
     * Callback context for the PlayerInstance - eg mplayer. The methods are
     * called from a different thread thefore we will queue commands in the
     * MediaPlayer queue to handle the events.
     * 
     * @author anthony
     */
    private final PlayerCallback playerCallback = new PlayerCallback() {

        @Override
        public void playerFinished(String statusCode) {
            queueCmd(new PlaybackFinishedCmd(statusCode));
        }

        @Override
        public void playerPosUpdate(int timeInSecs) {
            queueCmd(new PlaybackPosUpdatedCmd(timeInSecs));
        }

        @Override
        public void playbackError(String msg) {
            queueCmd(new PlaybackErrorCmd(msg));
        }

        @Override
        public void playbackStarted(PlaybackInfo item) {
            playStartedCmd = new PlaybackStartedCmd(item);
        }
    };

    /**
     * The command execution thread run loop.
     */
    private void playerThread() {
        boolean abort = false;

        while (abort == false) {
            try (Closeable it = SuonosLib.enterCtx()) {
                if (playerLock) {
                    pollPlayStart();
                } else {
                    pollQueue();
                }

            } catch (InterruptedException e) {
                abort = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pollPlayStart() throws Exception {
        // Wait for a second.
        //
        Thread.sleep(1000);

        if (playStartedCmd != null) {
            playStartedCmd.execute(this);
            playStartedCmd = null;
        }
    }

    private void pollQueue() throws Exception {
        // Wait for 1 second to get a command.
        //
        Cmd cmd = cmdQueue.poll(1, TimeUnit.SECONDS);

        if (cmd != null) {
            while (cmd != null) {
                // Execute the command.
                //
                // log.debug("cmd {}", cmd);
                //
                synchronized (this) {
                    if (playMode != PlayerMode.QUIT || (cmd.getClass() == PlayerQuitCmd.class)) {
                        cmd.execute(this);
                    }
                }

                if (playerLock) {
                    cmd = null;
                } else {
                    cmd = cmdQueue.poll();
                }
            }

        } else {
            // No command, poll the playback, this will update the player
            // position,
            // we do this every second.
            //
            if (player != null && itemPlaying != null) {
                player.pollPlayback();
            }
        }
    }

    public MediaPlayer(EventBus events, PlayerBuilder playerBuilder) {
        // init the player thread.
        //
        this.events = events;
        this.cmdQueue = new ArrayBlockingQueue<>(1000);
        this.playerBuilder = playerBuilder;
        this.initPlaylist();

        Runnable target = new Runnable() {
            @Override
            public void run() {
                playerThread();
            }
        };

        cmdThread = new Thread(null, target, "Player", 32768);
        cmdThread.start();
    }

    void queueCmd(Cmd cmd) {
        if (Thread.currentThread() == this.cmdThread) {
            Validate.notAllowed("Not allowed inside Player thread.");
        }

        synchronized (this) {
            // Cannot queue here!
            //
            cmd.queued(this);
        }

        try {
            cmdQueue.put(cmd);

        } catch (InterruptedException e) {
            // Usually as a result of the process terminating!
            // ignore the command.
        }
    }

    void checkAccess() {
        if (Thread.currentThread() != this.cmdThread) {
            Validate.notAllowed("Not allowed outside Player thread.");
        }
    }

    void nextTrack() throws Exception {
        checkAccess();

        // Is the queue empty?
        //
        if (playQueue.isEmpty()) {
            // If repeat is on, restart playback. In shuffle mode, the playlist
            // is reshuffled.
            //
            if (playlistMode == PlayListMode.REPEAT) {
                playQueue.addAll(playlist);

                if (shuffleMode == ShuffleMode.SHUFFLE) {
                    shufflePlayQueue();
                }
            }
        }

        if (playQueue.isEmpty()) {
            return;
        }

        // Remove head of queue.
        //
        PlayerItem next = playQueue.remove();

        // Add to played list.
        //
        playedQueue.add(next);

        if (player != null) {
            if (playMode == PlayerMode.PLAYING) {
                PlayTrackCmd cmd = new PlayTrackCmd(next);
                cmd.execute(this);
            }
        }
    }

    private void createPlayer() throws IOException {
        if (player != null)
            return;

        try {
            // Create the instance of the player. This will not initiate
            // playback!
            //
            player = playerBuilder.createInst(playerCallback);

            // Now start playback.
            // This will start the mplayer process and create the mplayer
            // monitor thread.
            // It will return immediately after starting the process.
            //
            player.start();

        } catch (Exception ex) {
            player = null;
            queueCmd(new PlaybackErrorCmd(ex.getMessage()));
        }
    }

    void stopPlayer() throws IOException {
        checkAccess();

        if (playMode == PlayerMode.QUIT)
            return;

        if (player != null) {
            // Stop the player.
            //
            player.stop();

            // Set playmode to stopped.
            //
            playMode = PlayerMode.STOPPED;
        }
    }

    void startPlayer() throws Exception {
        checkAccess();

        if (playMode == PlayerMode.QUIT)
            return;

        if (player == null) {
            createPlayer();
        }

        if (player != null) {
            // Set playmode to start.
            //
            playMode = PlayerMode.PLAYING;
        }
    }

    void removeItemFromPlaylist(PlayerItem item) {
        checkAccess();

        playlist.remove(item);
        playedQueue.remove(item);
    }

    PlayerItem getCurrentItem() {
        checkAccess();
        return itemPlaying;
    }

    /**
     * Clear the playlist. Note if an item is playing
     * ({@link #itemPlaying}!=null) it will play to completion.
     */
    void clearPlaylist() {
        checkAccess();
        initPlaylist();
    }

    private void initPlaylist() {
        playQueue = new LinkedList<>();
        playedQueue = new LinkedList<>();

        // The playlist is synchronized to allow external threads to get at the
        // playlist.
        //
        playlist = Collections.synchronizedList(AntLib.newList());
    }

    void shufflePlayQueue() {
        if (this.shuffleMode == ShuffleMode.SHUFFLE) {
            // Shuffle.
            //
            log.debug("shufflePlayQueue");
            Collections.shuffle(playQueue);

        } else {
            log.debug("unshufflePlayQueue");

            // Unshuffle.
            // Size of current play queue. Ie count of items remaining to play.
            //
            int len = playQueue.size();

            if (len != 0) {
                // Build a set containing the remaining items to play.
                //
                Set<PlayerItem> set = new HashSet<PlayerItem>(playQueue);

                // Clear current queue.
                //
                playQueue.clear();

                // Now reconstruct the play queue containing the remaining items
                // in the playlist sequence.
                //
                for (PlayerItem it : playlist) {
                    if (set.contains(it)) {
                        playQueue.add(it);
                    }
                }
            }
        }
    }

    public void addItemsToPlaylist(List<PlayerItem> list) {
        checkAccess();

        // Add the items to the playlist.
        //
        playlist.addAll(list);

        // Add the items to the play queue as well.
        //
        playQueue.addAll(list);

        // If in shuffle mode, reshuffle the play queue.
        //
        if (shuffleMode == ShuffleMode.SHUFFLE) {
            shufflePlayQueue();
        }
    }

    void setShuffleMode(int shuffleMode) {
        checkAccess();

        if (this.shuffleMode != shuffleMode) {
            this.shuffleMode = shuffleMode;
            shufflePlayQueue();
        }
    }

    void setPlayListMode(int mode) {
        checkAccess();
        this.playlistMode = mode;
    }

    public PlayerPlaylist getPlayList() {

        PlayerItem current = itemPlaying;
        String cur = null;

        Playlist pl = new Playlist();
        pl.setTitle("Media Player");

        for (PlayerItem it : playlist) {
            pl.addItem(it.relId);
            if (it == current) {
                cur = it.relId;
            }
        }

        return new PlayerPlaylist(pl, cur);
    }

    public boolean isPlaying() {
        return this.itemPlaying != null;
    }

    public void skipTrack(int off) throws Exception {

        checkAccess();

        boolean isPlaying = (itemPlaying != null);

        // make adjustments for file pos <25% >75%, etc.

        if (off > 0) {
            // If playing a file:
            // ------------------------------------------------------------
            // off = 1
            // Playing [C]: playedQueue = [A,B,C], playQueue=[D,E]
            // Play [D]
            //
            // off = 2, skip a track & play E
            // Playing [C]: playedQueue = [A,B,C], playQueue=[D,E]
            // Put [D] onto queue: playedQueue = [A,B,C,D], playQueue=[E]
            // Play [E]
            //
            // If player is stopped, ie not not playing a file:
            // ------------------------------------------------------------
            // off = 1
            // playedQueue = [A,B,C], playQueue=[D,E]
            // Put [D] onto queue: playedQueue = [A,B,C,D], playQueue=[E]
            //

            if (isPlaying) {
                off--;
            }

            // Forward
            // +N skip (N-1) tracks and play next track.
            //
            while (off-- > 0 && !playQueue.isEmpty()) {
                playedQueue.add(playQueue.remove());
            }

        } else {
            if (isPlaying) {
                // If currently playing a file, then play the previous file.
                // off = -1 play position=10%
                // Playing [C]: playedQueue = [A,B,C], playQueue=[D]
                // Put [B,C] onto queue: playedQueue = [A], playQueue=[B,C,D]
                // Play [B]
                //
                // off = -1 play position=90%
                // Playing [C]: playedQueue = [A,B,C], playQueue=[D]
                // Put [C] onto queue: playedQueue = [A,B], playQueue=[C,D]
                // Play [C]
                //
                if (playerPos < 50) {
                    off--;
                }
            }

            while (off++ < 0 && !playedQueue.isEmpty()) {
                playQueue.addFirst(playedQueue.removeLast());
            }
        }

        if (playMode == PlayerMode.PLAYING) {
            nextTrack();
        }

        // not sure about this:
        // We have to wait for stop playing to finish to avoid overlap of sound!
    }

}
