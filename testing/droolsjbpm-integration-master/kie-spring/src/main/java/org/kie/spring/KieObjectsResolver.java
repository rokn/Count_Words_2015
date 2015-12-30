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

package org.kie.spring;

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;

public class KieObjectsResolver {

    public KieBase resolveKBase(String id, ReleaseId releaseId) {
        KieContainer kieContainer = resolveKContainer(releaseId);
        KieBase kieBase = kieContainer.getKieBase(id);
        if (kieBase == null) {
            kieBase = kieContainer.newKieBase(id, null);
        }
        return kieBase;
    }

    public Object resolveKSession(String id, ReleaseId releaseId) {
        KieContainer kieContainer = resolveKContainer(releaseId);
        return resolveKSession(kieContainer, id);
    }

    public Object resolveKSession(KieContainer kieContainer, String id) {
        KieProject kProject = ((KieContainerImpl) kieContainer).getKieProject();
        KieSessionModel kieSessionModel = kProject.getKieSessionModel(id);
        if (kieSessionModel.getType() == KieSessionModel.KieSessionType.STATEFUL) {
            return ((KieContainerImpl) kieContainer).getKieSession(id);
        } else if (kieSessionModel.getType() == KieSessionModel.KieSessionType.STATELESS) {
            return ((KieContainerImpl) kieContainer).getStatelessKieSession(id);
        }
        return null;
    }

    private KieContainer resolveKContainer(ReleaseId releaseId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Cannot resolve a KieContainer using a null ReleaseId");
        }
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        if ( kieContainer == null) {
            throw new IllegalArgumentException("Could not find a KModule with ReleaseId ("+releaseId+")");
        }
        return kieContainer;
    }

    public StatelessKieSession newStatelessSession(String kbaseName, ReleaseId releaseId, KieSessionConfiguration conf) {
        KieBase kieBase = resolveKBase(kbaseName, releaseId);
        if (kieBase == null) {
            KieContainer kieContainer = resolveKContainer(releaseId);
            if (conf == null) {
                return kieContainer.newStatelessKieSession();
            } else {
                return kieContainer.newStatelessKieSession(conf);
            }
        } else {
            if (conf == null) {
                return kieBase.newStatelessKieSession();
            } else {
                return kieBase.newStatelessKieSession(conf);
            }
        }
    }

    public Object newStatefulSession(String kbaseName, ReleaseId releaseId, KieSessionConfiguration conf) {
        KieBase kieBase = resolveKBase(kbaseName, releaseId);
        if (kieBase == null) {
            KieContainer kieContainer = resolveKContainer(releaseId);
            if (conf == null) {
                return kieContainer.newKieSession(kbaseName);
            } else {
                return kieContainer.newKieSession(conf);
            }
        } else {
            if (conf == null) {
                return kieBase.newKieSession();
            } else {
                return kieBase.newKieSession(conf, null);
            }
        }
    }
}
