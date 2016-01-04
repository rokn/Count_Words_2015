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

package org.kie.server.client.helper;

import java.util.HashMap;
import java.util.Map;

import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.impl.JobServicesClientImpl;
import org.kie.server.client.impl.ProcessServicesClientImpl;
import org.kie.server.client.impl.QueryServicesClientImpl;
import org.kie.server.client.impl.UserTaskServicesClientImpl;

public class JBPMServicesClientBuilder implements KieServicesClientBuilder {

    @Override
    public String getImplementedCapability() {
        return "BPM";
    }

    @Override
    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {

        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

        services.put(ProcessServicesClient.class, new ProcessServicesClientImpl(configuration, classLoader));
        services.put(UserTaskServicesClient.class, new UserTaskServicesClientImpl(configuration, classLoader));
        services.put(QueryServicesClient.class, new QueryServicesClientImpl(configuration, classLoader));
        services.put(JobServicesClient.class, new JobServicesClientImpl(configuration, classLoader));

        return services;
    }
}
