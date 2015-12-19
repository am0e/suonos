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

package suonos.app;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.impl.DefaultAntLibProvider;
import com.github.am0e.commons.providers.Context;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import suonos.app.utils.Uids;
import suonos.lucene.LuceneIndex;
import suonos.lucene.QueryResults;
import suonos.lucene.Statement;
import suonos.models.Playable;
import suonos.models.media.PlayableStoreObject;
import suonos.models.music.MusicAlbum;
import suonos.models.music.MusicLib;
import suonos.models.playlist.PlayListMode;
import suonos.models.playlist.Playlist;
import suonos.services.music.tasks.RebuildMusicLibraryTask;
import suonos.services.player.MediaPlayerSvcs;
import suonos.services.player.PlaybackInfo;
import suonos.services.player.PlayerCallback;
import suonos.services.player.PlayerInstance;
import suonos.services.player.PlayerItem;
import suonos.services.player.mplayer.MPlayerBuilder;
import suonos.swt.SwtInterface;

public final class AppMain {

    public static SuonosContainer container;

    public static void main(String[] args) throws IOException {
        // new Test().run();
        // System.exit(0);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        new AppMain().main_(args);
    }

    private void main_(String[] args) throws IOException {
        // Init container.
        //
        AntLib.setAntLibProvider(new DefaultAntLibProvider());
        container = new SuonosContainer();

        // container.shareBean(new MockPlayer());

        // Wire in VERTX
        //
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(1000000000);
        Vertx vertx = Vertx.vertx(options);
        container.shareBean(vertx);

        // Wire in MPlayer.
        //
        container.shareBean(new MPlayerBuilder());

        // Setup default fields.
        //
        LuceneIndex index = container.getBean(LuceneIndex.class);

        // Setup context and run.
        //
        try (Closeable it = SuonosLib.enterCtx()) {
            container.init();
            while (true) {
                run(args);
            }
        }
    }

    // Get all artists?

    //
    // GET http://127.0.0.1:8080/api/tracks?q=genres:Classical
    // GET http://127.0.0.1:8080/api/tracks/-59Z0rXcs6nJ0
    // PUT http://127.0.0.1:8080/api/tracks/-5pCVCxpHsgy0/rating?val=5

    // Browse albums - Select "All Genres beginning with B". Sort by album
    // title:
    //
    // GET http://127.0.0.1:8080/api/albums?q=genres_f:b*&s=title

    // Browse albums - Select "All Artists beginning with B". Sort by album
    // title:
    //
    // GET http://127.0.0.1:8080/api/albums?q=artists_f:b*&s=title

    // Need to query facets:
    // GET http://127.0.0.1:8080/api/facets?ifields=track_artists&filter=B
    // Return all unique artists. filter=B, only return those with prefix B
    //
    // GET
    // http://127.0.0.1:8080/api/tracks?fl=title,id,tags&q=track_genres_f:c*&s=track_genres_f
    //
    private void run(String[] args) throws IOException {
        SuonosLib lib = SuonosLib.lib();

        lib.stmt().close();

        buildTestMusicLib();
        buildTestPlaylists();
        // loadObjects();
        // testMPlayer();
        // testPlayer();

        // if (true) return;

        SwtInterface app = Context.getInstanceOf(SwtInterface.class);
        app.run();

        if (true)
            System.exit(1);

        MediaPlayerSvcs playerSvcs = lib.playerSvcs();
        Playable item;

        // Locking up:
        // MediaPlayer:thread
        // -> MPlayerprocess.stop()
        // -> sendCmd("stop");
        // -> blocked
        //
        // MPlayerprocess:thread
        // -> processEof()
        // -> queueCmd(cmd)
        // -> blocked
        //
        // main:thread
        // -> queueCmd(cmd)
        // -> blocked
        //

        playerSvcs.start();
        item = getTestItem(playerSvcs, "./testmedia/2secs-A.ogg");
        playerSvcs.playItem(item);
        item = getTestItem(playerSvcs, "./testmedia/2secs-A.ogg");
        playerSvcs.playItem(item);

        item = getTestItem(playerSvcs, "./testmedia/2secs-A.ogg");
        playerSvcs.queueItem(item);
        item = getTestItem(playerSvcs, "./testmedia/2secs-B.ogg");
        playerSvcs.queueItem(item);
        item = getTestItem(playerSvcs, "./testmedia/2secs-C.ogg");
        playerSvcs.queueItem(item);
        item = getTestItem(playerSvcs, "./testmedia/2secs-D.ogg");
        playerSvcs.queueItem(item);
        playerSvcs.start();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.previousTrack();
        playerSvcs.previousTrack();
        playerSvcs.previousTrack();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        // playerSvcs.stop();

        item = playerSvcs.getItem(null, null, "./testmedia/bunny.mp4");
        playerSvcs.playItem(item);

        item = playerSvcs.getItem(null, null, "./testmedia/test\"ø.ogg");
        playerSvcs.playItem(item);

        playerSvcs.previousTrack();
        playerSvcs.previousTrack();

        playerSvcs.pause(true);
        playerSvcs.pause(false);
        playerSvcs.pause(false);
        playerSvcs.volume(2);
        playerSvcs.mute(true);
        playerSvcs.mute(false);
        playerSvcs.seek(0, true);
        playerSvcs.seek(60, false);
        // playerSvcs.stop();

        item = playerSvcs.getItem(null, null, "./testmedia/2secs-A.ogg");
        playerSvcs.queueItem(item);
        item = playerSvcs.getItem(null, null, "./testmedia/2secs-B.ogg");
        playerSvcs.queueItem(item);
        item = playerSvcs.getItem(null, null, "./testmedia/2secs-C.ogg");
        playerSvcs.queueItem(item);
        item = playerSvcs.getItem(null, null, "./testmedia/2secs-D.ogg");
        playerSvcs.queueItem(item);
        playerSvcs.start();
        playerSvcs.stop();
        playerSvcs.previousTrack();
        playerSvcs.previousTrack();
        playerSvcs.previousTrack();
        playerSvcs.previousTrack();

        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();

        playerSvcs.stop();
        item = playerSvcs.getItem(null, null, "./testmedia/test-file-not-found.ogg");
        playerSvcs.playItem(item);
        playerSvcs.stop();
        item = playerSvcs.getItem(null, null, "./testmedia/err.ogg");
        playerSvcs.playItem(item);
        item = playerSvcs.getItem(null, null, "./testmedia/inv.ogg");
        playerSvcs.playItem(item);
        playerSvcs.stop();
        playerSvcs.quit();

        System.out.println("finished");
    }

