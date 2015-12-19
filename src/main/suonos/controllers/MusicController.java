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

import com.github.am0e.commons.providers.Context;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.models.media.MediaTag;
import suonos.models.media.MediaTags;

public class MusicController extends Controller {

    public MusicController(ActionCtx ctx) {
        super(ctx);
    }

    /**
     * GET /api/music/tags/Artists Get all tag values for Artist GET
     * /api/music/tags/Genres Get all tag values for Genres GET /api/musiclib/
     */
    @Action
    public void tags(ActionCtx ctx) {
        // Get the tag id.
        //
        String tagId = ctx.param("id");

        // Get media tags.
        //
        MediaTags tags = Context.instanceOf(MediaTags.class);
        MediaTag tag = tags.getMediaTag(tagId);

    }

}
