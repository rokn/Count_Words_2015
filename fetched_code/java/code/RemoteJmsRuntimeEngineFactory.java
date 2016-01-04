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

package org.kie.services.client.api;

import java.net.URL;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This class will be deleted as of 7.
 * </p>
 * A factory for creating JMS remote API client instances of the {@link RuntimeEngine}.
 * @see {@link RemoteRuntimeEngineFactory}
 */
@Deprecated
public class RemoteJmsRuntimeEngineFactory {
  
    // The name of this class may not be changed until 7.x for backwards compatibility reasons!
   
    protected RemoteConfiguration config; 
    
    protected RemoteJmsRuntimeEngineFactory() {
        // private constructor 
    }
    
    protected RemoteJmsRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }

    /**
     * Retrieves the (remote) {@link InitialContext} from the JBoss AS server instance in order 
     * to be able to retrieve the {@link ConnectionFactory} and {@link Queue} instances to communicate 
     * with the workbench, console or BPMS instance.
     * </p>
     * This method is deprecated in favor of the {@link RemoteRuntimeEngineFactory#getRemoteJbossInitialContext(String, String, String)}
     * instance, which takes a {@link String} hostname parameter instead of a {@link URL} parameter.
     * 
     * @param url The URL of the server instance
     * @param user A user permitted to retrieve the remote {@link InitialContext}
     * @param password The password for the user specified
     * @return an {@link InitialContext} that contains the {@link ConnectionFactory} and {@link Queue} instances to communicate
     * with the workbench, console or BPMS instance.
     */
    @Deprecated
    public static InitialContext getRemoteJbossInitialContext(URL url, String user, String password) { 
        return org.kie.remote.client.api.RemoteRuntimeEngineFactory.getRemoteJbossInitialContext(url.getHost(), user, password);
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    /**
     * @see  {@link RemoteRuntimeEngineFactory#newJmsBuilder()}
     */
    @Deprecated
    public static RemoteJmsRuntimeEngineBuilder newBuilder()  { 
       return RemoteRuntimeEngineFactory.newJmsBuilder();
    }

}