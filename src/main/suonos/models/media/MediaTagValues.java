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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.providers.Context;

import suonos.lucene.DynamicIndexedFieldCtx;
import suonos.lucene.DynamicIndexedFields;
import suonos.models.StoreObject;

public class MediaTagValues implements DynamicIndexedFields, Iterable<MediaTagValue> {

    private List<MediaTagValue> values;

    private transient MediaTags tags = Context.instanceOf(MediaTags.class);

    public MediaTagValues() {
        this.values = new ArrayList<>();
    }

    public MediaTagValues(Collection<MediaTagValue> col) {
        this.values = new ArrayList<>(col);
    }

    public MediaTagValues(StoreObject mediaItem) {
        this();
    }

    public MediaTagValue[] getTags(String tagId) {
        return getTags(tags.getMediaTag(tagId));
    }

    public MediaTagValue[] getTags(MediaTag tag) {
        List<MediaTagValue> list = new ArrayList<>();

        for (MediaTagValue it : values) {
            if (it.getTagDefId() == tag.getTagId()) {
                list.add(it);
            }
        }

        return list.toArray(new MediaTagValue[0]);
    }

    public MediaTagValue[] getGenres() {
        return getTags(MediaTags.GENRES);
    }

    public void add(MediaTagValue tagValue) {
        // Ignore duplicates.
        //
        if (values.contains(tagValue) == false) {
            values.add(tagValue);
        }
    }

    public List<MediaTagValue> getValues() {
        return values;
    }

    public void setValues(List<MediaTagValue> values) {
        this.values = values;
    }

    @Override
    public void indexFields(DynamicIndexedFieldCtx ctx) {
        for (MediaTagValue it : this.values) {
            ctx.indexField(it._mediaTag.name, it.getValue());
        }
    }

    public String getTagValue(MediaTag tag) {
        MediaTagValue[] tags = getTags(tag);

        if (tags.length > 1) {
            // Is there a plural label. Eg "Genres".
            //
            String label = tags[0].getMediaTagDef().getVariousLabel();

            // Return "Various Genres", etc.
            //
            if (label != null) {
                return label;
            }
        }

        if (tags.length == 1) {
            return tags[0].getValue();
        }

        return null;
    }

    @Override
    public Iterator<MediaTagValue> iterator() {
        return this.values.iterator();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSortedValues() {

        Map<String, Object> map = AntLib.newHashMap();
        for (MediaTagValue it : values) {
            String key = it.getMediaTagDef().name;
            Object o = map.get(key);
            if (o == null && !it.getMediaTagDef().multiValue) {
                map.put(key, it.value);
            } else if (o instanceof List) {
                List<Object> list = (List<Object>) o;
                list.add(it.value);
            } else {
                List<Object> list = AntLib.newList();
                if (o != null)
                    list.add(o);
                list.add(it.value);
                map.put(key, list);
            }
        }
        return map;
    }

    public void getTagValues(MediaTag tag, StringBuilder sb) {
        MediaTagValue[] tags = getTags(tag);

        for (MediaTagValue it : tags) {
            sb.append(it.value);
            sb.append(' ');
        }
    }

}
