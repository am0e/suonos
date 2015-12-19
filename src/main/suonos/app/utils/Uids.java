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

import java.util.UUID;

public class Uids {
    private static final long LIMIT = 10000000000L;
    private static long last = 0;

    final static char[] encode_cset = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'f', 'g', 'h',
            'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z', 'B', 'C', 'D', 'F', 'G', 'H',
            'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };

    public static String newUID() {
        // 10 digits.
        //
        long id = (System.currentTimeMillis() % LIMIT);

        synchronized (Uids.class) {
            if (id <= last) {
                id = (last + 1) % LIMIT;
            }
            last = id;
        }

        // Get a random number from UUID up to 10 digits.
        //
        UUID uid = UUID.randomUUID();
        long rand = uid.getLeastSignificantBits() % LIMIT;

        String s1 = com.github.am0e.commons.utils.Uids.encodeLong(Math.abs(id), encode_cset);
        String s2 = com.github.am0e.commons.utils.Uids.encodeLong(Math.abs(rand), encode_cset);

        return s1.concat(s2);
    }
}
