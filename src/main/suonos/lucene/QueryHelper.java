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
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;

import com.github.am0e.commons.msgs.Msgs;

import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import suonos.lucene.fields.IndexedField;
import suonos.models.StoreObject;
import suonos.services.ServiceException;

/**
 * Query helper for building and running queries. Uses a fluent api rather than
 * overloaded constructors with many parameters.
 * 
 * @author anthony
 *
 */
public class QueryHelper {
    private Statement stmt;
    private Query query;
    private Sort sort;
    private ScoreDoc afterDoc;
    private Class<?> type;
    private int max = 50;

    public QueryHelper(Statement stmt) {
        this.stmt = stmt;
    }

    public Query createQuery(String field, String val) {
        Term term = new Term(field, val);
        return new TermQuery(term);
    }

    public Query createQuery(String field, List<String> values, Occur occur) {

        // Safety check.
        //
        if (values.size() >= BooleanQuery.getMaxClauseCount()) {
            throw new IllegalArgumentException();
        }

        BooleanQuery.Builder bldr = new BooleanQuery.Builder();
        for (String id : values) {
            bldr.add(createQuery(field, id), occur);
        }
        return bldr.build();
    }

    public Query createFmtQuery(String query, Object... args) {
        return createQuery(Msgs.format(query, args));
    }

    public Query createQuery(String query) {
        query = query.trim();

        if (query.isEmpty())
            return null;

        try {
            return stmt.standardQueryParser().parse(query, "title");

        } catch (QueryNodeException e) {
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    public Query createTypeQuery(Class<?> type) {
        return createQuery(stmt.context.type$_field().name(), type.getSimpleName());
    }

    public QueryHelper setQuery(Query query) {
        if (query != null) {
            this.query = query;
        }
        return this;
    }

    public QueryHelper setQuery(String field, String val) {
        return setQuery(new TermQuery(new Term(field, val)));
    }

    public QueryHelper setQuery(String query) {
        if (query != null) {
            setQuery(createQuery(query));
        }
        return this;
    }

    public QueryHelper setType(Class<?> type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    public QueryHelper setSort(Sort sort) {
        if (sort != null) {
            this.sort = sort;
        }
        return this;
    }

    public QueryHelper setSort(String sort) {
        if (sort != null) {
            setSort(createSort(sort));
        }
        return this;
    }

    /**
     * Parses the sort string and builds a lucene Sort object.
     * 
     * @param sortExpr
     *            The sort string.
     * @return
     */
    public Sort createSort(String sortExpr) {

        if (sortExpr != null && !sortExpr.isEmpty()) {

            boolean reverse = false;

            // Loo for "-price"
            //
            if (sortExpr.charAt(0) == '-') {
                sortExpr = sortExpr.substring(1);
                reverse = true;
            }

            // If the field is sort by score (relevance), return null (default
            // is sort by relevance).
            //
            if (sortExpr.equals("relevance")) {
                return null;
            }

            // Get the field information.
            //
            IndexedField field = stmt.luceneIndex.models().indexedField(sortExpr);

            Type sortFieldType = field.getSortFieldType();

            if (sortFieldType != null && sortFieldType != SortField.Type.SCORE) {
                return new Sort(new SortField(field.getName(), sortFieldType, reverse));
            }

            return new Sort(new SortField(sortExpr, SortField.Type.STRING, reverse));
        }
        return null;
    }

    public QueryHelper setNextToken(String nextToken) {
        if (nextToken != null) {
            afterDoc = IndexUtils.parseLastScoreToken(nextToken);
        }
        return this;
    }

    public <T extends StoreObject> QueryResults<T> query() throws IOException {
        Query query = buildQuery();
        TopDocs docs;

        if (afterDoc != null) {
            if (sort == null)
                docs = stmt.indexSearcher().searchAfter(afterDoc, query, max);
            else
                docs = stmt.indexSearcher().searchAfter(afterDoc, query, max, sort);

        } else {
            if (sort == null)
                docs = stmt.indexSearcher().search(query, max);
            else
                docs = stmt.indexSearcher().search(query, max, sort);
        }

        return new QueryResults<T>(stmt, query, docs);
    }

    public Query buildQuery() {
        if (type != null) {
            Query typeQuery = createTypeQuery(type);

            if (query == null) {
                this.query = typeQuery;
            } else {
                BooleanQuery and = new BooleanQuery();
                and.add(typeQuery, Occur.MUST);
                and.add(query, Occur.MUST);
                this.query = and;
            }
        }
        return query;
    }

    public QueryHelper setMax(int max) {
        if (max == 0 || max == -1 || max > QueryResults.SEARCH_MAX)
            this.max = QueryResults.SEARCH_MAX;
        else
            this.max = max;

        return this;
    }
}
