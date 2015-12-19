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

import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.lang3.RandomUtils;

import com.github.am0e.commons.AntLib;

import suonos.models.Playable;
import suonos.models.PlayableContainer;
import suonos.models.PlayableFile;
import suonos.models.media.PlayableStoreObject;
import suonos.models.movies.Movie;
import suonos.models.music.MusicAlbum;
import suonos.models.music.MusicTrack;
import suonos.models.playlist.Playlist;
import suonos.models.playlist.ShuffleMode;
import suonos.services.AbstractSvcs;

@Singleton
public final class MediaPlayerSvcs extends AbstractSvcs {

    private final MediaPlayer inst = lib.instanceOf(MediaPlayer.class);

    /**
     * Stop the player and clear the playlist.
     */
    public void stop() {
        queueCmd(new PlayerStopCmd());
    }

    /**
     * Start the player.
     */
    public void start() {
        queueCmd(new StartPlayerCmd());
    }

    /**
     * Clear the playlist.
     */
    public void clearPlaylist() {
        queueCmd(new ClearPlaylistCmd());
    }

    /**
     * Pause or resume.
     * 
     * @param pause
     */
    public void pause(boolean pause) {
        queueCmd(new PlayerPauseCmd(pause));
    }

    public void seek(int seekPos, boolean abs) {
        queueCmd(new PlayerSeekCmd(seekPos, abs));
    }

    public void volume(int percent) {
        queueCmd(new PlayerVolumeCmd(percent));
    }

    public void mute(boolean mute) {
        queueCmd(new PlayerMuteCmd(mute));
    }

    public void osdMenuAction(String action) {
        queueCmd(new OsdMenuActionCmd(action));
    }

    private void queueCmd(Cmd cmd) {
        try {
            Thread.sleep(RandomUtils.nextLong(200, 500));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        inst.queueCmd(cmd);
    }

    public void nextTrack() {
        queueCmd(new PlayerSkipTrackCmd(1));
    }

    public void previousTrack() {
        queueCmd(new PlayerSkipTrackCmd(-1));
    }

    public PlayerPlaylist getPlayList() {
        return inst.getPlayList();
    }

    public void playItem(Playable item) throws IOException {
        List<PlayerItem> list = addPlayableToList(item, AntLib.newList());

        // A track / movie.
        //
        inst.queueCmd(new AddPlayableCmd(list, true));
    }

    /**
     * Add a playable object.
     * 
     * @param item
     *            An instance of: {@link MusicAlbum}, {@link MusicTrack},
     *            {@link Playlist}, or {@link Movie}.
     * @throws IOException
     */
    public void queueItem(Playable item) throws IOException {

        List<PlayerItem> list = addPlayableToList(item, AntLib.newList());

        // A track / movie.
        //
        inst.queueCmd(new AddPlayableCmd(list, false));
    }

    private List<PlayerItem> addPlayableToList(Playable item, List<PlayerItem> items) throws IOException {
        if (item instanceof PlayableContainer) {
            // Playlist - add playlist items.
            // Album - add album tracks.
            //
            PlayableContainer cntr = (PlayableContainer) item;

            for (Playable it : cntr.getPlayableItems()) {
                addPlayableToList(it, items);
            }

        } else {
            // A track / movie.
            //
            items.add(new PlayerItem(item.getTitle(), item.getPath(), item.getId()));
        }

        return items;
    }

    public Playable getPlayableForUrl(String title, String url) throws IOException {
        PlayableStoreObject item = new PlayableStoreObject();
        item.setTitle(title);
        item.setPath(url);
        return item;
    }

    public void setShuffleMode(boolean v) {
        queueCmd(new PlayerShuffleCmd(v == false ? ShuffleMode.NO_SUFFLE : ShuffleMode.SHUFFLE));
    }

    public void setPlayListMode(int mode) {
        queueCmd(new PlayListModeCmd(mode));
    }

    public void setAutoAdvanceMode(boolean mode) {
        queueCmd(new AutoAdvModeCmd(mode));
    }

    public void quit() {
        queueCmd(new PlayerQuitCmd());
    }

    public Playable getItem(String id, String title, String mediaFile) {
        return new PlayableFile(id, title, mediaFile);
    }
}
