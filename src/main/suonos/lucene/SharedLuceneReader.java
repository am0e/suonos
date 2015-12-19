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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed lucene reader.
 * 
 * There is a single instance of this class shared by different threads. The
 * lucene IndexReader class is thread safe.
 */
final public class SharedLuceneReader extends SharedBase {

    private static final Logger log = LoggerFactory.getLogger(SharedLuceneReader.class);

    private IndexReader reader;
    private IndexSearcher searcher;
    private LuceneIndex store;

    public SharedLuceneReader(LuceneIndex store, IndexReader reader) throws CorruptIndexException, IOException {
        this.reader = reader;
        this.store = store;
    }

    public void close() {
        store.releaseSharedReader(this);
    }

    void releaseResources() throws IOException {

        if (refCount != 0)
            throw new IllegalStateException();

        log.debug("Destroying IndexReader");

        if (searcher != null) {
            searcher = null;
        }

        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    public IndexReader indexReader() {
        return reader;
    }

    public IndexSearcher indexSearcher() {
        if (searcher == null) {
            this.searcher = new IndexSearcher(reader);
        }
        return searcher;
    }

    public Document doc(int doc) throws CorruptIndexException, IOException {
        return indexSearcher().doc(doc);
    }

    public int maxDoc() throws IOException {
        return reader.maxDoc();
    }

    public LuceneIndex getStore() {
        return store;
    }
}
