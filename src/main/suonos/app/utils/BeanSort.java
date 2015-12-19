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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.github.am0e.commons.beans.BaseInfo;
import com.github.am0e.commons.beans.BeanException;
import com.github.am0e.commons.beans.BeanInfo;
import com.github.am0e.commons.beans.FieldInfo;
import com.github.am0e.commons.beans.MapFieldInfo;

public class BeanSort {
    private static class Item implements Comparable<Item> {
        Comparable<Object> key;
        Object val;

        @Override
        public int compareTo(Item o) {
            return key.compareTo(o.key);
        }
    }

    @SuppressWarnings("unchecked")
    public static void sort(List<?> values, String orderBy, boolean reverse) {
        if (values == null || values.isEmpty() || orderBy == null)
            return;

        Item[] nvList = new Item[values.size()];

        BaseInfo getter = getFieldGetter(values.get(0), orderBy);

        for (int i = 0; i != values.size(); i++) {
            Object val = values.get(i);
            Object key = getter.callGetter(val);

            if (key instanceof String)
                key = key.toString().toLowerCase();

            Item item = new Item();
            item.val = val;
            item.key = (Comparable<Object>) key;

            nvList[i] = item;
        }

        Arrays.sort(nvList);

        if (reverse)
            ArrayUtils.reverse(nvList);

        final List<Object> tlist = (List<Object>) values;

        for (int i = 0; i != nvList.length; i++) {
            tlist.set(i, nvList[i].val);
        }
    }

    public static BaseInfo getFieldGetter(Object bean, String fieldName) {
        if (bean instanceof Map) {
            return new MapFieldInfo(fieldName);
        } else {
            BeanInfo beanInfo = BeanInfo.forClass(bean.getClass());

            // If no method, try a public field.
            //
            FieldInfo getter = beanInfo.getPublicField(fieldName);

            if (getter == null) {
                throw BeanException.fmtExcStr("Getter not found", bean, fieldName, null);
            }
            return getter;
        }
    }
}
