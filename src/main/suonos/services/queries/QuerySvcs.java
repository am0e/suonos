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
package suonos.services.queries;

import java.io.IOException;

import com.github.am0e.commons.json.JsonObjectReader;
import com.github.am0e.commons.utils.StringUtil;
import com.github.am0e.webc.WebException;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.response.JsonResponse;
import com.github.am0e.webc.urls.UrlBuilder;

import suonos.app.SuonosLib;
import suonos.app.utils.TagUtils;
import suonos.controllers.resp.DataResp;
import suonos.controllers.resp.JsonResp;
import suonos.lucene.QueryResults;
import suonos.lucene.fields.IndexedFieldCounts;
import suonos.lucene.fields.IndexedFieldCountsBuilder;
import suonos.models.StoreObject;

public class QuerySvcs {
    private ActionCtx ctx;
    protected final SuonosLib lib = SuonosLib.lib();

    public QuerySvcs(ActionCtx ctx) {
        this.ctx = ctx;
    }

    public void query() {
    }

    public JsonResponse jsonQueryId(Class<? extends StoreObject> type) throws IOException {

        // Return back as a json object.
        //
        return jsonData(getObject());
    }

    public JsonResponse jsonQuery(Class<? extends StoreObject> type) throws IOException {

        return jsonQuery(type, ctx.param("q", null), ctx.param("s", null), ctx.param("n", null),
                ctx.params().getInteger("max", -1));
    }

    public JsonResponse jsonQuery(Class<? extends StoreObject> type, String query) throws IOException {

        return jsonQuery(type, query, ctx.params().getString("s", null), null, -1);
    }

    public JsonResponse jsonQuery(Class<? extends StoreObject> type, String q, String s, String nextToken, int max)
            throws IOException {

        s = s == null ? null : s.toLowerCase();

        // Build the query.
        //
        if (q != null) {
            q = q.toLowerCase();

            // Bit of a kludge!!!!
            // The q is "album_artists_u:"Glenn Gould"
            // We have to convert "Glenn Gould" to "glenngould" to conform to
            // the encoding of the album_artists_u field.
            // Need to rectify this.
            //
            int ndx = q.indexOf(':');
            if (ndx > 1) {
                String id = q.substring(0, ndx);
                String term = q.substring(ndx + 1);
                if (id.endsWith("_u")) {
                    q = id.concat(":").concat(TagUtils.convertStringToId(term));
                }
            }
        }

        // Query the objects.
        //
        QueryResults<StoreObject> objects = lib.stmt().queryHelper().setType(type).setQuery(q).setSort(s)
                .setNextToken(nextToken).setMax(max).query();

        return resultsAsJson(objects);
    }

    public JsonResponse resultsAsJson(QueryResults<StoreObject> objects) throws IOException {
        UrlBuilder urlb = ctx.request().urlBuilder();
        urlb.setQueryParams();
        String lastScoreToken = objects.lastScoreToken();
        String nextLink = null;

        if (lastScoreToken != null) {
            urlb.setQueryParam("n", lastScoreToken);
            nextLink = urlb.buildUrl(ctx.request().path());
        }

        // Return back as a json object.
        //
        return jsonData(objects).setNextLink(nextLink);
    }

    public IndexedFieldCounts queryFacets() throws IOException {
        // Get the ids.
        //
        String[] ids = StringUtil.split(ctx.params().getString("fields"), ",");

        // Optional filter string.
        //
        String filter = ctx.params().getString("filter", null);
        if ("all".equals(filter)) {
            filter = null;

        }
        if (filter != null) {
            filter = filter.toLowerCase();
        }

        IndexedFieldCountsBuilder bldr = lib.stmt().indexedFieldCountsBuilder();
        for (int i = 0; i != ids.length; i++) {
            bldr.addField(ids[i], filter);
        }

        return bldr.build();
    }

    public <E extends StoreObject> E getCreateObject(Class<E> typeClass) throws IOException {
        E data = getDataObject(typeClass);
        data.store_reset();
        return data;
    }

    public JsonResponse createObject(StoreObject obj) throws IOException {
        // Create the playlist.
        //
        lib.stmt().createObject(obj);

        return json().objectCreated(obj.getId());
    }

    public <T extends StoreObject> T getObject() throws IOException {
        String id = ctx.param("id");

        T obj = lib.stmt().getObject(id);

        if (obj == null)
            throw WebException.notFound();
        else
            return obj;
    }

    public <T extends StoreObject> QueryResults<T> getObjects(Class<T> typeClass) throws IOException {
        return lib.stmt().queryHelper().setType(typeClass).query();
    }

    public <T> T getDataObject(Class<T> typeClass) throws IOException {
        String data = ctx.param("data");
        return JsonObjectReader.asObject(data, typeClass);
    }

    public JsonResp json() {
        return new JsonResp();
    }

    public DataResp jsonData(Object data) {
        return new DataResp(data);
    }

    public <E extends StoreObject> E getPutObject(Class<E> typeClass) throws IOException {

        // First validate the object exists in the store.
        //
        E obj = getObject();

        // Deserialise the new data.
        //
        E data = getDataObject(typeClass);

        // data must not have a id as it was specified in the url.
        //
        if (data.getId() != null) {
            throw WebException.validationError("id not allowed in data payload");
        }

        // Store the id in the data object for an update.
        //
        data.setId(obj.getId());

        return data;
    }

    public Object putObject(StoreObject obj) throws IOException {
        // Save it back to the store.
        //
        lib.stmt().saveObject(obj);

        return new JsonResp().success("saved");
    }

    public Object deleteObject(StoreObject obj) throws IOException {
        // Create the playlist.
        //
        lib.stmt().deleteObject(obj);

        return json().objectDeleted(obj.getId());
    }
}