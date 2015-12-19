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
package suonos.models.music;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.github.am0e.commons.msgs.Msgs;

import suonos.app.SuonosLib;
import suonos.app.utils.BeanSort;
import suonos.lucene.DynamicIndexedFieldCtx;
import suonos.lucene.DynamicIndexedFields;
import suonos.models.Playable;
import suonos.models.PlayableContainer;
import suonos.models.Rateable;
import suonos.models.StoreObject;
import suonos.models.annotations.IndexDoc;
import suonos.models.annotations.IndexField;
import suonos.models.annotations.Serialized;
import suonos.models.media.MediaTagValues;
import suonos.models.media.MediaTags;

@Serialized
@IndexDoc(abbrev = "album")
public final class MusicAlbum extends StoreObject
        implements Rateable, Playable, PlayableContainer, DynamicIndexedFields {

    /**
     * Date when the album was imported.
     */
    @IndexField(indexed = true, docValues = true, analyzer = "date")
    private Date importDate;

    /**
     * Album title. filterable to allow for browsing albums by the first letter.
     * Also allows for sorting.
     * 
     */
    @IndexField(indexed = true, filterable = true, analyzer = "default")
    private String title;

    /**
     * Path to the folder containing the album. Relative to the Media Root
     * Folder.
     */
    @IndexField(indexed = false)
    private String path;

    /**
     * Build id. Every time the music library is rebuilt, the objects rebuilt
     * like the albums and tracks aquire a new build id. Note that all objects
     * in the music library that are constructed from files must have this
     * field.
     */
    @IndexField(idField = true, prefixed = false, analyzer = "keyword")
    private String musicLibBuildId;

    /**
     * Autobuilt. Used with #musicLibBuildId to delete the previous objects from
     * the index.
     */
    @IndexField(prefixed = false, analyzer = "keyword")
    private boolean musicLibAutoBuilt;

    /**
     * Rating.
     */
    @IndexField(analyzer = "numeric")
    private int rating;

    /**
     * Does the album have cover artwork?
     */
    private boolean coverArtwork;

    /**
     * Cached tracks.
     */
    private transient List<MusicTrack> _tracks;

    /**
     * Cached tags. Loaded once.
     */
    @IndexField
    private MediaTagValues tags;

    public final static String COVER_LG = "cover-lg";
    public final static String COVER_XS = "cover-xs";
    public final static String COVER_SM = "cover-sm";

    public void setTitle(String albumTitle) {
        this.title = albumTitle;
    }

    public String getTitle() {
        return title;
    }

    public MediaTagValues getTags() {
        if (tags == null) {
            tags = new MediaTagValues(this);
        }
        return tags;
    }

    public void setTags(MediaTagValues tags) {
        this.tags = tags;
    }

    /**
     * @return the musicLibBuildId
     */
    public String getMusicLibBuildId() {
        return musicLibBuildId;
    }

    /**
     * @param id
     *            the musicLibBuildId to set
     */
    public void setMusicLibBuildId(String id) {
        this.musicLibBuildId = id;
    }

    /**
     * @return the musicLibAutoBuilt
     */
    public boolean isMusicLibAutoBuilt() {
        return musicLibAutoBuilt;
    }

    /**
     * @param musicLibAutoBuilt
     *            the musicLibAutoBuilt to set
     */
    public void setMusicLibAutoBuilt(boolean musicLibAutoBuilt) {
        this.musicLibAutoBuilt = musicLibAutoBuilt;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * @param rating
     *            the rating to set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public List<? extends Playable> getPlayableItems() throws IOException {
        return getTracks();
    }

    /**
     * @return the importDate
     */
    public Date getImportDate() {
        return importDate;
    }

    /**
     * @param importDate
     *            the importDate to set
     */
    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    /**
     * Get the tracks for this album.
     * 
     * @return
     * @throws IOException
     */
    public List<MusicTrack> getTracks() throws IOException {
        if (_tracks == null) {
            _tracks = SuonosLib.lib().stmt().queryRelated(MusicTrack.class,
                    Msgs.format("track_albumId:{}", this.getId()));

            BeanSort.sort(_tracks, "trackNumber", false);
        }

        return _tracks;
    }

    /**
     * @return the coverArtwork
     */
    public boolean isCoverArtwork() {
        return coverArtwork;
    }

    /**
     * @param coverArtwork
     *            the coverArtwork to set
     */
    public void setCoverArtwork(boolean coverArtwork) {
        this.coverArtwork = coverArtwork;
    }

    @Override
    public void indexFields(DynamicIndexedFieldCtx ctx) {

        // Construct the "searchterms" keyword.
        //
        StringBuilder keywords = new StringBuilder();
        if (tags != null) {
            tags.getTagValues(MediaTags.ARTISTS, keywords);
            tags.getTagValues(MediaTags.COMPOSERS, keywords);
            tags.getTagValues(MediaTags.CONDUCTORS, keywords);
            tags.getTagValues(MediaTags.GENRES, keywords);
            if (title != null) {
                keywords.append(title);
            }
        }
        ctx.indexField("_searchterms", keywords.toString());
    }
}
