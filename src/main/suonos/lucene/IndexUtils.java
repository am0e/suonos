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
package suonos.lucene;

import java.nio.ByteBuffer;
import java.util.Base64;

import org.apache.lucene.search.ScoreDoc;

public class IndexUtils {

    public static String buildLastScoreToken(ScoreDoc lastScore) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putInt(lastScore.doc);
        b.putFloat(lastScore.score);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b.array());
    }

    public static ScoreDoc parseLastScoreToken(String id) {
        ByteBuffer b = ByteBuffer.wrap(Base64.getUrlDecoder().decode(id));
        return new ScoreDoc(b.getInt(), b.getFloat());
    }

}
