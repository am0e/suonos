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

package suonos.app;

import java.io.Closeable;

import com.github.am0e.commons.providers.Context;
import com.github.am0e.commons.providers.ContextEvents;
import com.github.am0e.commons.providers.ObjectProvider;

import io.vertx.core.Vertx;
import suonos.lucene.LuceneIndex;
import suonos.lucene.Statement;
import suonos.models.music.MusicLib;
import suonos.services.music.MusicLibSvcs;
import suonos.services.player.MediaPlayerSvcs;

public final class SuonosLib implements ObjectProvider, ContextEvents {
    /**
     * Statement. Local to the current thread.
     */
    private Statement statement;
    private SuonosContainer container;

    public static SuonosLib lib() {
        return (SuonosLib) Context.objectProvider();
    }

    public SuonosLib() {
        this.container = AppMain.container;
    }

    public Vertx vertx() {
        return getInstanceOf(Vertx.class);
    }

    public MusicLibSvcs musicLibSvcs() {
        return getInstanceOf(MusicLibSvcs.class);
    }

    public MediaPlayerSvcs playerSvcs() {
        return getInstanceOf(MediaPlayerSvcs.class);
    }

    public LuceneIndex luceneIndex() {
        return getInstanceOf(LuceneIndex.class);
    }

    public MusicLib getMusicLib() {
        return getInstanceOf(MusicLib.class);
    }

    public Statement stmt() {
        if (statement == null) {
            statement = luceneIndex().getStatement();
        }
        return statement;
    }

    @Override
    public <T> T getInstanceOf(Class<T> type) {
        return container.getInstanceOf(type);
    }

    @Override
    public void enterContext() {
    }

    @Override
    public void leaveContext() {
        if (statement != null)
            statement.close();
        statement = null;
    }

    public <T> T instanceOf(Class<T> type) {
        return container.getBean(type);
    }

    public static Closeable enterCtx() {
        return Context.enterContext(new SuonosLib());
    }

    public static void leaveCtx() {
        Context.leaveContext();
    }
}
