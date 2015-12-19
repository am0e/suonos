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

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class TagUtils {

    public static String getTagValue(Tag tag, FieldKey fk) {
        String s = tag.getFirst(fk);
        return normalizeTagValue(s.trim());
    }

    public static String normalizeTagValue(String value) {
        return value.trim();
    }

    public static String convertStringToId(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i != s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }
}
