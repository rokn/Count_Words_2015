/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class KieSpringNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("kstore", new KStoreDefinitionParser());
        registerBeanDefinitionParser("kmodule", new KModuleDefinitionParser());
        registerBeanDefinitionParser("kbase", new KBaseDefinitionParser());
        registerBeanDefinitionParser("ksession", new KSessionDefinitionParser());
        registerBeanDefinitionParser("eventListeners", new EventListenersDefinitionParser());
        registerBeanDefinitionParser("environment", new EnvironmentDefinitionParser());
        registerBeanDefinitionParser("fileLogger", new LoggerDefinitionParser());
        registerBeanDefinitionParser("consoleLogger", new LoggerDefinitionParser());
        registerBeanDefinitionParser("import", new KieImportDefinitionParser());
        registerBeanDefinitionParser("releaseId", new ReleaseIdDefinitionParser());
    }

}
