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

package suonos.controllers;

import java.io.IOException;

import com.github.am0e.webc.WebException;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.models.Playable;
import suonos.models.playlist.PlayListMode;
import suonos.services.player.MediaPlayerSvcs;

/**
 * Player controller. GET /player/playlist Get the media player Playlist.
 * 
 * PUT /player/cmd?c=pause PUT /player/cmd?c=unpause PUT /player/cmd?c=mute PUT
 * /player/cmd?c=unmute PUT /player/cmd?c=previous PUT /player/cmd?c=next PUT
 * /player/cmd?c=play&href=http://movies.net/mymovies/muppets.mp4 PUT
 * /player/cmd?c=play&id=387djdjd7fb4 (Playlist/Track/Album/Movie)
 * 
 * @author anthony
 *
 */
public final class PlayerController extends Controller {

    public PlayerController(ActionCtx ctx) {
        super(ctx);
    }

    private MediaPlayerSvcs playerSvcs() {
        return lib.playerSvcs();
    }

    @Action
    public Object put_cmd() throws IOException {
        switch (ctx.param("c")) {
        case "pause": {
            playerSvcs().pause(true);
            break;
        }

        case "unpause": {
            playerSvcs().pause(false);
            break;
        }

        case "mute": {
            playerSvcs().mute(true);
            break;
        }

        case "unmute": {
            playerSvcs().mute(false);
            break;
        }

        case "shuffle": {
            playerSvcs().setShuffleMode(true);
            break;
        }

        case "unshuffle": {
            playerSvcs().setShuffleMode(false);
            break;
        }

        case "repeat": {
            playerSvcs().setPlayListMode(PlayListMode.REPEAT);
            break;
        }

        case "playToEnd": {
            playerSvcs().setPlayListMode(PlayListMode.TO_END);
            break;
        }

        case "previous": {
            playerSvcs().previousTrack();
            break;
        }

        case "next": {
            playerSvcs().nextTrack();
            break;
        }

        case "play": {
            return playCmd();
        }

        case "add": {
            return addCmd();
        }

        case "start": {
            playerSvcs().start();
            break;
        }

        case "stop": {
            playerSvcs().stop();
            break;
        }

        case "clear": {
            playerSvcs().clearPlaylist();
            break;
        }

        }

        return json().success("");

    }

    private Object addCmd() throws IOException {

        // Add track / playlist / album / movie to player playlist.
        //
        playerSvcs().queueItem(getPlayable());

        return json().success("added");
    }

    public Object playCmd() throws IOException {

        MediaPlayerSvcs mediaPlayer = playerSvcs();
        Playable playable = getPlayable();

        mediaPlayer.playItem(playable);

        return json().success("playing");
    }

    private Playable getPlayable() throws IOException {
        if (ctx.params().contains("href")) {
            String url = ctx.param("href");
            String title = ctx.param("title", url);

            return playerSvcs().getPlayableForUrl(title, url);

        } else if (ctx.params().contains("id")) {
            return querySvcs().getObject();

        } else {
            throw WebException.badRequest("Missing `id` query param");
        }

    }

    /**
     * Get the playlist from the media player.
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object playlist() throws IOException {
        return jsonData(playerSvcs().getPlayList().playlist);
    }

}
