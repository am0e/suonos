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
package suonos.models;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

/**
 * MTime allows for compact times to be stored in the database as an integer. an
 * MTime value is an integer that contains the number of minutes since the java
 * epoch. The maximum date it can express is "Thu Jan 23 02:07:00 GMT 6053"
 * 
 * @author Anthony
 */
@SuppressWarnings("serial")
public class MTime extends Date {

    public MTime() {
    }

    public MTime(int mtime) {
        super(DateUtils.MILLIS_PER_MINUTE * mtime);
    }

    public int getMTime() {
        return fromDate(this);
    }

    public static int fromDate(Date date) {
        return date == null ? 0 : fromTime(date.getTime());
    }

    public static Date toDate(int mtime) {
        return mtime == 0 ? null : new MTime(mtime);
    }

    public static int fromCurrentTime() {
        return fromTime(System.currentTimeMillis());
    }

    public static int fromTime(long time) {
        return (int) (time / DateUtils.MILLIS_PER_MINUTE);
    }
}
