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

package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "double-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDouble implements Wrapped<Double> {

    @XmlElement
    @XmlSchemaType(name = "double")
    private double value;

    public JaxbDouble() {
    }

    public JaxbDouble(Double value) {
        if (value != null) {
            this.value = value;
        }
    }

    public double getValue() {
        return value;
    }

    public void setValue(Double value) {
        if (value != null) {
            this.value = value;
        }
    }

    @Override
    public Double unwrap() {
        return value;
    }
}
