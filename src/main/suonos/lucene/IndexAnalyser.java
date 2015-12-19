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

import org.apache.lucene.analysis.Analyzer;

public class IndexAnalyser {
    private String name;
    private Analyzer analyser;

    public IndexAnalyser() {
    }

    public IndexAnalyser(String name, Analyzer analyzer) {
        setName(name);
        setAnalyser(analyzer);
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
        this.name = name;
    }

    /**
     * @return the analyser
     */
    public Analyzer analyser() {
        return analyser;
    }

    /**
     * @param analyser
     *            the analyser to set
     */
    public void setAnalyser(Analyzer analyser) {
        this.analyser = analyser;
    }
}
