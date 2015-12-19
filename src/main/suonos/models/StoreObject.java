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
package suonos.models;

import suonos.models.annotations.IndexField;

public class StoreObject {
    private transient byte storeFlags;

    /**
     * This flag indicates the entity was queried from the database via a query
     * or has been recently inserted.
     */
    private static final int STORE_QUERIED = 1;

    /**
     * id. This is the PK. It is a unique id.
     */
    @IndexField(idField = true, prefixed = false, analyzer = "keyword")
    private String id;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public void onQueried() {
        this.storeFlags |= STORE_QUERIED;
    }

    public void onSaved() {
    }

    public boolean store_isQueried() {
        return (this.storeFlags & STORE_QUERIED) != 0;
    }

    public void store_reset() {
        this.storeFlags = 0;
        this.id = null;
    }
}
