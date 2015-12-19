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

import java.lang.reflect.Constructor;

import org.apache.lucene.index.IndexOptions;
import org.jaudiotagger.tag.FieldKey;

import com.github.am0e.commons.beans.BeanUtils;
import com.github.am0e.commons.msgs.Msgs;

import suonos.lucene.IndexModels;
import suonos.lucene.fields.IndexedField;
import suonos.lucene.fields.IndexedFieldType;

public final class MediaTag {
    /**
     * Tag ID.
     */
    int tagId;

    /**
     * Tag name. Eg "Artist", "Composer", "Track"
     */
    String name;

    Constructor<? extends MediaTagValue> valueCtor;

    FieldKey audioTaggerKey;

    /**
     * Is the value an id?
     */
    boolean identifier;

    String analyzer;

    /**
     * Add the tag to the parent container. ie the album.
     */
    boolean addToContainer;

    public boolean filterable;

    public boolean docValues;

    public boolean indexed;

    public boolean multiValue;

    /**
     * Is the tag prefixed with the model? Eg album_genre
     */
    public boolean prefixed;

    public String pluralLabel;

    public static class Builder {
        MediaTag tag = new MediaTag();

        public Builder indexed() {
            tag.indexed = true;
            return this;
        }

        public Builder filterable() {
            tag.filterable = true;
            return this;
        }

        public Builder name(String name) {
            tag.name = name;
            return this;
        }

        public Builder pluralLabel(String pluralLabel) {
            tag.pluralLabel = pluralLabel;
            return this;
        }

        public MediaTag build() {
            return tag.init();
        }

        public Builder id(int id) {
            tag.tagId = id;
            return this;
        }

        public Builder valueClass(Class<? extends MediaTagValue> c) {
            tag.valueCtor = BeanUtils.getConstructor(c);
            return this;
        }

        public Builder addToParent() {
            tag.addToContainer = true;
            return this;
        }

        public Builder key(FieldKey key) {
            tag.audioTaggerKey = key;
            return this;
        }

        public Builder identifier() {
            tag.identifier = true;
            return this;
        }

        public Builder analyzer(String analyzer) {
            tag.analyzer = analyzer;
            return this;
        }

        public Builder prefixed() {
            tag.prefixed = true;
            return this;
        }

        public Builder docValues() {
            tag.docValues = true;
            return this;
        }

        public Builder multiValue() {
            tag.multiValue = true;
            return this;
        }
    }

    public MediaTag() {
    }

    public MediaTag init() {
        return this;
    }

    public IndexedField createIndexedField(IndexModels models, String prefix) {
        IndexedField field = null;

        if (indexed) {
            // Set up the lucene field.
            // Tags are never stored in the document, they are serialised in the
            // json object.
            //
            IndexedFieldType fieldType = models.createFieldTypeForJavaType(String.class);
            field = new IndexedField();
            field.setName(prefix == null ? name : prefix.concat("_").concat(name));
            field.setType(fieldType);
            field.setFilterable(filterable);
            field.setMultiValue(multiValue);
            field.setDocValues(docValues);
            field.setOmitNorms(false);
            field.setStored(false);
            field.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            field.setAnalyzer(models.getAnalyzer(analyzer));

            // If the field is an id, do not tokenize. ie the String value is
            // indexed as a single token
            //
            if (identifier) {
                field.setOmitNorms(true);
                field.setIndexOptions(IndexOptions.DOCS);
            }
        }

        return field;
    }

    public String toString() {
        return Msgs.format("{}:{}", name, tagId);
    }

    /**
     * @return the tagId
     */
    public int getTagId() {
        return tagId;
    }

    /**
     * @param tagId
     *            the tagId to set
     */
    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public FieldKey getAudioTaggerKey() {
        return audioTaggerKey;
    }

    public boolean isIdentifier() {
        return identifier;
    }

    public boolean addToContainer() {
        return addToContainer;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public String getVariousLabel() {
        return "Various " + pluralLabel;
    }
}
