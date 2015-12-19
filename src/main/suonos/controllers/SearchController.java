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
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.NIOFSDirectory;

import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.lucene.QueryHelper;
import suonos.lucene.QueryResults;
import suonos.models.StoreObject;
import suonos.services.queries.QuerySvcs;

public class SearchController extends Controller {

    public SearchController(ActionCtx ctx) {
        super(ctx);
    }

    /**
     * GET /api/search?q=album_genres:classical GET
     * /api/search?q=track_genres_u:classical&fields=type,title,albumId
     * 
     * @throws IOException
     */
    @Action
    public Object index() throws IOException {
        QuerySvcs querySvcs = querySvcs();
        QueryHelper queryHelper = lib.stmt().queryHelper();
        Query query;

        if (ctx.params().contains("kq")) {
            // Use a phrase query and a slop to allow for finding text with
            // terms near each other.
            //
            query = lib.stmt().queryBuilder().createPhraseQuery("searchterms", ctx.param("kq"), 1);

        } else {
            // Build the query.
            //
            String q = ctx.param("q");
            query = queryHelper.createQuery(q);
        }

        String s = ctx.param("s", null);
        String n = ctx.param("n", null);
        int max = ctx.params().getInteger("max", -1);

        // Query the objects.
        //
        QueryResults<StoreObject> objects = queryHelper.setQuery(query).setSort(s).setNextToken(n).setMax(max).query();

        // Return back as a json object.
        //
        return querySvcs.resultsAsJson(objects);

        /*
         * ? Need a better search: 1) Search album. 2) Search composers. 3)
         * Search artists. 4) Search tags: Show Found 4 Composers: Beethoven,
         * etc Found 4 Albums: show albums (more...) Found 9 Artists: Pink
         * Floyd, Pink Punk (more...)
         */
    }

    /**
     * GET /api/search/suggestions?q=rock
     * 
     * @throws IOException
     */
    @Action
    public Object suggestions() throws IOException {
        QueryResults<?> objects;

        String q = ctx.param("q");

        NIOFSDirectory directory = lib.luceneIndex().getLuceneDirectory();
        IndexReader rdr = DirectoryReader.open(directory);
        Dictionary dict = new LuceneDictionary(rdr, "track_title");

        // Problem is track_title is all lower case!!! ??
        //
        // Also need a composite "search" field for searching:
        // ie seach "bach", or search "schiff" should return all albums
        // containing terms bach, schiff, etc.
        // "<artists> <composers> <arrangers> <album title>"
        //
        // would we ever search for tracks:
        // "artists composers arrangers title"
        //
        // Could search for tracks and then get the album id from lucene????
        //
        List<LookupResult> results;

        if (false) {
            // http://lucene.apache.org/core/4_4_0/suggest/index.html
            AnalyzingSuggester as1 = new AnalyzingSuggester(new StandardAnalyzer());
            as1.build(dict);
            results = as1.lookup(q, false, 10);

        } else {
            // Returns suggestions.
            // Eg: for "ba" -
            // "bach", "bat", "banner"
            //
            try (AnalyzingInfixSuggester as = new AnalyzingInfixSuggester(directory, new StandardAnalyzer())) {
                as.build(dict);

                results = as.lookup(q, false, 10);
            }
        }

        // Return back as a json object.
        //
        return jsonData(results);
    }
}
