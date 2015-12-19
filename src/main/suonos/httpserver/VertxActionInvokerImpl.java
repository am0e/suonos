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

package suonos.httpserver;

import com.github.am0e.webc.action.impl.ActionInvokerImpl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class VertxActionInvokerImpl extends ActionInvokerImpl<VertxWebRequest> {

    public VertxActionInvokerImpl(VertxRouteCtx ctx) {
        super(ctx, ctx.request());
    }

    @Override
    protected Object getMethodParam(Class<?> type) throws Exception {
        if (type == HttpServerResponse.class)
            return request.httpServerResponse();

        if (type == HttpServerRequest.class)
            return request.httpServerRequest();

        if (type == VertxWebRequest.class)
            return request;

        return super.getMethodParam(type);
    }

}
