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
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.lucene.index.IndexOptions;
import org.jaudiotagger.tag.FieldKey;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.beans.BeanUtils;
import com.github.am0e.commons.beans.FieldInfo;
import com.github.am0e.jdi.BeanContainer;
import com.github.am0e.jdi.annotn.Global;
import com.github.am0e.jdi.interfaces.Startable;

import suonos.lucene.DynamicIndexedFields;
import suonos.lucene.LuceneIndex;
import suonos.lucene.ModelType;
import suonos.lucene.fields.IndexedField;

@Singleton
@Global
public class MediaTags implements Startable {
    public final Map<String, MediaTag> tagsMap = AntLib.newHashMap();
    public final List<MediaTag> tags = new ArrayList<>();

    /**
     * Note the Lucene field is never built for the TITLE tag because the Title
     * field is always stored as a field of the Java class. Eg MusicAlbum#title.
     * Therefore the Lucene field is built from the annotation of the title
     * field in the Java class!!
     */
    public static final MediaTag TITLE = MediaTag.Builder().name("title").key(FieldKey.TITLE).prefixed().build();

    /**
     * Special field. This is a global field that can apply to any kind of
     * document. It is used to supply the results to the UI general search
     * field. For albums, it is composed of many fields, eg:
     * "{artists} {genres} {composers} albumTitle"
     */
    public static final MediaTag SEARCH_TERMS = MediaTag.Builder().name("searchterms").analyzer("default").indexed()
            .build();

    // "Beaker Muppet" Indexed as:
    // track_artists "beaker", "muppet" tokenised, indexed used for searching
    // track_artists_s "beak" indexed, docvalues first 4 characters, used for
    // sorting by letter.
    // track_artists_f "beak" indexed first 4 characters, used for filtering by
    // letter "b"

    public static final MediaTag ARTISTS = MediaTag.Builder().name("artists").key(FieldKey.ARTIST).id(1001)
            .valueClass(Artist.class).addToParent().filterable().indexed().docValues().multiValue().analyzer("default")
            .prefixed().pluralLabel("Artists").build();

    public static final MediaTag COMPOSERS = MediaTag.Builder().name("composers").key(FieldKey.COMPOSER).id(1002)
            .valueClass(Composer.class).addToParent().filterable().indexed().docValues().multiValue()
            .analyzer("default").prefixed().pluralLabel("Composers").build();

    public static final MediaTag GENRES = MediaTag.Builder().name("genres").key(FieldKey.GENRE).id(1004).addToParent()
            .identifier().filterable().indexed().docValues().multiValue().analyzer("default").prefixed()
            .pluralLabel("Genres").build();

    public static final MediaTag YEAR = MediaTag.Builder().name("year").key(FieldKey.YEAR).id(1005).addToParent()
            .identifier().indexed().docValues().multiValue().analyzer("numeric").prefixed().pluralLabel("Years")
            .build();

    public static final MediaTag TRACKNUMBER = MediaTag.Builder().name("trackNumber").key(FieldKey.TRACK)
            .analyzer("numeric").identifier().prefixed().build();

    public static final MediaTag CONDUCTORS = MediaTag.Builder().name("conductors").key(FieldKey.CONDUCTOR).id(1007)
            .addToParent().filterable().indexed().docValues().multiValue().analyzer("default").pluralLabel("Conductors")
            .prefixed().build();

    public static final MediaTag ALBUM_ARTISTS = MediaTag.Builder().name("albumArtists").key(FieldKey.ALBUM_ARTIST)
            .id(1008).addToParent().filterable().indexed().docValues().multiValue().analyzer("default")
            .pluralLabel("Album Artists").prefixed().build();

    public static final MediaTag ARRANGERS = MediaTag.Builder().name("arrangers").key(FieldKey.ARRANGER).id(1009)
            .addToParent().filterable().indexed().docValues().multiValue().analyzer("default").pluralLabel("Arrangers")
            .prefixed().build();

    public static final MediaTag COMMENT = MediaTag.Builder().name("comment").key(FieldKey.COMMENT).addToParent()
            .prefixed().build();

    public static final MediaTag COUNTRY = MediaTag.Builder().name("country").key(FieldKey.COUNTRY).id(1011).indexed()
            .addToParent().multiValue().analyzer("default").prefixed().build();

    public MediaTags(LuceneIndex index) {
        add(ARTISTS);
        add(COMPOSERS);
        add(TITLE);
        add(GENRES);
        add(YEAR);
        add(TRACKNUMBER);
        add(CONDUCTORS);
        add(ALBUM_ARTISTS);
        add(ARRANGERS);
        add(COMMENT);
        add(COUNTRY);
        add(SEARCH_TERMS);

        // Create the lucene fields.
        // We have to do this per model type because the lucene fields are
        // specific to each model.
        // Eg genre will have 3 lucene fields - "album_genre", "movie_genre" and
        // "track_genre".
        // We do this so that we can easily get the unique terms for each field.
        // Eg: All terms for "album_genre". If we were to name the field
        // "genre", we would not be able to get
        // the unique genres for all albums, because the term set would contain
        // the terms associated with movies and tracks.
        //
        for (ModelType modelType : index.models().getModelTypes()) {
            for (FieldInfo dynamicFields : modelType.getDynamicFields()) {
                if (DynamicIndexedFields.class.isAssignableFrom(dynamicFields.getActualType())) {
                    addIndexFields(index, modelType.getAbbrevName());
                }
            }
        }

        // Kludge. Add the global searchterms field and enable POSITIONS+OFFSETS
        // for full text search, ie phrase queries.
        // Eg match "Well Clavier" on "Well Tempered Clavier"
        //
        IndexedField fld = SEARCH_TERMS.createIndexedField(index.models(), null);
        fld.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        index.models().addField(fld);
    }

    private void addIndexFields(LuceneIndex index, String prefix) {
        for (MediaTag it : tags) {
            if (it.prefixed && it.indexed) {
                index.models().addField(it.createIndexedField(index.models(), prefix));
            }
        }
    }

    private MediaTags add(MediaTag mediaTag) {
        tagsMap.put(mediaTag.getName(), mediaTag);
        tags.add(mediaTag);

        return this;
    }

    public MediaTag getMediaTag(String name) {
        return tagsMap.get(name);
    }

    public MediaTag getMediaTag(int id) {
        for (int i = 0; i != tags.size(); i++) {
            if (tags.get(i).tagId == id)
                return tags.get(i);
        }
        return null;
    }

    public MediaTag getMediaTagForAudioTaggerKey(String key) {
        return tagsMap.get(key);
    }

    public List<MediaTag> getMediaTags() {
        return tags;
    }

    public MediaTagValue createTagValue(MediaTag tag, String value) {
        MediaTagValue val;

        if (tag.valueCtor == null) {
            val = new MediaTagValue();
        } else {
            val = BeanUtils.newInstance(tag.valueCtor);
        }

        val.setValue(value);
        val.setTag(tag);

        return val;
    }

    @Override
    public void start(BeanContainer container) throws Exception {
    }

    @Override
    public void stop(BeanContainer container) throws Exception {
    }
}
