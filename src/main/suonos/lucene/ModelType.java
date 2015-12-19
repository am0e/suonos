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
package suonos.lucene;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.beans.BeanInfo;
import com.github.am0e.commons.beans.BeanUtils;
import com.github.am0e.commons.beans.FieldInfo;
import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.commons.utils.Validate;

import suonos.app.utils.TagUtils;
import suonos.lucene.fields.IndexedField;
import suonos.lucene.fields.IndexedFieldType;
import suonos.models.StoreObject;
import suonos.models.annotations.IndexDoc;
import suonos.models.annotations.IndexField;

public class ModelType {
    final String modelName;

    /**
     * Model Class.
     */
    final Class<?> modelClass;

    /**
     * Abbreviated model name. Eg "track", "album"
     */
    String abbrevName;

    /**
     * Map of {@link ModelField} objects, indexed by the java field name.
     */
    private Map<String, ModelField> fieldsMap;

    /**
     * List of {@link ModelField} objects.
     */
    private ModelField[] modelFields;

    /**
     * List of fields that implement the {@link DynamicIndexedFields} interface.
     * These objects index themselves at runtime. The fields vary from document
     * to document.
     */
    private FieldInfo[] dynamicFields;

    /**
     * Reflection field, link between reflection and lucene field.
     * 
     * @author anthony
     */
    public final static class ModelField {
        /**
         * Reflection field.
         */
        public final FieldInfo field;

        /**
         * Lucene facet field definition.
         */
        public final IndexedField indexedField;

        public ModelField(FieldInfo field, IndexedField indexedField) {
            this.field = field;
            this.indexedField = indexedField;
        }
    }

    public ModelType(IndexModels models, Class<?> modelClass) {
        this.modelName = modelClass.getSimpleName().intern();
        this.modelClass = modelClass;
        this.fieldsMap = AntLib.newHashMap();

        IndexDoc indexDoc = modelClass.getAnnotation(IndexDoc.class);

        if (indexDoc == null) {
            throw Validate.notAllowed(Msgs.format("Missing annotation {} on {}", IndexDoc.class, modelClass));
        }

        abbrevName = indexDoc.abbrev().intern();

        initFields(models);
    }

    /**
     * @return the modelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * @return the modelClass
     */
    public Class<?> getModelClass() {
        return modelClass;
    }

    public ModelField getModelField(String fieldName) {
        return fieldsMap.get(fieldName);
    }

    private void initFields(IndexModels models) {
        List<FieldInfo> dynamicFieldObjects = AntLib.newList();
        List<ModelField> indexedFields = AntLib.newList();

        for (Class<?> it = modelClass; it != Object.class; it = it.getSuperclass()) {
            BeanInfo beanInfo = BeanInfo.forClass(it);

            // Process each acccessible field in this class.
            //
            for (FieldInfo mf : beanInfo.getDeclaredPublicFields()) {
                // Ignore if not readable or is transient.
                //
                if (mf.isReadable() == false || mf.isTransient()) {
                    continue;
                }

                // Get the index field.
                //
                IndexField ndxFld = mf.getAnnotation(suonos.models.annotations.IndexField.class);

                if (ndxFld == null) {
                    continue;
                }

                // See if the field is compatible with DynamicIndexableFields
                //
                if (DynamicIndexedFields.class.isAssignableFrom(mf.getActualType())) {
                    dynamicFieldObjects.add(mf);
                    continue;
                }

                if (ndxFld.indexed() == false && ndxFld.stored() == false) {
                    continue;
                }

                // Get Non primitive type.
                // Ie both long and Long map to Long.class
                //
                Class<?> javaType = BeanUtils.getNonPrimitiveClass(mf.getType());
                IndexedFieldType fieldType = models.createFieldTypeForJavaType(javaType);

                String fieldName = mf.getName();

                if (ndxFld.prefixed()) {
                    fieldName = abbrevName.concat("_").concat(fieldName);
                }

                IndexedField fld = new IndexedField();
                fld.setName(fieldName);
                fld.setType(fieldType);
                fld.setMultiValue(ndxFld.multiValue());
                fld.setPrefixed(ndxFld.prefixed());
                fld.setDocValues(ndxFld.docValues());
                fld.setFilterable(ndxFld.filterable());
                fld.setStored(ndxFld.stored());
                fld.setOmitNorms(ndxFld.omitNorms());
                fld.setAnalyzer(models.getAnalyzer(ndxFld.analyzer()));

                if (ndxFld.indexed() == false) {
                    fld.setIndexOptions(IndexOptions.NONE);
                } else {
                    fld.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
                }

                IndexedField luceneField = models.addField(fld);

                ModelField modelField = new ModelField(mf, luceneField);
                fieldsMap.put(mf.getName(), modelField);
                indexedFields.add(modelField);
            }
        }

        this.modelFields = indexedFields.toArray(new ModelField[0]);
        this.dynamicFields = dynamicFieldObjects.toArray(new FieldInfo[0]);
    }

