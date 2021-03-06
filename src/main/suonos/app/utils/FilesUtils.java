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

package suonos.app.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.am0e.commons.json.JsonObjectReader;
import com.github.am0e.commons.json.JsonObjectWriter;

public final class FilesUtils {
    public static void writeString(Path path, String str) throws IOException {
        byte[] bytes = str.getBytes("utf-8");
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String readString(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, "utf-8");
    }

    public static <T> T readObject(Class<T> type, Path path) throws IOException {
        return JsonObjectReader.asObject(new StringReader(readString(path)), type);
    }

    public static void saveObject(Object obj, Path path) throws IOException {
        JsonObjectWriter jw = new JsonObjectWriter();
        writeString(path, jw.write(obj));
    }

    public static String resolvePath(String path) {
        return System.getProperty("user.dir") + "/" + path;
    }
}
