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

import suonos.models.music.MusicTrack;

/**
 * Tracks controller.
 * 
 * GET /api/tracks GET /api/tracks/-id
 * 
 * @author anthony
 *
 */
public final class TracksController extends StoredItemController<MusicTrack> {

    public TracksController(ActionCtx ctx) {
        super(ctx, MusicTrack.class);
    }

    /**
     * GET /ws/tracks/-abf2323/download
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object download() throws IOException {
        // Query the album so that a not found exception is thrown if the
        // id is invalid.
        //
        MusicTrack track = querySvcs().getObject();

        return ctx.sendFile(lib.getMusicLib().getTrackFolderPath(track)).disposition("attachment")
                .type(lib.getMusicLib().getMimeType(track)).fileName(track.getTitleAsFileName());
    }
}
