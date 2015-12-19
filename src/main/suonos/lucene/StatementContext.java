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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;

import com.github.am0e.commons.providers.Context;
import com.github.am0e.jdi.scopes.SessionScope;

/**
 * Statement context. Instances are scoped to the session so that they are local
 * to the executing thread and remain in existence until the end of the session.
 * 
 * @author anthony
 */
@SessionScope
public class StatementContext {

    /**
     * The lucene index.
     */
    private LuceneIndex index = Context.instanceOf(LuceneIndex.class);

    // type$ field. This is indexed so that we can do queries for matching
    // types.
    //
    private Field type$_field;

    // obj$ field. This stores a JSON object and is not indexed.
    //
    private Field obj$_field;

    // cdate$ field. This is indexed so that we can do queries for matching
    // types.
    //
    private Field update$_field;

    public IndexModels models() {
        return index.models();
    }

    public ModelType getModelType(String modelName) {
        return models().getModelType(modelName);
    }

    public ModelType getModelType(Class<?> model) {
        return models().getModelType(model);
    }

    public Field type$_field() {
        if (type$_field == null) {
            type$_field = new Field("type$", "", models().getFieldType("type$"));
        }
        return type$_field;
    }

    public Field obj$_field() {
        if (obj$_field == null) {
            obj$_field = new Field("obj$", new byte[] {}, models().getFieldType("obj$"));
        }
        return obj$_field;
    }

    public Field update$_field() {
        if (update$_field == null) {
            update$_field = new IntField("update$", 0, models().getFieldType("update$"));
        }
        return update$_field;
    }
}
