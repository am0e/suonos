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
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import com.github.am0e.commons.AntLib;

import suonos.app.utils.FilesUtils;
import suonos.app.utils.TagUtils;
import suonos.lucene.Statement;

/**
 * Album meta data. This is stored in the album folder in json format as
 * ".suonos.meta".
 * 
 * @author anthony
 */
public final class AlbumMetaData {
    /**
     * Album ID
     */
    private String folderId;

    /**
     * Date when the album was imported.
     */
    private Date importDate;

    /**
     * Path to the meta data file. Stored here for the save() method.
     */
    private transient Path path;

    /**
     * The album rating.
     */
    private int rating;

    /**
     * Track data.
     */
    private List<TrackMetaData> trackMetaData;

    public AlbumMetaData() {
        trackMetaData = AntLib.newList();
    }

    /**
     * Save the meta data to the file system.
     * 
     * @throws IOException
     */
    public void save() throws IOException {
        FilesUtils.saveObject(this, path);
    }

    /**
     * @return the id
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setFolderId(String id) {
        this.folderId = id;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public TrackMetaData addTrack(String trackFileName, AlbumMetaData curMetaData) {
        TrackMetaData it = curMetaData.removeTrackMetaData(trackFileName);
        if (it == null) {
            it = new TrackMetaData();
            it.setTrackFileName(getFileId(trackFileName));
            it.setTrackId(suonos.app.utils.Uids.newUID());
            Statement.log.debug("New Track UID {}", it.getTrackId());
        }
        trackMetaData.add(it);
        return it;
    }

    private TrackMetaData removeTrackMetaData(String trackFileName) {
        TrackMetaData it = getTrackMetaData(trackFileName);
        if (it != null) {
            trackMetaData.remove(it);
        }
        return it;
    }

    public TrackMetaData getTrackMetaData(String trackFileName) {
        trackFileName = getFileId(trackFileName);

        for (TrackMetaData it : trackMetaData) {
            if (it.getTrackFileName().equals(trackFileName))
                return it;
        }
        return null;
    }

    public List<TrackMetaData> getTrackMetaData() {
        return trackMetaData;
    }

    public void setTrackMetaData(List<TrackMetaData> trackMetaData) {
        this.trackMetaData = trackMetaData;
    }

    public void updateFrom(AlbumMetaData other) {
        this.folderId = other.folderId;
        this.path = other.path;
        this.importDate = other.importDate;
    }

    private String getFileId(String trackFileName) {
        return TagUtils.convertStringToId(trackFileName.toLowerCase());
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
}
