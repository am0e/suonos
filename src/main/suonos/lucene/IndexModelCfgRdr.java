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

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.am0e.commons.beans.BaseInfo;
import com.github.am0e.commons.beans.BeanClassWrapper;
import com.github.am0e.commons.beans.BeanUtils;
import com.github.am0e.commons.utils.Validate;
import com.github.am0e.commons.xml.model.Attribute;
import com.github.am0e.commons.xml.model.Attributes;
import com.github.am0e.commons.xml.model.Document;
import com.github.am0e.commons.xml.model.Element;
import com.github.am0e.commons.xml.parser.SimpleDomParser;

import suonos.lucene.fields.IndexedField;
import suonos.lucene.fields.IndexedFieldType;

public class IndexModelCfgRdr {

    private IndexModels models;
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private BeanClassWrapper classWrapper = new BeanClassWrapper();

    public static void load(IndexModels models, Path path) throws IOException {
        IndexModelCfgRdr rdr = new IndexModelCfgRdr();
        rdr.models = models;
        rdr.load(path);
    }

    private void load(Path path) throws IOException {
        try (Reader rdr = Files.newBufferedReader(Paths.get("./conf/models.xml"))) {
            load(rdr);
        }
    }

    private void load(Reader rdr) throws IOException {
        SimpleDomParser parser = new SimpleDomParser();
        Document doc = parser.parse("models.xml", rdr);
        addAnalyzers(doc);
        addFieldTypes(doc);
        addFields(doc);
        addModels(doc);
    }

    private void addModels(Document doc) {
        Element[] models = doc.getDocumentElement().getElements("model");

        for (Element model : models) {
            String typeName = model.getAttributeValue("type");
            Class<Object> ModelClass = loadClass(typeName);

            addType(ModelClass);
        }
    }

    private <T> Class<T> loadClass(String typeName) {
        return BeanUtils.loadClass(classLoader, typeName);
    }

    private void addAnalyzers(Document doc) {
        Element[] analyzers = doc.getDocumentElement().getElements("analyzer");

        for (Element el : analyzers) {
            String name = el.getAttributeValue("name");
            String type = el.getAttributeValue("type");
            classWrapper.setClass(loadClass(type));

            IndexAnalyser analyzer = new IndexAnalyser(name, classWrapper.newInstance());
            models.addAnalyzer(analyzer);
        }
    }

    private void addFields(Document doc) {
        Element[] fields = doc.getDocumentElement().getElements("field");

        classWrapper.setClass(IndexedField.class);

        for (Element el : fields) {
            IndexedField o = classWrapper.newInstance();
            bindAttributes(o, el);
            models.addField(o);
        }
    }

    private void addFieldTypes(Document doc) {
        Element[] fields = doc.getDocumentElement().getElements("type");

        classWrapper.setClass(IndexedFieldType.class);

        for (Element el : fields) {
            IndexedFieldType o = classWrapper.newInstance();
            bindAttributes(o, el);
            models.addFieldType(o);
        }
    }

    private void bindAttributes(Object o, Element el) {
        Attributes attributes = el.getAttributes();

        for (int i = 0; i != attributes.size(); i++) {
            Attribute e = attributes.get(i);

            String fieldName = e.getQname().replace('-', '_');
            Object value = e.getValue();

            BaseInfo setter = classWrapper.getBeanSetter(fieldName, value.getClass());

            if (setter == null) {
                Validate.notNull(setter, "Unknown field: {}", fieldName);
            }

            if (setter.getType() == IndexedFieldType.class) {
                value = models.getFieldType(value.toString());
            }
            classWrapper.callSetter(o, fieldName, value);
        }
    }

    private ModelType addType(Class<?> modelClass) {

        ModelType type = models.modelTypes.get(modelClass);
        if (type != null) {
            return type;
        }

        type = new ModelType(models, modelClass);

        models.models.put(type.modelName.intern(), type);
        models.models.put(type.abbrevName.intern(), type);
        models.modelTypes.put(type.modelClass, type);
        return type;
    }

}
