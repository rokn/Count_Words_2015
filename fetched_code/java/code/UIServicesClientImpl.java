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

package org.kie.server.client.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.UIServicesClient;

import static org.kie.server.api.rest.RestURI.*;

public class UIServicesClientImpl extends AbstractKieServicesClientImpl implements UIServicesClient {

    public UIServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public UIServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }


    @Override
    public String getProcessForm(String containerId, String processId, String language) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(baseURI, FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap) + "?lang=" + language);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormService", "getFormDisplayProcess", new Object[]{containerId, processId, language} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public String getTaskForm(String containerId, Long taskId, String language) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.TASK_INSTANCE_ID, taskId);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(baseURI, FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + "?lang=" + language);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormService", "getFormDisplayTask", new Object[]{containerId, taskId, language} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public String getProcessImage(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_ID, processId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.APPLICATION_SVG_XML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(baseURI, IMAGE_URI + "/" + PROCESS_IMG_GET_URI, valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ImageService", "getProcessImage", new Object[]{containerId, processId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public String getProcessInstanceImage(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.APPLICATION_SVG_XML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(baseURI, IMAGE_URI + "/" + PROCESS_INST_IMG_GET_URI, valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ImageService", "getActiveProcessImage", new Object[]{containerId, processInstanceId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }
}