    public void saveToLuceneDoc(final StatementContext context, final StoreObject object, final Document doc) {

        final IndexModels models = context.models();

        for (ModelField it : modelFields) {
            // Get the value from the bean.
            //
            addFieldToDoc(doc, models, it.indexedField, it.field.callGetter(object));
        }

        // Setup a context for the dynamic indexable fields.
        //
        DynamicIndexedFieldCtx ctx = (fieldName, value) -> {
            String name;
            if (fieldName.startsWith("_")) {
                name = fieldName.substring(1);
            } else {
                name = abbrevName.concat("_").concat(fieldName);
            }
            IndexedField fld = models.getIndexedField(name);
            if (fld != null) {
                addFieldToDoc(doc, models, fld, value);
            }
        };

        // Now serialize the dynamic fields.
        //
        for (FieldInfo it : dynamicFields) {
            // Using reflection get the object. May be null!
            //
            DynamicIndexedFields iface = (DynamicIndexedFields) it.callGetter(object);
            if (iface != null) {
                iface.indexFields(ctx);
            }
        }

        if (object instanceof DynamicIndexedFields) {
            ((DynamicIndexedFields) object).indexFields(ctx);
        }
    }

    public String getAbbrevName() {
        return abbrevName;
    }

    public FieldInfo[] getDynamicFields() {
        return dynamicFields;
    }

    private void addFieldToDoc(Document doc, IndexModels models, IndexedField indexedField, Object val) {

        if (val == null)
            return;

        Field fld = indexedField.createField(val);
        addToDoc(doc, fld);

        // Somewhat of a kludge!!!!
        // The default token analyser used is SimpleAnalyzer. This will apply
        // lowercase to the tokens. In order to keep
        // consistency, we also have to apply a lowercase to untokenised values.
        //
        Object filter_val = getFilterValue(val);

        // If indexed and docValues, we have already created the normal indexed
        // field.
        // We now have to create a DocValue field with the same name. This will
        // be used for sorting and faceting.
        // The DocValue field is not used during queries:
        // Field fieldName field value field type flags terms
        // StringField muppetName "Beaker Muppet" indexed tokenised "beaker"
        // "muppet" (multiple terms)
        // SortedSetDocValuesField muppetName "Beaker Muppet" "Beaker Muppet"
        // (stored untokenised)
        //
        // In lucene it's perfectly legit to have a docfield and normal indexed
        // field with the same name as they are
        // used for different purposes.
        //
        if (indexedField.isIndexed() && indexedField.isDocValues()) {
            fld = indexedField.createDocValueField(indexedField.getName(), val);
            addToDoc(doc, fld);

            // Add the field value as untokenised. Used for querying all
            // documents containing a facet value.
            // Eg: All albums for genre "Classical"
            //
            IndexedField ndxField = models.getIndexedField(indexedField.getName().concat("_u"));
            fld = ndxField.createField(filter_val);
            addToDoc(doc, fld);
        }

        if (indexedField.isFilterable()) {
            // Create the following fields:
            // Name Type Use
            // field_s DocValuesField Sorting. Eg: "/api/albums?s=title_s" Sort
            // albums by abbrev title (first 4 characters)
            // field_a DocValuesField Faceting. Eg:
            // "/api/facets?fields=album_artists_a" returns all first letters
            // for artists.
            // field_a Normal Field Single character Queries. Eg
            // "/api/albums?q=title_a:B" All titles starting with "B"
            //
            String filterValue = (String) filter_val;
            filterValue = StringUtils.left(filterValue, 4);

            if (filterValue != null) {
                // field_s (DocValueField) max len is 4 characters.
                //
                String filterFieldName = indexedField.getName().concat("_s");

                // For faceting/sorting only. No need to create a normal field
                // for this!
                // Eg: GET "/api/albums?s=title_s"
                // Sorting on tokens that are only 4 letters in length is more
                // efficient than sorting on the full length.
                //
                fld = indexedField.createDocValueField(filterFieldName, filterValue);
                addToDoc(doc, fld);

                // field_a (DocValue) and (Field)
                // For Alphabet browsing.
                // Eg: A / B / C / D / E / F
                //
                filterFieldName = indexedField.getName().concat("_a");
                filterValue = filterValue.substring(0, 1);

                // Create DocField for faceting
                //
                fld = indexedField.createDocValueField(filterFieldName, filterValue);
                addToDoc(doc, fld);

                // Get the IndexedField - field_a
                //
                IndexedField ndxField = models.getIndexedField(filterFieldName);

                // Create normal field for simple queries by first character.
                // /api/albums?q=title_a=B (find all albums beginning with the
                // letter "B")
                //
                fld = ndxField.createField(filterValue);
                addToDoc(doc, fld);

                // Should we add a new field - artist_e (untokenised token) for
                // searching albums by exact author.
            }
        }
    }

    private void addToDoc(Document doc, Field f) {
        Statement.log.debug("Add Field {}", f);
        doc.add(f);
    }

    private Object getFilterValue(Object value) {

        if (value == null || value.getClass() != String.class)
            return value;

        String s = (String) value;

        // TODO
        // Warning: a change here: review the code in QuerySvcs::jsonQuery()
        //
        s = TagUtils.convertStringToId(s);

        if (s.isEmpty())
            return null;

        return s;
    }

    private Field getDocValueField(IndexedField indexedField, String suffix, Object value) {
        String name = indexedField.getName().concat(suffix);
        return indexedField.createDocValueField(name, value);
    }
}
