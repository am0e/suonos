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

package suonos.controllers.resp;

import java.io.IOException;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import com.github.am0e.commons.functions.WriterFunction;
import com.github.am0e.commons.json.JsonObjectWriter;
import com.github.am0e.webc.ResponseHandler;
import com.github.am0e.webc.WebException;
import com.github.am0e.webc.action.response.JsonResponse;

public class JsonResp {
    public static DataResp data(WriterFunction<JsonObjectWriter> w) {
        return new DataResp(w);
    }

    public static JsonResponse error(int httpStatusCode, String code, String detailMsg) {
        StringWriter w = new StringWriter();
        JsonGenerator jw = Json.createGenerator(w);
        jw.writeStartObject();
        jw.writeStartObject("error");
        jw.write("status", httpStatusCode);
        jw.write("detail", detailMsg);
        if (code != null)
            jw.write("code", code);

        jw.writeEnd();
        jw.writeEnd();
        jw.flush();
        return new JsonResponse(w.toString());
    }

    public static Object error(Exception ex) throws IOException {
        int statusCode = WebException.getStatusCode(ex);
        return error(statusCode, null, ex.toString());
    }

    public static JsonResponse success(String msg) {
        StringWriter w = new StringWriter();
        JsonGenerator jw = Json.createGenerator(w);
        jw.writeStartObject();
        jw.writeStartObject("success");
        jw.write("message", msg);
        jw.writeEnd();
        jw.writeEnd();
        jw.flush();
        return new JsonResponse(w.toString());
    }

    public ResponseHandler updated() {
        return success("updated");
    }

    public JsonResponse objectCreated(String id) {
        StringWriter w = new StringWriter();
        JsonGenerator jw = Json.createGenerator(w);
        jw.writeStartObject();
        jw.writeStartObject("success");
        jw.write("message", "object created");
        jw.write("id", id);
        jw.writeEnd();
        jw.writeEnd();
        jw.flush();
        return new JsonResponse(w.toString());
    }

    public Object objectDeleted(String id) {
        StringWriter w = new StringWriter();
        JsonGenerator jw = Json.createGenerator(w);
        jw.writeStartObject();
        jw.writeStartObject("success");
        jw.write("message", "object deleted");
        jw.write("id", id);
        jw.writeEnd();
        jw.writeEnd();
        jw.flush();
        return new JsonResponse(w.toString());
    }
}
