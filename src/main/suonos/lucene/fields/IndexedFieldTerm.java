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

import java.util.Objects;

import com.github.am0e.commons.msgs.Msgs;

/**
 * The facet and it's value as obtained from a search query. Eg BrandFacet:sony
 */
public class IndexedFieldTerm implements Comparable<IndexedFieldTerm> {
    /**
     * The indexed field
     */
    final IndexedField indexedField;

    /**
     * The field value.
     */
    final String term;

    /**
     * Term query value. for queries.
     */
    final String queryTerm;

    public IndexedFieldTerm(IndexedField facet, String term, String queryTerm) {
        this.indexedField = facet;
        this.term = term;
        this.queryTerm = queryTerm;
    }

    public String toString() {
        return Msgs.format("{}:{}", indexedField, term);
    }

    public final IndexedField indexedField() {
        return indexedField;
    }

    public final String getTerm() {
        return term;
    }

    public final Object getDisplayValue() {
        return indexedField.getDisplayValue(term);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof IndexedFieldTerm) {
            IndexedFieldTerm o2 = (IndexedFieldTerm) obj;
            return indexedField == o2.indexedField && Objects.equals(term, o2.term);
        }

        return false;
    }

    public String getQueryTerm() {
        return queryTerm;
    }

    @Override
    public int compareTo(IndexedFieldTerm o) {
        return term.compareTo(o.term);
    }
}
