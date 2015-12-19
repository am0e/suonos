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

package suonos.app.settings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import javax.inject.Singleton;

import com.github.am0e.jdi.annotn.Global;

@Singleton
@Global
public final class Settings {
    private Properties props;

    public Settings() throws IOException {
        props = new Properties();

        Path path = getPath();

        if (Files.isReadable(path)) {
            try (Reader rdr = Files.newBufferedReader(path)) {
                props.load(rdr);
            }
        }
    }

    private Path getPath() {
        return Paths.get("conf/settings.properties");
    }

    public String getString(String name, String defVal) {
        return props.getProperty(name, defVal);
    }

    public int getInt(String name, int defVal) {
        String v = props.getProperty(name);
        if (v == null)
            return defVal;
        else
            return Integer.parseInt(v);
    }

    public void set(String name, int val) {
        props.setProperty(name, Integer.toString(val));
    }

    public void save() throws IOException {
        Path path = getPath();

        try (BufferedWriter wr = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(wr, null);
        }
    }
}
