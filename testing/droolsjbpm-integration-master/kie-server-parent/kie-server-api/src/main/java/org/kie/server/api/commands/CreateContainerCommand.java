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

package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name = "create-container")
@XStreamAlias( "create-container" )
@XmlAccessorType(XmlAccessType.NONE)
public class CreateContainerCommand implements KieServerCommand {

    private static final long    serialVersionUID = -1803374525440238478L;

    @XmlElement
    @XStreamAlias( "kie-container" )
    private KieContainerResource container;

    public CreateContainerCommand() {
        super();
    }

    public CreateContainerCommand(KieContainerResource container) {
        this.container = container;
    }

    public KieContainerResource getContainer() {
        return container;
    }

    public void setContainer(KieContainerResource container) {
        this.container = container;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((container == null) ? 0 : container.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CreateContainerCommand other = (CreateContainerCommand) obj;
        if (container == null) {
            if (other.container != null)
                return false;
        } else if (!container.equals(other.container))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CreateContainerCommand [container=" + container + "]";
    }

}
