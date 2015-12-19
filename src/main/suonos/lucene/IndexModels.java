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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.index.IndexOptions;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.commons.utils.Validate;

import suonos.lucene.fields.IndexedField;
import suonos.lucene.fields.IndexedFieldType;
import suonos.models.annotations.Threaded;

/**
 * Manages the fields for the lucene documents. The field types are constructed
 * at runtime when a model is first accessed. The field types are built from the
 * annotations in the model class.
 * 
 * Note this is part of MediaStore and must be thread safe.
 * 
 * @author anthony
 */
@Threaded
public class IndexModels {

    /**
     * The models, indexed by the model name.
     */
    final Map<String, ModelType> models;

    /**
     * The models indexed by the associated java class.
     */
    final Map<Class<?>, ModelType> modelTypes;

    /**
     * Map of {@link IndexedField} objects, indexed by the field name.
     */
    final Map<String, IndexedField> indexedFields;

    /**
     * Map of {@link IndexedField} objects, indexed by the field name.
     */
    final Map<String, IndexedFieldType> fieldTypes;

    /**
     * The analysers.
     */
    final Map<String, Analyzer> analysers;

    private Analyzer defaultAnaylser = new StandardAnalyzer();

    public IndexModels() {
        modelTypes = new IdentityHashMap<>();
        models = new IdentityHashMap<>();
        indexedFields = AntLib.newHashMap();
        fieldTypes = AntLib.newHashMap();
        analysers = AntLib.newHashMap();
    }

    public Collection<ModelType> getModelTypes() {
        return modelTypes.values();
    }

    public ModelType getModelType(Class<?> type) {
        return modelTypes.get(type);
    }

    public ModelType getModelType(String modelName) {
        return models.get(modelName.intern());
    }

    public void load() throws IOException {
        IndexModelCfgRdr.load(this, Paths.get("./conf/models.xml"));
    }

    public IndexedField addField(IndexedField indexedField) {
        IndexedField cur = indexedFields.get(indexedField.getName());

        if (cur != null) {
            if (cur.equals(indexedField) == false) {
                throw Validate
                        .notAllowed(Msgs.format("Field {} defined with different arguments", indexedField.getName()));
            }
            return cur;
        }

        addField_(indexedField);

        if (indexedField.isDocValues()) {
            IndexedField field = new IndexedField(indexedField);
            field.setName(indexedField.getName() + "_u");
            field.setStored(false);
            field.setAnalyzer(getAnalyzer("keyword"));
            field.setIndexOptions(IndexOptions.DOCS);
            addField_(field);
        }

        if (indexedField.isFilterable()) {
            IndexedField field;

            // field_s field
            //
            field = new IndexedField(indexedField);
            field.setName(indexedField.getName() + "_s");
            field.setDocValues(true);
            field.setStored(false);
            field.setAnalyzer(getAnalyzer("keyword"));
            field.setIndexOptions(IndexOptions.DOCS);
            addField_(field);

            // field_a field
            //
            field = new IndexedField(indexedField);
            field.setName(indexedField.getName() + "_a");
            field.setDocValues(true);
            field.setStored(false);
            field.setAnalyzer(getAnalyzer("keyword"));
            field.setIndexOptions(IndexOptions.DOCS);
            addField_(field);

        }

        return indexedField;
    }

    private void addField_(IndexedField indexedField) {
        indexedFields.put(indexedField.getName(), indexedField);
        indexedField.freeze();
    }

    public IndexedField getIndexedField(String fieldName) {
        return indexedFields.get(fieldName);
    }

    public IndexedField indexedField(String fieldName) {
        IndexedField it = indexedFields.get(fieldName);
        if (it == null) {
            throw Validate.illegalArgument(fieldName, "Unknown field. Not declared in IndexModels.");
        }
        return it;
    }

    public void addFieldType(IndexedFieldType type) {
        this.fieldTypes.put(type.name(), type);
        type.freeze();
    }

    public IndexedFieldType getFieldType(String name) {

        IndexedFieldType res = this.fieldTypes.get(name);

        if (res == null) {
            throw Validate.illegalArgument(Msgs.format("Invalid Field Type {}", name));
        }

        return res;
    }

    public void addAnalyzer(IndexAnalyser analyzer) {
        this.analysers.put(analyzer.name(), analyzer.analyser());
    }

    public Analyzer getAnalyzer(String name) {
        if (name == null || name.isEmpty()) {
            Validate.illegalArgument("name");
        }

        Analyzer res = this.analysers.get(name);

        if (res == null) {
            throw Validate.illegalArgument(Msgs.format("Invalid analyzer name {}", name));
        }

        return res;
    }

    public IndexedFieldType createFieldTypeForJavaType(Class<?> type) {
        String key = type.getName();

        IndexedFieldType fieldType = this.fieldTypes.get(key);

        if (fieldType == null) {
            fieldType = new IndexedFieldType();
            fieldType.setAnalyzer(getAnalyzer("default"));
            fieldType.setJavaType(type);
            fieldType.setStored(true);
            fieldType.setName(key);

            if (type == Integer.class) {
                fieldType.setNumericType(NumericType.INT);
            } else if (type == Long.class) {
                fieldType.setNumericType(NumericType.LONG);
            } else if (type == Double.class) {
                fieldType.setNumericType(NumericType.DOUBLE);
            } else if (type == Float.class) {
                fieldType.setNumericType(NumericType.FLOAT);
            } else if (type == String.class) {
                fieldType.setNumericType(null);
            } else if (type == Date.class) {
                fieldType.setNumericType(NumericType.LONG);
            } else if (type == Boolean.class) {
                fieldType.setNumericType(null);
                fieldType.setOmitNorms(true);
                fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            } else {
                throw Validate.illegalArgument("type");
            }

            this.addFieldType(fieldType);
        }

        return fieldType;
    }

    /**
     * @return the defaultAnaylser
     */
    public Analyzer defaultAnaylser() {
        return defaultAnaylser;
    }

    /**
     * @param defaultAnaylser
     *            the defaultAnaylser to set
     */
    public void setDefaultAnaylser(Analyzer defaultAnaylser) {
        this.defaultAnaylser = defaultAnaylser;
    }

}