    private Playable getTestItem(MediaPlayerSvcs playerSvcs, String s) {
        Playable item = playerSvcs.getItem(null, null, s);
        return item;
    }

    private void testMPlayer() throws IOException {
        MPlayerBuilder b = new MPlayerBuilder();
        PlayerItem item = new PlayerItem("none", "./testmedia/test\"ø.ogg", "N");
        PlayerInstance inst = b.createInst(new PlayerCallback() {

            @Override
            public void playbackError(String msg) {
                System.out.println("err");
            }

            @Override
            public void playbackStarted(PlaybackInfo item) {
                System.out.println("started");
            }

            @Override
            public void playerFinished(String statusCode) {
                System.out.println("finished");
            }

            @Override
            public void playerPosUpdate(int timeInSecs) {
                System.out.println("posUpdated " + timeInSecs);
            }

        });
        inst.start();
        inst.playFile(item);

        for (int i = 0; i != 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            inst.pollPlayback();
        }
        inst.stop();
        for (int i = 0; i != 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            inst.pollPlayback();
        }

        inst.getClass();
    }

    private void testPlayer() throws IOException {
        SuonosLib lib = SuonosLib.lib();
        PlayableStoreObject item = new PlayableStoreObject();
        Statement stmt = SuonosLib.lib().stmt();
        MediaPlayerSvcs playerSvcs = lib.playerSvcs();

        QueryResults<MusicAlbum> res = stmt.queryHelper().setType(MusicAlbum.class).query();
        MusicAlbum album = res.first();

        playerSvcs.queueItem(album);
        playerSvcs.setShuffleMode(true);
        playerSvcs.setShuffleMode(false);
        playerSvcs.setShuffleMode(true);
        playerSvcs.setPlayListMode(PlayListMode.REPEAT);
        playerSvcs.setAutoAdvanceMode(false);
        playerSvcs.setAutoAdvanceMode(true);
        playerSvcs.start();
        playerSvcs.nextTrack();
        playerSvcs.nextTrack();
        playerSvcs.previousTrack();
        playerSvcs.getPlayList();
        playerSvcs.setShuffleMode(false);
        playerSvcs.setShuffleMode(false);
        playerSvcs.setShuffleMode(true);
    }

    private void buildTestPlaylists() throws IOException {
        SuonosLib lib = SuonosLib.lib();
        Playlist pl = Playlist.create("My Playlist");
        pl.addItem("nGVzpvmsC8Dg");
        pl.addItem("nGVzpw9yjwDC");
        pl.addItem("nGVzpxlN7kJR");
        lib.stmt().createObject(pl);
        pl = Playlist.create("My Playlist 2");
        pl.addItem("nGVzgzrjzF5K");
        pl.addItem("nGVzgC3pw4BS");
        pl.addItem("nGVzgBq8Z17G");
        lib.stmt().createObject(pl);
        lib.stmt().commit();
        lib.stmt().close();
    }

    private void buildTestMusicLib() throws IOException {
        MusicLib lib = Context.instanceOf(MusicLib.class);

        RebuildMusicLibraryTask task = new RebuildMusicLibraryTask();
        // task.setRebuildMetaData();

        task.run(lib);
    }

    private static void testu() {
        for (int i = 0; i != 10; i++) {
            String s = UUID.randomUUID().toString();
            System.out.println(s + " " + (s.length()));

            s = Uids.newUID();
            System.out.println(s + " " + (s.length()));
        }
    }

    public void loadObjects() throws IOException {
        LuceneIndex luceneIndex = Context.instanceOf(LuceneIndex.class);

        try (Statement statement = luceneIndex.getStatement()) {

            QueryResults<MusicAlbum> list;

            // autoBuilt:1
            // BooleanQuery np = new BooleanQuery();
            // np.add(statement.createQuery("autoBuilt", "1"), Occur.MUST);
            // np.add(statement.createQuery("musicLibBuildId",
            // "Y3Mkd/OYRMSK1i+T2SfY2A"), Occur.MUST_NOT);

            // Query q = statement.createQuery("musicLibBuildId",
            // "Y3Mkd/OYRMSK1i+T2SfY2A");
            // list = statement.queryObjects(MusicTrack.class, np);
            // Query q = statement.createQuery("genres:Classical");
            // Sort s = statement.parseSortExpr(MusicAlbum.class, "title");
            // list = statement.queryObjects(MusicAlbum.class, q, s);
            // list.all();
            // list.getClass();
        }
    }
}
