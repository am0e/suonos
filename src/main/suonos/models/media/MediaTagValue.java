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

import com.github.am0e.commons.msgs.Msgs;

import suonos.models.annotations.Serialized;

@Serialized
public class MediaTagValue implements Comparable<MediaTagValue> {
    String value = "";
    int tagDefId;
    transient MediaTag _mediaTag;

    public MediaTagValue() {
    }

    public String toString() {
        return Msgs.format("{}:{}", _mediaTag, value);
    }

    public MediaTag getMediaTagDef() {
        return _mediaTag;
    }

    /**
     * @return the tag values
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the tagDefId
     */
    public int getTagDefId() {
        return tagDefId;
    }

    /**
     * @param tagDefId
     *            the tagDefId to set
     */
    public void setTagDefId(int tagDefId) {
        this.tagDefId = tagDefId;
    }

    public void setTag(MediaTag tag) {
        this.tagDefId = tag.getTagId();
        this._mediaTag = tag;
    }

    @Override
    public int compareTo(MediaTagValue o) {
        if (this.tagDefId != o.tagDefId) {
            return this.tagDefId - o.tagDefId;
        }

        return this.value.compareTo(o.value);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof MediaTagValue) == false)
            return false;

        MediaTagValue other = (MediaTagValue) obj;
        return tagDefId == other.tagDefId && value.equals(other.value);
    }
}
