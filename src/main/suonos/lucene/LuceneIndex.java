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
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.beans.BeanException;
import com.github.am0e.commons.cache.ICache;
import com.github.am0e.commons.providers.Context;
import com.github.am0e.jdi.BeanContainer;
import com.github.am0e.jdi.annotn.Global;
import com.github.am0e.jdi.interfaces.Startable;

/**
 * This class manages the Lucene index. It is a singleton object.
 * 
 * @author Anthony
 */
@Singleton
@Global
public class LuceneIndex implements Startable {
    static final Logger log = LoggerFactory.getLogger(LuceneIndex.class);

    /**
     * Path to the lucene store.
     */
    private Path indexRoot;
    private Analyzer analyser;
    private SharedLuceneReader sharedIndexReader;
    private SharedLuceneWriter sharedIndexWriter;
    private Similarity similarity;
    private IndexModels models;
    private ICache<Object> cache = AntLib.getCache(LuceneIndex.class, "cache");

    public LuceneIndex() throws IOException {
        models = new IndexModels();
        indexRoot = Paths.get("./data/lucene");
        models.load();
        initialiseAnalyser();
    }

    public Statement getStatement() {
        return new Statement(this, Context.instanceOf(StatementContext.class));
    }

    public Analyzer getAnalyser() {
        return analyser;
    }

    public Path getIndexDirectory() {
        return getIndexRoot();
    }

    public final Path getIndexRoot() {
        return indexRoot;
    }

    public NIOFSDirectory getLuceneDirectory() throws IOException {
        return new NIOFSDirectory(getIndexDirectory());
    }

    private void initialiseAnalyser() {
        analyser = new FieldsAnalyser(models.indexedFields);
    }

    public SharedLuceneWriter openSharedWriter() {

        synchronized (this) {
            // Do we have a searcher for the store id?
            //
            if (sharedIndexWriter == null) {
                // Make sure that we have an IndexReader before creating the
                // IndexWriter so that if any searches are
                // performed while the index is being written to, the readers
                // will not see the changes until we have committed.
                //
                try {
                    Directory indexDir = getLuceneDirectory();
                    IndexWriter writer = getIndexWriter(indexDir);
                    sharedIndexWriter = new SharedLuceneWriter(this, writer);

                } catch (IOException e) {
                    throw new BeanException(e);
                }
            }

            sharedIndexWriter.incRefCount();
            return sharedIndexWriter;
        }
    }

    private IndexWriter getIndexWriter(Directory indexDir) throws IOException {
        // Build the index writer using our analyser to do the analysis.
        // This is the same analyser as used by the searcher.
        //
        IndexWriterConfig cfg = new IndexWriterConfig(getAnalyser());
        cfg.setOpenMode(OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(indexDir, cfg);
    }

    public void commitWrites() throws IOException {
        synchronized (this) {
            if (sharedIndexWriter != null) {
                // Commit the changes to the writer.
                //
                sharedIndexWriter.indexWriter().commit();

                // Tear down the reader.
                //
                tearDownReader();
            }
        }
    }

    void releaseSharedWriter(SharedLuceneWriter writer) {

        synchronized (this) {
            // Sanity check.
            //
            if (writer != sharedIndexWriter) {
                throw new IllegalArgumentException();
            }

            try {
                // Decrease reference count. If 0 the search is kept open for
                // the next call.
                //
                if (writer.decRefCount() == 0) {
                    // Close down the writer as we have finished writing.
                    //
                    writer.releaseResources();

                    // Tear down the existing index searcher as the index has
                    // changed and we need to allocate a new
                    // searcher for the changes to be seen during searching.
                    //
                    tearDownReader();
                }

                this.sharedIndexWriter = null;

            } catch (IOException e) {
                throw new BeanException(e);
            }
        }
    }

    /**
     * Retrieves the Lucene IndexSearcher object. The IndexSearcher is allocated
     * only once and is shared between multiple threads. When closeWriter() is
     * called, the index searcher is reset and existing IndexSearcher objects
     * are closed.
     * 
     * @param storeId
     * @return A single IndexSearcher shared by all threads. Use closeSearcher()
     *         to close the searcher rather than calling the internal close()
     *         method.
     */
    public SharedLuceneReader openSharedReader() {

        synchronized (this) {
            // Do we have a searcher for the store id?
            //
            if (sharedIndexReader == null) {
                // Create a new lucene Index Reader using the currently active
                // index.
                //
                try {
                    Directory indexDir = getLuceneDirectory();

                    // Check if there is an index.
                    //
                    if (indexDir.listAll().length == 0) {
                        // Create an empty index.
                        //
                        IndexWriter writer = getIndexWriter(indexDir);
                        writer.close();
                    }

                    // Open a reader onto the index.
                    //
                    IndexReader reader = DirectoryReader.open(indexDir);

                    // Wrap the reader.
                    //
                    sharedIndexReader = new SharedLuceneReader(this, reader);

                    // If there are any searches currently active in other
                    // threads, they will all call decCount() on the
                    // RefCountedIndexSearcher until the final one causes the
                    // index reader to be closed.

                } catch (IOException e) {
                    throw new BeanException(e);
                }
            }

            // Increase usage count first time to 1.
            //
            sharedIndexReader.incRefCount();
            return sharedIndexReader;
        }
    }

    void releaseSharedReader(SharedLuceneReader searcher) {
        synchronized (this) {
            try {
                // Decrease reference count. If 0 the search is kept open for
                // the next call.
                //
                searcher.decRefCount();

            } catch (IOException e) {
                throw new BeanException(e);
            }
        }
    }

    private void tearDownReader() {

        synchronized (this) {
            // Create a new IndexReader/Search combo.
            // If we have an existing object, set it to close down when ref
            // count reaches zero.
            //
            if (sharedIndexReader != null) {
                try {
                    sharedIndexReader.forceCloseDown();

                } catch (IOException e) {
                    throw new BeanException(e);
                }

                // Clear the reader so that we create a new one.
                //
                sharedIndexReader = null;
            }
        }
    }

    public final void setIndexRoot(String indexRoot) {
        this.indexRoot = Paths.get(indexRoot);
    }

    public void start(BeanContainer container) throws Exception {
    }

    public void stop(BeanContainer container) throws Exception {
    }

    public final ICache<Object> getCache() {
        return cache;
    }

    public final IndexModels models() {
        return models;
    }

    public final ModelType getModelType(String modelName) {
        return models.getModelType(modelName);
    }

    public final ModelType getModelType(Class<?> type) {
        return models.getModelType(type);
    }
}
