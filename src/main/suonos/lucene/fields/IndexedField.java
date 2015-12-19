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
package suonos.lucene.fields;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;

import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.commons.utils.Validate;

import suonos.lucene.SharedLuceneReader;

public class IndexedField {

    /**
     * The Lucene Field Type.
     */
    private IndexedFieldType fieldType;
    private boolean fieldTypeLocal;
    private Class<?> javaType;

    /**
     * The field name stored in the lucene document.
     */
    private String name;

    /**
     * Is the field a multivalue field. Ie a document can have a set of values.
     * Eg an album can have multiple genres and multiple artists.
     *
     */
    private boolean multiValue;

    private boolean prefixed;

    private boolean filterable;

    private boolean docValues;

    private boolean frozen;

    private boolean indexed;

    private IndexedFieldType copyFieldType() {
        if (!fieldTypeLocal) {
            this.fieldType = new IndexedFieldType(fieldType);
            this.fieldTypeLocal = true;
        }
        return fieldType;
    }

    public IndexedField() {
    }

    public IndexedField(IndexedField field) {
        this.fieldType = field.fieldType;
        this.javaType = field.javaType;
        this.name = field.name;
        this.multiValue = field.multiValue;
        this.prefixed = field.prefixed;
        this.filterable = field.filterable;
        this.docValues = field.docValues;
        this.indexed = field.indexed;
    }

    public void setName(String fieldName) {
        this.name = fieldName;
    }

    public void setType(IndexedFieldType type) {
        this.fieldType = type;
        this.javaType = type.javaType();
    }

    public String toString() {
        return Msgs.format("{}:{}", name, fieldType);
    }

    public Iterator<Term> getTerms(SharedLuceneReader is) {
        return null;
    }

    public String getName() {
        return name;
    }

    public IndexedField getDependentFacet() {
        return null;
    }

    public NumericType getNumericType() {
        return fieldType.numericType();
    }

    public float getTermBoost(Term term, int termPos) {
        return 0.0f;
    }

    public FieldType getLuceneFieldType() {
        return fieldType;
    }

    public Field createDocValueField(String fieldName, Object value) {
        if (javaType == String.class) {
            if (multiValue) {
                return new SortedSetDocValuesField(fieldName, new BytesRef(value.toString()));
            } else {
                return new SortedDocValuesField(fieldName, new BytesRef(value.toString()));
            }
        }
        if (javaType == Long.class) {
            if (multiValue == false) {
                return new SortedNumericDocValuesField(fieldName, (Long) value);
            }
        }
        if (javaType == Date.class) {
            if (multiValue == false) {
                return new SortedNumericDocValuesField(fieldName, ((Date) value).getTime());
            }
        }

        throw Validate.notAllowed("");
    }

    public Field createField(Object value) {

        if (docValues && !indexed) {
            return createDocValueField(name, value);

        } else {
            if (javaType == String.class)
                return new Field(name, (String) value, fieldType);

            if (javaType == Integer.class)
                return new IntField(name, (Integer) value, fieldType);

            if (javaType == Long.class)
                return new LongField(name, (Long) value, fieldType);

            if (javaType == Double.class)
                return new DoubleField(name, (Double) value, fieldType);

            if (javaType == Float.class)
                return new FloatField(name, (Float) value, fieldType);

            if (javaType == Boolean.class)
                return new Field(name, getBoolValue(value), fieldType);

            if (javaType == Date.class)
                return new LongField(name, ((Date) value).getTime(), fieldType);

            if (javaType == Byte[].class)
                return new Field(name, (byte[]) value, fieldType);
        }

        throw Validate.notAllowed("");
    }

    public void setValue(Field field, Object value) {
        if (javaType == String.class) {
            field.setStringValue((String) value);

        } else if (javaType == Double.class) {
            field.setDoubleValue((Double) value);

        } else if (javaType == Float.class) {
            field.setFloatValue((Float) value);

        } else if (javaType == Integer.class) {
            field.setIntValue((Integer) value);

        } else if (javaType == Long.class) {
            field.setLongValue((Long) value);

        } else if (javaType == Boolean.class) {
            field.setStringValue(getBoolValue(value));

        } else if (javaType == Date.class) {
            field.setLongValue(((Date) value).getTime());

        } else if (javaType == Byte[].class) {
            field.setBytesValue((byte[]) value);
        }

        throw Validate.notAllowed("");
    }

    private String getBoolValue(Object value) {
        if (((Boolean) value).booleanValue()) {
            return "1";
        } else {
            return "0";
        }
    }

    public Term createTerm(String termText) {
        return new Term(name, termText);
    }

    public Query createQuery(String termText) {
        return new TermQuery(createTerm(termText));
    }

    public Type getSortFieldType() {
        if (javaType == Integer.class)
            return Type.INT;
        if (javaType == Long.class || javaType == Date.class)
            return Type.LONG;
        if (javaType == Double.class)
            return Type.DOUBLE;
        if (javaType == Float.class)
            return Type.FLOAT;

        return Type.STRING;
    }

    public Class<?> getType() {
        return javaType;
    }

    /**
     * @return the multiValue
     */
    public boolean isMultiValue() {
        return multiValue;
    }

    /**
     * @param multiValue
     *            the multiValue to set
     */
    public void setMultiValue(boolean multiValue) {
        getWrite().multiValue = multiValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof IndexedField == false) {
            return false;
        }

        IndexedField other = (IndexedField) obj;

        return this.multiValue == other.multiValue && this.filterable == other.filterable
                && this.prefixed == other.prefixed && this.javaType == other.javaType
                && Objects.equals(this.name, other.name) && Objects.equals(this.fieldType, other.fieldType);
    }

    public void setPrefixed(boolean prefixed) {
        getWrite().prefixed = prefixed;
    }

    public boolean isPrefixed() {
        return prefixed;
    }

    public void setFilterable(boolean filterable) {
        getWrite().filterable = filterable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    /**
     * @return the docValues
     */
    public boolean isDocValues() {
        return docValues;
    }

    /**
     * @param docValues
     *            the docValues to set
     */
    public void setDocValues(boolean docValues) {
        getWrite().docValues = docValues;
    }

    /**
     * @param frozen
     *            the frozen to set
     */
    public void freeze() {
        this.frozen = true;

        if (this.fieldTypeLocal)
            this.fieldType.freeze();

        this.indexed = (fieldType.indexOptions() == IndexOptions.NONE ? false : true);
    }

    private IndexedField getWrite() {
        if (frozen) {
            Validate.notAllowed("Object is frozen");
        }
        return this;
    }

    /**
     * @return the indexed
     */
    public boolean isIndexed() {
        return indexed;
    }

    public Object getDisplayValue(String term) {
        return term;
    }

    /**
     * @return the fieldType
     */
    public IndexedFieldType fieldType() {
        return fieldType;
    }

    /**
     * @return the stored
     */
    public boolean stored() {
        return fieldType.stored();
    }

    /**
     * @param stored
     *            the stored to set
     */
    public void setStored(boolean stored) {
        copyFieldType().setStored(stored);
    }

    public void setOmitNorms(boolean omitNorms) {
        copyFieldType().setOmitNorms(omitNorms);
    }

    public void setIndexOptions(IndexOptions opts) {
        copyFieldType().setIndexOptions(opts);
    }

    public void setAnalyzer(Analyzer analyzer) {
        copyFieldType().setAnalyzer(analyzer);
    }

}
