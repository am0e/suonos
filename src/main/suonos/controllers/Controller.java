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

import com.github.am0e.webc.action.ActionCtx;

import suonos.app.SuonosLib;
import suonos.controllers.resp.DataResp;
import suonos.controllers.resp.JsonResp;
import suonos.services.queries.QuerySvcs;

public abstract class Controller {

    protected final SuonosLib lib = SuonosLib.lib();
    protected final ActionCtx ctx;
    private QuerySvcs querySvcs;

    protected Controller(ActionCtx ctx) {
        this.ctx = ctx;
    }

    protected JsonResp json() {
        return new JsonResp();
    }

    protected DataResp jsonData(Object data) {
        return new DataResp(data);
    }

    protected QuerySvcs querySvcs() {
        if (querySvcs == null)
            querySvcs = new QuerySvcs(ctx);
        return querySvcs;
    }
}
