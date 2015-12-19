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
package suonos.lucene.fields;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;

public class IndexedFieldType extends FieldType {
    /**
     * The Java value type.
     */
    private Class<?> javaType;

    private String name = "";

    private Analyzer analyzer;

    public IndexedFieldType() {
    }

    public IndexedFieldType(IndexedFieldType type) {
        super(type);
        this.name = type.name;
        this.analyzer = type.analyzer();
        this.javaType = type.javaType;
    }

    /**
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        super.checkIfFrozen();
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        boolean b = super.equals(obj);
        if (b) {
            IndexedFieldType other = (IndexedFieldType) obj;
            if (!name.equals(other.name))
                return false;
            if (analyzer() != other.analyzer())
                return false;
        }
        return b;
    }

    /**
     * @return the type
     */
    public Class<?> javaType() {
        return javaType;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setJavaType(Class<?> type) {
        super.checkIfFrozen();
        this.javaType = type;
    }

    /**
     * @return the analyzer
     */
    public Analyzer analyzer() {
        return analyzer;
    }

    /**
     * @param analyzer
     *            the analyzer to set
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        setTokenized(true);
    }
}
