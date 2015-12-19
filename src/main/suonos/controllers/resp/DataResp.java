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

package suonos.controllers.resp;

import java.io.IOException;
import java.io.Writer;

import com.github.am0e.commons.json.JsonObjectWriter;
import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.webc.action.response.JsonResponse;

import suonos.app.SuonosLib;
import suonos.lucene.QueryResults;
import suonos.models.StoreObject;
import suonos.models.media.MediaTagValues;
import suonos.models.media.MediaTags;
import suonos.models.music.MusicAlbum;
import suonos.models.music.MusicLib;
import suonos.models.playlist.Playlist;
import suonos.models.playlist.PlaylistItem;

/**
 * Common response handler for Json requests
 * 
 * @author anthony
 *
 */
public class DataResp extends JsonResponse {

    private String nextLink;
    private MusicLib musicLib;
    private String prevLink;
    QueryResults<StoreObject> _rel = null;

    public DataResp(Object model) {
        super(model);
    }

    public DataResp setNextLink(String link) {
        this.nextLink = link;
        return this;
    }

    public DataResp setPrevLink(String link) {
        this.prevLink = link;
        return this;
    }

    private MusicLib getMusicLib() {
        if (musicLib == null) {
            musicLib = SuonosLib.lib().getMusicLib();
        }
        return musicLib;
    }

    @Override
    protected void serializeJavaObject(JsonObjectWriter jw, Writer writer) throws IOException {

        jw.setTypeFieldName("type");
        jw.includeTypeInfo();

        // Override tag values to output json friendly response for client.
        // We output as:
        // {
        // artists: ["Schiff", "Joy Division", "Pink Floyd" ],
        // genres: ["a", "b" ]
        //
        jw.registerAdaptor(MediaTagValues.class, (_jw, tagValues) -> {
            jw.startObj();
            jw.genObject(tagValues.getSortedValues());
            jw.endObj();
        });

        jw.registerAdaptor(MusicAlbum.class, (_jw, album) -> {
            jw.startObj();

            if (jw.isTarget("list")) {
                // Target is a list. Return back a subset of the data to the
                // client.
                // Artists is returned back as Various Artists, etc.
                //
                jw.genTypeField(album);
                jw.genValue("id", album.getId());
                jw.genValue("coverArtwork", album.isCoverArtwork());
                jw.genValue("title", album.getTitle());
                jw.genValue("year", album.getTags().getTagValue(MediaTags.YEAR));
                jw.genValue("artists", album.getTags().getTagValue(MediaTags.ARTISTS));
                jw.genValue("genres", album.getTags().getTagValue(MediaTags.GENRES));
                jw.genValue("composers", album.getTags().getTagValue(MediaTags.COMPOSERS));

            } else {
                jw.genObjectFields(album);
            }

            if (album.isCoverArtwork()) {
                jw.genValue("coverArtworkUrl", Msgs.format("/ws/albums/-{}/coverart", album.getId()));
            }

            if (jw.includeRelation("tracks")) {
                jw.genValue("tracks", album.getTracks());
            }

            jw.endObj();
        });

        jw.registerAdaptor(Playlist.class, (_jw, playList) -> {
            // Query the objects related to the playlist and store locally. We
            // know that the serializer will
            // invoke the playlistitem adaptor next, so this is ok!
            //
            _rel = playList.getRelatedItems();

            // Do default processing. Note it won't serialize the playlist
            // content.
            //
            jw.startObj();
            jw.genObjectFields(playList);
            jw.endObj();
        });

        jw.registerAdaptor(PlaylistItem.class, (_jw, item) -> {
            // Get the object related to the playlist item. Typically a Track.
            //
            StoreObject obj = _rel.get(item.getItemId());

            jw.startObj();

            // Add the playlist item id.
            //
            jw.genValue("itemId", item.getItemId());

            if (obj != null) {
                // Generate the referenced item.
                //
                jw.genValue("rel", obj);

                jw.endObj();
            }
        });

        jw.registerAdaptor(QueryResults.class, (_jw, results) -> {
            jw.startObj();
            jw.genValue("totalHits", results.totalHits());
            jw.genValue("results", results.iterator());
            jw.endObj();
        });

        // As we are nesting the response in "data" we call skipTopLevelField()
        // to ignore "data."
        // in field paths for only,exclude,include query parameters.
        //
        jw.start(writer);
        jw.setFilterContextPath("data.");
        jw.startObj();
        jw.genValue("data", this.model);
        if (nextLink != null || prevLink != null) {
            jw.startObj("links");
            jw.genValue("next", nextLink);
            jw.genValue("prev", prevLink);
            jw.endObj();
        }
        jw.endObj();
        jw.flush();
    }
}
