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
package suonos.models.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for lucene fields. Used by the index serializer when building
 * Lucene documents.
 * 
 * @author anthony
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexField {
    boolean indexed() default true;

    boolean stored() default false;

    boolean omitNorms() default false;

    boolean idField() default false;

    boolean multiValue() default false;

    boolean prefixed() default true;

    boolean docValues() default false;

    boolean filterable() default false;

    String analyzer() default "";
}
