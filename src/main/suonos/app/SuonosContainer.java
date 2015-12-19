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

import java.lang.reflect.Field;

import com.github.am0e.jdi.DefaultBeanContainer;
import com.github.am0e.jdi.LifecycleManager;
import com.github.am0e.jdi.XmlConfigurator;
import com.github.am0e.jdi.annotn.Setting;

import suonos.app.settings.Settings;

public final class SuonosContainer extends DefaultBeanContainer {

    // Setup a lifecycle manager for the beans.
    //
    private final LifecycleManager lifecycleManager = new LifecycleManager(this);
    private Settings settings;

    public SuonosContainer() {
        super(null);
    }

    public void init() {
        new XmlConfigurator(this).fileName("conf/conf.xml").load();

        lifecycleManager.initalize();
        lifecycleManager.start();
    }

    public Settings getSettings() {
        if (settings == null) {
            settings = getBean(Settings.class);
        }

        return settings;
    }

    @Override
    public Object getBean(Field field) {
        Setting setting = field.getAnnotation(Setting.class);
        if (setting != null) {
            String val = getSettings().getString(setting.path(), setting.defValue());
            if (val.isEmpty())
                return null;
            else
                return val;
        } else {
            return super.getBean(field);
        }
    }

}
