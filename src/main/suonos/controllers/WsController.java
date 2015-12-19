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
import com.github.am0e.webc.action.annotations.Action;

import suonos.controllers.resp.JsonResp;

/**
 * /ws/ Web Services root controller.
 * 
 * @author anthony
 *
 */
public final class WsController extends Controller {

    public WsController(ActionCtx ctx) {
        super(ctx);
    }

    @Action
    public Object server_error() {
        return JsonResp.error(ctx.response().getStatusCode(), "ServerError", "Server Error");
    }

    @Action
    public Object resource_not_found() {
        return JsonResp.error(ctx.response().getStatusCode(), "NotFound", "Resource Not Found");
    }
}
