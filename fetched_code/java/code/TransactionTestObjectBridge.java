/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.persistence.jta;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class TransactionTestObjectBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value != null) {
			try {
				TransactionTestObject obj = (TransactionTestObject) value;
				Long objId = obj.getId();
				String objName = obj.getName();
				
				ByteArrayOutputStream baout = new ByteArrayOutputStream();
				ObjectOutputStream oout = new ObjectOutputStream(baout);
				oout.writeObject(objId);
				oout.writeUTF(objName);
				Field field = new Field(name, baout.toByteArray());
				field.setBoost(luceneOptions.getBoost());
				
				document.add(field);
			} catch (Exception e) {
				throw new RuntimeException("problem bridging SessionInfo", e);
			}
		}

	}

}
