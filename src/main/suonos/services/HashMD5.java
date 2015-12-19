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
package suonos.services;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.github.am0e.commons.beans.BeanException;

public final class HashMD5 extends AbstractSvcs {
    private MessageDigest digest;

    public HashMD5() {
        try {
            digest = MessageDigest.getInstance("MD5");

        } catch (NoSuchAlgorithmException ex) {
            throw new BeanException(ex);
        }
    }

    public final String hash(String key) {
        try {
            digest.update(key.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            throw new BeanException(e);
        }

        byte[] digest = this.digest.digest();

        // Important : we internalize the result of this computation
        // because all equals between items is done with '==' operator
        // against strings and new discovered items call this method
        //
        return new BigInteger(digest).abs().toString(36).intern();
    }
}
