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

package suonos.controllers;

import java.io.IOException;

import com.github.am0e.commons.json.JsonWriter;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.lucene.fields.IndexedFieldCounts;
import suonos.lucene.fields.IndexedFieldTermCount;

/**
 * Facets controller.
 * 
 * GET /api/facets?id=album_artists_l All artists as a collection of Alphabetic
 * characters. Eg "A", "B", "C"
 * 
 * GET /api/facets?id=album_artists&filter=B All artists beginning with the
 * letter "B"
 * 
 * @author anthony
 *
 */
public final class FacetsController extends Controller {

    public FacetsController(ActionCtx ctx) {
        super(ctx);
    }

    /**
     * GET /api/facets?fields=album_genres&filter=B
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object index() throws IOException {
        IndexedFieldCounts counters = querySvcs().queryFacets();

        return ctx.render("application/json", (resp) -> {
            JsonWriter w = new JsonWriter(resp.getWriter());
            w.startObject();
            w.startArray("fieldCounts");
            int terml_seq = 0;
            char terml_seq_ch = 0;
            for (String fieldName : counters.getFieldNames()) {
                IndexedFieldTermCount[] fieldTermCounts = counters.getFieldTermCounts(fieldName);

                w.startObject();
                w.write("fieldName", fieldName);
                w.startArray("terms");
                for (IndexedFieldTermCount it : fieldTermCounts) {
                    String term = it.getTerm();
                    String terml = term.substring(0, 1).toUpperCase();

                    w.startObject();
                    w.write("docFreq", it.docFreq());
                    w.write("term", term);
                    w.write("terml", terml);
                    w.write("queryTerm", it.getQueryTerm());

                    // Gen a seq id based on "term" for alternating the
                    // background colour in the UI.
                    //
                    if (terml.charAt(0) != terml_seq_ch) {
                        terml_seq_ch = terml.charAt(0);
                        terml_seq += 1;
                        w.write("newseq", '1');
                    }

                    w.write("seq", terml_seq);
                    w.endObject();
                }
                w.endArray();
                w.endObject();
            }
            w.endArray();
            w.endObject();
            resp.getWriter().flush();
        });
    }
}
