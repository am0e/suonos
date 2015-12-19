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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.utils.Validate;

import suonos.models.StoreObject;
import suonos.services.ServiceException;

public class QueryResults<T extends StoreObject> implements Iterable<T> {
    private Statement stmt;
    private Query query;
    private TopDocs docs;
    private Map<String, T> byId;

    private class QueryResultsIterator implements Iterator<T> {
        int pos = 0;

        public boolean hasNext() {
            return pos < docs.scoreDocs.length;
        }

        @Override
        public T next() {
            if (pos >= docs.scoreDocs.length)
                return null;

            try {
                T obj = get(pos);
                pos++;
                return obj;

            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

    }

    public final static int SEARCH_CNT = 4;
    public final static int SEARCH_MAX = 500;

    public QueryResults(Statement stmt, Query query, TopDocs docs) {
        this.docs = docs;
        this.stmt = stmt;
        this.query = query;
    }

    public int totalHits() {
        return docs.totalHits;
    }

    public T first() throws IOException {
        if (docs.scoreDocs.length > 0) {
            return get(0);
        } else {
            return null;
        }
    }

    public ScoreDoc lastScore() throws IOException {
        if (docs.scoreDocs.length > 0 && docs.scoreDocs.length < docs.totalHits) {
            return docs.scoreDocs[docs.scoreDocs.length - 1];
        } else {
            return null;
        }
    }

    public String lastScoreToken() throws IOException {
        ScoreDoc lastScore = lastScore();
        if (lastScore != null) {
            return IndexUtils.buildLastScoreToken(lastScore);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T get(int pos) throws IOException {
        if (pos < docs.scoreDocs.length) {
            return (T) stmt.getDoc(docs.scoreDocs[pos].doc);
        }
        throw Validate.illegalArgument("pos");
    }

    @SuppressWarnings("unchecked")
    public List<T> all() throws IOException {
        return (List<T>) getObjects();
    }

    private List<?> getObjects() throws IOException {

        if (docs.scoreDocs.length <= 0) {
            return Collections.emptyList();
        }

        List<Object> list = AntLib.newList(docs.totalHits);
        int totalHits = docs.totalHits;

        while (totalHits != 0) {
            for (ScoreDoc it : docs.scoreDocs) {
                list.add(stmt.getDoc(it.doc));
                totalHits--;
            }

            if (totalHits > 0 && docs.scoreDocs.length == SEARCH_CNT) {
                docs = stmt.indexSearcher().searchAfter(docs.scoreDocs[SEARCH_CNT - 1], query, SEARCH_CNT);

            } else {
                break;
            }
        }

        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return new QueryResultsIterator();
    }

    public T get(String id) throws IOException {
        if (byId == null) {
            // Build the hashmap on the first get().
            //
            byId = AntLib.newHashMap();

            for (T obj : this) {
                byId.put(obj.getId(), obj);
            }
        }

        return byId.get(id);
    }
}
