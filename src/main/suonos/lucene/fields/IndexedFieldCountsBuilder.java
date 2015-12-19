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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import com.github.am0e.commons.AntLib;

import gnu.trove.map.hash.TIntIntHashMap;
import suonos.app.utils.TagUtils;
import suonos.lucene.IndexModels;
import suonos.lucene.Statement;

public class IndexedFieldCountsBuilder {
    Map<String, IndexedFieldTermCount[]> fieldCounts = AntLib.newHashMap();
    private IndexReader ir;
    private IndexModels models;

    public IndexedFieldCountsBuilder(Statement stmt) {
        this.ir = stmt.indexReader();
        this.models = stmt.luceneIndex().models();
    }

    public IndexedFieldCountsBuilder addField(String fieldName, String filter) throws IOException {

        final IndexedField fld = models.indexedField(fieldName);
        final Map<String, IndexedFieldTermCount> valuesMap = AntLib.newHashMap();
        final TIntIntHashMap ordCounts = new TIntIntHashMap();

        if (filter != null) {
            filter = filter.toLowerCase();
        }

        // Get count of segments.
        //
        int sz = ir.leaves().size();

        for (int i = 0; i != sz; i++) {
            // Get the segment reader.
            //
            LeafReader lr = ir.leaves().get(i).reader();

            // Doc count for field. Eg "album_genres"
            //
            lr.getDocCount(fld.getName());

            // Get all documents that have the field "album_genres"
            //
            Bits docs = lr.getDocsWithField(fld.getName());
            ordCounts.clear();

            // Enumerate the field terms.
            //
            if (fld.isDocValues()) {
                if (fld.isMultiValue()) {
                    // docvalues & multivalue is a SortedSetDocValues
                    // Per-Document values in a SortedDocValues are
                    // deduplicated, dereferenced, and sorted into a dictionary
                    // of
                    // unique values. A pointer to the dictionary value
                    // (ordinal) can be retrieved for each document.
                    // Ordinals are dense and in increasing sorted order.
                    //
                    SortedSetDocValues set = lr.getSortedSetDocValues(fld.getName());

                    if (set != null) {
                        // For all documents that have the field "album_genres":
                        //
                        for (int docId = 0; docId != docs.length(); docId++) {
                            if (docs.get(docId)) {
                                // Enumerate the set of [terms] of
                                // "album_genres" for the document represented
                                // by docId.
                                // Each ord represents the term value.
                                //
                                set.setDocument(docId);

                                // For each term bump up the frequency.
                                //
                                long ord;
                                while ((ord = set.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
                                    ordCounts.adjustOrPutValue((int) ord, 1, 1);

                                    System.out.println("term=" + set.lookupOrd(ord).utf8ToString());
                                }
                            }
                        }

                        TermsEnum te = set.termsEnum();
                        BytesRef term;

                        while ((term = te.next()) != null) {

                            int ord = (int) te.ord();

                            add(fld, valuesMap, filter, term, ordCounts.get(ord));
                        }

                    }

                } else {
                    SortedDocValues set = lr.getSortedDocValues(fld.getName());

                    if (set != null) {
                        // For all documents that have the field "album_genres":
                        //
                        for (int docId = 0; docId != docs.length(); docId++) {
                            if (docs.get(docId)) {
                                // Get the term - Classical, Rock, etc.
                                //
                                BytesRef term = set.get(docId);

                                add(fld, valuesMap, filter, term, 1);
                            }
                        }
                    }
                }
            } else {
                // Normal field, not a doc value.
                //
                Terms terms = lr.terms(fld.getName());
                TermsEnum te = terms.iterator();

                BytesRef term;
                while ((term = te.next()) != null) {
                    add(fld, valuesMap, filter, term, te.docFreq());
                }
            }

            /*
             * SORTED doc[0] = "aardvark" doc[1] = "beaver" doc[2] = "aardvark"
             * 
             * doc[0] = 0 doc[1] = 1 doc[2] = 0
             * 
             * term[0] = "aardvark" term[1] = "beaver"
             */

            // http://127.0.0.1:8080/api/facets?fields=track_title_a
            // the above should return B:(4) because titles starting with B are
            // 4!
        }

        // Get the array of term counters.
        //
        IndexedFieldTermCount[] list = valuesMap.values().toArray(new IndexedFieldTermCount[0]);

        // Sort by term.
        //
        Arrays.sort(list);

        // add to the map.
        //
        this.fieldCounts.put(fld.getName(), list);

        return this;
    }

    void add(IndexedField fld, Map<String, IndexedFieldTermCount> valuesMap, String filter, BytesRef term,
            int docFreq) {

        String termVal = term.utf8ToString();

        // Case insensitive comparison.
        //
        String termValLC = TagUtils.convertStringToId(termVal);
        if (filter != null && !termValLC.startsWith(filter)) {
            return;
        }

        IndexedFieldTermCount c = valuesMap.get(termValLC);

        if (c == null) {
            valuesMap.put(termValLC, c = new IndexedFieldTermCount(fld, termVal, termValLC));
        }

        c.docFreq += docFreq;
        System.out.println("term=" + termVal + " " + docFreq);
    }

    public IndexedFieldCounts build() {
        return new IndexedFieldCounts(this.fieldCounts);
    }
}
