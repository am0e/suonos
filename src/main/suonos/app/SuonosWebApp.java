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

import java.nio.file.Path;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.commons.utils.Validate;
import com.github.am0e.jdi.annotn.Global;
import com.github.am0e.webc.WebApp;
import com.github.am0e.webc.WebRequest;
import com.github.am0e.webc.action.RequestParameterBinder;
import com.github.am0e.webc.view.ModelView;
import com.github.am0e.webc.view.impl.ViewResolverImpl;
import com.github.am0e.webc.view.jtl.JtlWebViewEngine;
import com.github.am0e.webc.views.forms.WebFormFields;

import suonos.httpserver.VertxWebApp;

@Global
@Singleton
public class SuonosWebApp extends VertxWebApp {

    static final Logger log = LoggerFactory.getLogger(SuonosWebApp.class);

    public SuonosWebApp() {
        super(".");

        JtlWebViewEngine engine = new JtlWebViewEngine(this);
        engine.setTemplateLoaderRootPath("./views");

        ViewResolverImpl resolver = new ViewResolverImpl();
        resolver.addWebViewEngine(engine);
        resolver.setPrefix("suonos.controllers");
        setViewResolver(resolver);
    }

    @Override
    public <T> T getInstanceOf(Class<T> type) {
        return AppMain.container.getBean(type);
    }

    @Override
    public RequestParameterBinder createQueryParamBinder(WebRequest ctx) {
        throw Validate.notImplemented();
    }

    @Override
    public WebFormFields createWebFormFields(WebRequest ctx, ModelView modelView) {
        throw Validate.notImplemented();
    }

    @Override
    public void logException(WebRequest ctx, Exception ex) {
        log.error("Request Exception", ex);
    }

    @Override
    public void enterRequest(WebRequest ctx) {
        SuonosLib.enterCtx();
        super.enterRequest(ctx);
    }

    @Override
    public void leaveRequest(WebRequest ctx) {
        super.leaveRequest(ctx);
        SuonosLib.leaveCtx();
    }

    @Override
    public Path getWebRoutesConfFilePath(WebApp app) {
        // relative to webapp folder.
        //
        return app.file("./conf/web-routes.xml");
    }
}
