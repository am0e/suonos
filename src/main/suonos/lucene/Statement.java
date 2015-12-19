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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;
import org.iq80.snappy.Snappy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.commons.AntLib;

import suonos.app.utils.Uids;
import suonos.lucene.fields.IndexedFieldCountsBuilder;
import suonos.models.MTime;
import suonos.models.StoreObject;
import suonos.models.StoreRef;

public class Statement implements AutoCloseable {
    private SharedLuceneWriter _writer;
    private SharedLuceneReader _reader;
    final StatementContext context;
    final LuceneIndex luceneIndex;
    private StringWriter sw;
    private JsonDeserializer deserializer;
    private JsonSerializer serializer;
    public static final Logger log = LoggerFactory.getLogger(Statement.class);

    public Statement(LuceneIndex index, StatementContext context) {
        this.luceneIndex = index;
        this.context = context;
    }

    public <T extends StoreObject> QueryResults<T> queryObject(String id) throws IOException {
        return queryHelper().setQuery("id", id).query();
    }

    public <T extends StoreObject> QueryResults<T> queryRelated(List<? extends StoreRef> objs) throws IOException {
        List<String> ids = AntLib.newList(objs.size());

        for (StoreRef it : objs) {
            if (it.getRefId() != null) {
                ids.add(it.getRefId());
            }
        }

        return queryObjects(ids);
    }

    public <T extends StoreObject> QueryResults<T> queryObjects(List<String> ids) throws IOException {
        QueryHelper qh = queryHelper();
        return qh.setQuery(qh.createQuery("id", ids, Occur.SHOULD)).query();
    }

    Object getDoc(int docId) throws CorruptIndexException, IOException {
        // Unserialize the json string.
        //
        Document doc = openSharedReader().doc(docId);

        String modelName = doc.get(context.type$_field().name());

        // Get the type.
        //
        ModelType type = context.getModelType(modelName);

        return unserializeJson(type.getModelClass(), getDocumentJson(docId));
    }

    public String getDocumentJson(int docId) throws CorruptIndexException, IOException {
        Document doc = openSharedReader().doc(docId);

        // Get the serialized field from the document.
        //
        BytesRef binVal = doc.getBinaryValue(context.obj$_field().name());

        // Uncompress the data into a string using snappy.
        //
        byte[] bytes = Snappy.uncompress(binVal.bytes, binVal.offset, binVal.length);

        // Return as string.
        //
        return new String(bytes, "UTF-8");
    }

    private String serializeToJson(StoreObject obj) {
        if (sw == null) {
            sw = new StringWriter();
        } else {
            sw.getBuffer().setLength(0);
        }

        serializer().write(sw, obj);

        String s = sw.getBuffer().toString();
        sw.getBuffer().setLength(0);

        return s;
    }

    private JsonDeserializer deserializer() {
        if (deserializer == null) {
            deserializer = new JsonDeserializer();
        }
        return deserializer;
    }

    private JsonSerializer serializer() {
        if (serializer == null) {
            serializer = new JsonSerializer();
        }
        return serializer;
    }

    public StoreObject unserializeJson(Class<?> type, String json) {
        // Unserialize.
        //
        StoreObject obj = (StoreObject) deserializer().readObject(new StringReader(json), type);
        obj.onQueried();

        return obj;
    }

    public void saveObjects(Collection<? extends StoreObject> objects) throws IOException {
        for (StoreObject it : objects) {
            saveObject(it);
        }
    }

    public void createObject(StoreObject object) throws IOException {
        object.store_reset();
        saveObject(object);
    }

    public void saveObject(StoreObject object) throws IOException {

        openSharedWriter();

        Document doc = new Document();
        Field fld;

        if (object.getId() == null) {
            object.setId(Uids.newUID());
            log.debug("New UID {}", object.getId());
        }

        // Update date.
        //
        fld = context.update$_field();
        fld.setIntValue(MTime.fromCurrentTime());
        doc.add(fld);

        // Document type.
        //
        fld = context.type$_field();
        fld.setStringValue(object.getClass().getSimpleName());
        doc.add(fld);

        // Document object serialized in json format and compressed using
        // snappy.
        //
        fld = context.obj$_field();
        fld.setBytesValue(Snappy.compress(serializeToJson(object).getBytes()));
        doc.add(fld);

        ModelType modelType = context.getModelType(object.getClass());

        modelType.saveToLuceneDoc(context, object, doc);

        Term term = new Term("id", object.getId());

        indexWriter().updateDocument(term, doc);

        // commit();
        // object = queryObject(object.getClass(), object.getUid()).getObject();
    }

    private SharedLuceneWriter openSharedWriter() {
        if (_writer == null)
            _writer = luceneIndex.openSharedWriter();

        return _writer;
    }

    private SharedLuceneReader openSharedReader() {
        if (_reader == null)
            _reader = luceneIndex.openSharedReader();

        return _reader;
    }

    public IndexSearcher indexSearcher() {
        return openSharedReader().indexSearcher();
    }

    public IndexWriter indexWriter() {
        return openSharedWriter().indexWriter();
    }

    public IndexReader indexReader() {
        return openSharedReader().indexReader();
    }

    public LuceneIndex luceneIndex() {
        return luceneIndex;
    }

    @Override
    public void close() {
        if (_writer != null) {
            luceneIndex.releaseSharedWriter(_writer);
            _writer = null;
        }

        if (_reader != null) {
            luceneIndex.releaseSharedReader(_reader);
            _reader = null;
        }
    }

    public void mergeAndCommit() throws IOException {
        indexWriter().forceMergeDeletes(true);
        indexWriter().forceMerge(2);
        commit();
    }

    public void deleteObject(StoreObject obj) throws IOException {
        Query q = queryHelper().createQuery("id", obj.getId());
        deleteObjects(q);
    }

    public void deleteObjects(Query query) throws IOException {
        indexWriter().deleteDocuments(query);
    }

    public void commit() throws IOException {
        // Commit writes.
        //
        if (_writer != null) {
            luceneIndex.commitWrites();

            // Close the current reader.
            //
            if (_reader != null) {
                luceneIndex.releaseSharedReader(_reader);
                _reader = null;
            }
        }
    }

    public IndexedFieldCountsBuilder indexedFieldCountsBuilder() {
        return new IndexedFieldCountsBuilder(this);
    }

    public QueryHelper queryHelper() {
        return new QueryHelper(this);
    }

    public <T extends StoreObject> List<T> queryRelated(Class<T> type, String query) throws IOException {

        QueryResults<T> objs = queryHelper().setType(type).setQuery(query).query();

        return objs.all();
    }

    public <T extends StoreObject> T getObject(String id) throws IOException {
        QueryResults<T> obj = queryObject(id);

        if (obj.totalHits() == 0)
            return null;
        else
            return obj.first();
    }

    public QueryBuilder queryBuilder() {
        return new QueryBuilder(luceneIndex.getAnalyser());
    }

    public StandardQueryParser standardQueryParser() {
        return new StandardQueryParser(luceneIndex.getAnalyser());
    }
}
