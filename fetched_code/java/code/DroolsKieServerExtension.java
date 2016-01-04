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

package org.kie.server.services.drools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.remote.Remotable;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieContainerCommandServiceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.drools.compiler.kproject.xml.DependencyFilter;

public class DroolsKieServerExtension implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger(DroolsKieServerExtension.class);

    public static final String EXTENSION_NAME = "Drools";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));
    private static final Boolean filterRemoteable = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_FILTER_REMOTEABLE_CLASSES, "false"));

    private RulesExecutionService rulesExecutionService;
    private KieContainerCommandService batchCommandService;
    private KieServerRegistry registry;

    private List<Object> services = new ArrayList<Object>();

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.rulesExecutionService = new RulesExecutionService(registry);
        this.batchCommandService = new DroolsKieContainerCommandServiceImpl(kieServer, registry, this.rulesExecutionService);
        this.registry = registry;
        if (registry.getKieSessionLookupManager() != null) {
            registry.getKieSessionLookupManager().addHandler(new DroolsKieSessionLookupHandler());
        }
        services.add(batchCommandService);
        services.add(rulesExecutionService);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();

        // create kbases so declared types can be created
        Collection<String> kbases = kieContainerInstance.getKieContainer().getKieBaseNames();
        for (String kbase : kbases) {
            kieContainerInstance.getKieContainer().getKieBase(kbase);
        }

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(kieContainerInstance.getKieContainer().getReleaseId(), DependencyFilter.COMPILE_FILTER);
        Collection<String> packages = metaData.getPackages();

        for (String p : packages) {
            Collection<String> classes = metaData.getClasses(p);

            for (String c : classes) {
                String type = p + "." + c;
                try {
                    logger.debug("Adding {} type into extra jaxb classes set", type);
                    Class<?> clazz = Class.forName(type, true, kieContainerInstance.getKieContainer().getClassLoader());

                    if (filterRemoteable) {
                        // add only these classes that are valid for being used as remoteable
                        if (clazz.isAnnotationPresent(XmlRootElement.class) || clazz.isAnnotationPresent(Remotable.class)) {
                            extraClasses.add(clazz);
                            logger.debug("Added {} type into extra jaxb classes set", type);
                        }
                    } else {
                        extraClasses.add(clazz);
                        logger.debug("Added {} type into extra jaxb classes set", type);
                    }
                } catch (ClassNotFoundException e) {
                    logger.warn("Unable to create instance of type {} due to {}", type, e.getMessage());
                    logger.debug("Complete stack trace for exception while creating type {}", type, e);
                }  catch (Throwable e) {
                    logger.warn("Unexpected error while create instance of type {} due to {}", type, e.getMessage());
                    logger.debug("Complete stack trace for unknown error while creating type {}", type, e);
                }
            }
        }

        kieContainerInstance.addJaxbClasses(extraClasses);

    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {

    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList =  new ArrayList<Object>();
        Object [] services = { 
                batchCommandService,
                rulesExecutionService,
                registry

        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) { 
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(batchCommandService.getClass())) {
            return (T) batchCommandService;
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "BRM";
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
}
