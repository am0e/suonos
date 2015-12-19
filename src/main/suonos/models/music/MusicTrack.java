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

import com.github.am0e.commons.msgs.Msgs;

import suonos.models.annotations.IndexDoc;
import suonos.models.annotations.IndexField;
import suonos.models.annotations.Serialized;
import suonos.models.media.Artist;
import suonos.models.media.Composer;
import suonos.models.media.MediaTagValues;
import suonos.models.media.MediaTags;
import suonos.models.media.PlayableStoreObject;

/**
 * Represents a music track in an album.
 * 
 * @author anthony
 */
@Serialized
@IndexDoc(abbrev = "track")
public final class MusicTrack extends PlayableStoreObject {

    @IndexField(idField = true, analyzer = "keyword")
    private String albumId;

    @IndexField(analyzer = "numeric")
    private int trackNumber;

    /**
     * tags.
     */
    @IndexField(analyzer = "numeric")
    private MediaTagValues tags;

    /**
     * Build Id. Every time the music library is rebuilt, the objects rebuilt
     * like the albums and tracks aquire a new Id. This is used to delete the
     * previous objects from the index. Note that all objects in the music
     * library that are constructed from files must have this field. Playlists,
     * etc, do not have this field.
     */
    @IndexField(idField = true, prefixed = false, analyzer = "keyword")
    private String musicLibBuildId;

    /**
     * Autobuilt. Used with musicLibBuildId to delete the previous objects from
     * the index.
     */
    @IndexField(prefixed = false, analyzer = "keyword")
    private boolean musicLibAutoBuilt;

    public String toString() {
        return Msgs.format("{}:{}:{}", getTitle(), trackNumber, getPath());
    }

    /**
     * 
     * @return the artists. There may be more than 1 artist!
     */
    public Artist[] getArtists() {
        return (Artist[]) getTags().getTags(MediaTags.ARTISTS);
    }

    /**
     * @return The composers for this track.
     */
    public Composer[] getComposers() {
        return (Composer[]) getTags().getTags(MediaTags.COMPOSERS);
    }

    /**
     * @return The album that this track belongs to.
     */
    public MusicAlbum getAlbum() {
        return null;
    }

    /**
     * @return the albumId
     */
    public String getAlbumId() {
        return albumId;
    }

    /**
     * @param albumId
     *            the albumId to set
     */
    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    /**
     * @return the trackNumber
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /**
     * @param trackNumber
     *            the trackNumber to set
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
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
}
