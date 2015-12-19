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

/**
 * Meta data for a track.
 * 
 * @author anthony
 */
public class TrackMetaData {
    /**
     * The track album Id.
     */
    private String albumId;

    /**
     * Track file name. Eg "18. The Beat - Ranking Full Stop.ogg". There is no
     * path name.
     */
    private String trackFileName;

    /**
     * The track Id
     */
    private String trackId;

    /**
     * The track rating
     */
    private int rating;

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTrackFileName() {
        return trackFileName;
    }

    public void setTrackFileName(String trackFileName) {
        this.trackFileName = trackFileName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }
}
