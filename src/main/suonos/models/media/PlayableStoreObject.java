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
package suonos.models.media;

import java.nio.file.Path;
import java.nio.file.Paths;

import suonos.models.Playable;
import suonos.models.Rateable;
import suonos.models.StoreObject;
import suonos.models.annotations.IndexField;

public class PlayableStoreObject extends StoreObject implements Rateable, Playable {
    /**
     * Format.
     */
    @IndexField(analyzer = "keyword")
    private String format;

    /**
     * Encoding string. Eg: "Ogg Vorbis"
     */
    @IndexField(analyzer = "keyword")
    private String encoding;

    /**
     * Path to the physical file. Relative to the Media Root Folder.
     */
    @IndexField(analyzer = "keyword", indexed = false)
    private String path;

    /**
     * item title.
     */
    @IndexField(analyzer = "default", filterable = true)
    private String title;

    /**
     * Length. For music/video this is the track length in seconds.
     */
    @IndexField(analyzer = "numeric")
    private long length;

    /**
     * Bit Rate in kbs.
     */
    @IndexField(analyzer = "numeric")
    private int bitRate;

    /**
     * Sample rate in Hz
     */
    @IndexField(analyzer = "numeric")
    private int sampleRate;

    /**
     * Rating.
     */
    @IndexField(analyzer = "numeric")
    private int rating;

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding
     *            the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
     * @return the path
     */
    public Path getFilePath() {
        return Paths.get(path);
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the track title
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
     * @return the bitRate
     */
    public int getBitRate() {
        return bitRate;
    }

    /**
     * @param bitRate
     *            the bitRate to set in kbs.
     */
    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    /**
     * @return the sampleRate
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate
     *            the sampleRate to set
     */
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
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

    public String getTitleAsFileName() {
        int ndx = path.lastIndexOf('.');
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append(ndx == -1 ? "" : path.substring(ndx));
        return sb.toString();
    }

}
