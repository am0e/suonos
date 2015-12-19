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
package suonos.models.playlist;

import java.io.IOException;
import java.util.List;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.utils.Validate;

import suonos.app.SuonosLib;
import suonos.app.utils.Uids;
import suonos.lucene.QueryResults;
import suonos.models.Playable;
import suonos.models.PlayableContainer;
import suonos.models.StoreObject;
import suonos.models.annotations.IndexDoc;
import suonos.models.annotations.IndexField;
import suonos.models.annotations.Serialized;

@Serialized
@IndexDoc(abbrev = "playlist")
public class Playlist extends StoreObject implements Playable, PlayableContainer {
    /**
     * Playlist title.
     */
    @IndexField(indexed = true, filterable = true, analyzer = "default")
    private String title;

    /**
     * The list of tracks in the playlist.
     */
    private List<PlaylistItem> items = AntLib.newList();

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(List<PlaylistItem> items) {
        this.items = items;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }

    public static Playlist create(String title) {
        Playlist it = new Playlist();
        it.setTitle(title);
        it.setId(Uids.newUID());
        return it;
    }

    public void addItem(String id) {
        this.items.add(PlaylistItem.create(id));
    }

    public QueryResults<StoreObject> getRelatedItems() throws IOException {
        // Query the objects related to the playlist.
        //
        return SuonosLib.lib().stmt().queryRelated(getItems());
    }

    @Override
    public List<? extends Playable> getPlayableItems() throws IOException {
        List<Playable> playables = AntLib.newList();

        for (StoreObject it : getRelatedItems()) {
            if (it instanceof Playable) {
                playables.add((Playable) it);
            }
        }
        return playables;
    }

    @Override
    public String getPath() {
        throw Validate.notImplemented();
    }
}
