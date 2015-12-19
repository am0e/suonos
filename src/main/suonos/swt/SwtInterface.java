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
package suonos.swt;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.github.am0e.commons.providers.Context;
import com.github.am0e.jdi.annotn.Setting;

import suonos.httpserver.VertxHttpServer;

@Singleton
public final class SwtInterface {
    Display display;
    Shell shell;
    Browser browser;
    private RequestFunction requestFunction;

    @Inject
    @Setting(path = "app.fullscreenmode")
    private boolean fullScreenMode = true;

    public void run() throws IOException {

        try {
            initUI();

            // Standard SWT UI loop.
            //
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }

        } finally {
            disposeUI();
        }
    }

    private void disposeUI() {
        if (shell != null)
            shell.dispose();
        if (browser != null)
            browser.dispose();
        if (display != null)
            display.dispose();
        if (requestFunction != null)
            requestFunction.dispose();
    }

    private void initUI() throws IOException {
        display = new Display();
        shell = new Shell(display);

        // Full screen mode.
        //
        shell.setFullScreen(fullScreenMode);

        // Fill the layout.
        //
        shell.setLayout(new FillLayout());

        shell.setText("WebKit");
        Display.setAppName("Suonos");

        // url of local server.
        //
        String url = Context.instanceOf(VertxHttpServer.class).getUrl();

        // Construct the browser with the bootstrap html file.
        //
        browser = new Browser(shell, SWT.WEBKIT);
        browser.setUrl(url + "/index");

        // Create a request function for local requests.
        // Ie when the UI is running on the suonos host. Remote clients will use
        // http.
        //
        requestFunction = new RequestFunction(browser, "suonosRequest");

        shell.open();
    }

    /**
     * Our local request handling for serving local web requests from the local
     * UI.
     * 
     * @author anthony
     *
     */
    static class RequestFunction extends BrowserFunction {
        RequestFunction(Browser browser, String name) {
            super(browser, name);
        }

        @Override
        public Object function(Object[] args) {
            // Todo:
            // convert into a http style request and invoke web services.
            //
            String method = args[0].toString();
            String url = args[1].toString();
            return null;
        }
    }

}
