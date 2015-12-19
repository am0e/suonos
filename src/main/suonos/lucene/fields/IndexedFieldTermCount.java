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

/**
 * The field value and it's computed counter for a search query. Eg
 * "genres:classical(10)" Eg "artists:The Muppets(10)"
 */
public final class IndexedFieldTermCount extends IndexedFieldTerm {
    int docFreq;

    public IndexedFieldTermCount(IndexedField indexedField, String term, String termQuery) {
        super(indexedField, term, termQuery);
    }

    public final String toString() {
        return indexedField + ":" + term + ":" + docFreq;
    }

    public final int docFreq() {
        return docFreq;
    }
}
