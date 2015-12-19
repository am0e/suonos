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

import com.github.am0e.webc.WebException;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.models.Rateable;
import suonos.models.StoreObject;
import suonos.services.RatingSvcs;

public abstract class StoredItemController<T extends StoreObject> extends Controller {

    private Class<T> typeClass;

    public StoredItemController(ActionCtx ctx, Class<T> type) {
        super(ctx);
        this.typeClass = type;
    }

    /**
     * Get all. GET /api/tracks?q=genres:Classical GET
     * /api/albums?q=genres:Classical GET /api/albums?q=title_l:S&
     * 
     * @throws IOException
     */
    @Action
    public Object index() throws IOException {
        return querySvcs().jsonQuery(typeClass);
    }

    /**
     * GET /api/tracks/-id GET /api/albums/-id GET /api/movies/-id GET
     * /api/playlists/[id]?display=1 (Get playlist including track names).
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object get() throws IOException {
        return jsonData(queryObject(typeClass));
    }

    protected T queryObject(Class<T> typeClass) throws IOException {
        return querySvcs().getObject();
    }

    /**
     * PUT /tracks/10232/rating?val=5
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object put_rating(ActionCtx ctx) throws IOException {
        StoreObject obj = querySvcs().getObject();

        if ((obj instanceof Rateable) == false) {
            throw WebException.badRequest();
        }

        int val = ctx.params().getInteger("val");
        lib.getInstanceOf(RatingSvcs.class).rate(obj, val);

        lib.stmt().saveObject(obj);

        return json().updated();
    }
}
