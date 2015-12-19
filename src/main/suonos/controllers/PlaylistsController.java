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

import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.models.playlist.Playlist;

/**
 * Playlist controller.
 * 
 * GET /api/playlists/Id GET /api/playlists/Id?display=1 POST /api/playlists PUT
 * /api/playlists/Id GET /api/playlists GET /api/playlists?fields=title,id
 * 
 * @author anthony
 *
 */
public final class PlaylistsController extends StoredItemController<Playlist> {

    public PlaylistsController(ActionCtx ctx) {
        super(ctx, Playlist.class);
    }

    @Action
    public Object delete() throws IOException {
        // Get the playlist.
        //
        Playlist pl = querySvcs().getObject();

        // delete the playlist.
        //
        return querySvcs().deleteObject(pl);
    }

    // POST api/playlists
    //
    @Action
    public Object post() throws IOException {
        // Get the playlist.
        //
        Playlist pl = querySvcs().getCreateObject(Playlist.class);

        // Create the playlist.
        //
        return querySvcs().createObject(pl);
    }

    /**
     * PUT api/playlists/1234
     * 
     * data = json PlayList
     * 
     * @throws IOException
     */
    @Action
    public Object put() throws IOException {

        // Get the playlist.
        //
        Playlist pl = querySvcs().getPutObject(Playlist.class);

        // Update the playlist.
        //
        return querySvcs().putObject(pl);
    }
}
