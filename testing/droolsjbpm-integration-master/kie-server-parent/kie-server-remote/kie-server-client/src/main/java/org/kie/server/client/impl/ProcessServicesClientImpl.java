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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.ProcessServicesClient;

import static org.kie.server.api.rest.RestURI.*;

public class ProcessServicesClientImpl extends AbstractKieServicesClientImpl implements ProcessServicesClient {

    public ProcessServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public ProcessServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public ProcessDefinition getProcessDefinition(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_GET_URI, valuesMap),
                    ProcessDefinition.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getProcessDefinition",  new Object[]{containerId, processId}) ) );
            ServiceResponse<ProcessDefinition> response = (ServiceResponse<ProcessDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public SubProcessesDefinition getReusableSubProcessDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_SUBPROCESS_GET_URI, valuesMap),
                    SubProcessesDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getReusableSubProcesses", new Object[]{containerId, processId}) ) );
            ServiceResponse<SubProcessesDefinition> response = (ServiceResponse<SubProcessesDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public VariablesDefinition getProcessVariableDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_VARIABLES_GET_URI, valuesMap),
                    VariablesDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getProcessVariables", new Object[]{containerId, processId}) ) );
            ServiceResponse<VariablesDefinition> response = (ServiceResponse<VariablesDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public ServiceTasksDefinition getServiceTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_SERVICE_TASKS_GET_URI, valuesMap),
                    ServiceTasksDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getServiceTasks", new Object[]{containerId, processId}) ) );
            ServiceResponse<ServiceTasksDefinition> response = (ServiceResponse<ServiceTasksDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public AssociatedEntitiesDefinition getAssociatedEntityDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI, valuesMap),
                    AssociatedEntitiesDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getAssociatedEntities", new Object[]{containerId, processId}) ) );
            ServiceResponse<AssociatedEntitiesDefinition> response = (ServiceResponse<AssociatedEntitiesDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public UserTaskDefinitionList getUserTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_USER_TASKS_GET_URI, valuesMap),
                    UserTaskDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getTasksDefinitions", new Object[]{containerId, processId}) ) );
            ServiceResponse<UserTaskDefinitionList> response = (ServiceResponse<UserTaskDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public TaskInputsDefinition getUserTaskInputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(TASK_NAME, taskName);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_USER_TASK_INPUT_GET_URI, valuesMap),
                    TaskInputsDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getTaskInputMappings", new Object[]{containerId, processId, taskName}) ) );
            ServiceResponse<TaskInputsDefinition> response = (ServiceResponse<TaskInputsDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public TaskOutputsDefinition getUserTaskOutputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(TASK_NAME, taskName);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_URI + "/" + PROCESS_DEF_USER_TASK_OUTPUT_GET_URI, valuesMap),
                    TaskOutputsDefinition.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DescriptorCommand( "DefinitionService", "getTaskOutputMappings", new Object[]{containerId, processId, taskName}) ) );
            ServiceResponse<TaskOutputsDefinition> response = (ServiceResponse<TaskOutputsDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return response.getResult();
        }
    }

    @Override
    public Long startProcess(String containerId, String processId) {
        return startProcess(containerId, processId, new HashMap<String, Object>());
    }

    @Override
    public Long startProcess(String containerId, String processId, Map<String, Object> variables) {
        Object result = null;

        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap), variables,
                    Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "startProcess", serialize(safeMap(variables)), marshaller.getFormat().getType(), new Object[]{containerId, processId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = deserialize(response.getResult(), Object.class);
        }

        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public Long startProcess(String containerId, String processId, CorrelationKey correlationKey) {
        return startProcess(containerId, processId, correlationKey, null);
    }

    @Override
    public Long startProcess(String containerId, String processId, CorrelationKey correlationKey, Map<String, Object> variables) {
        Object result = null;
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + START_PROCESS_WITH_CORRELATION_KEY_POST_URI, valuesMap), variables,
                    Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "startProcessWithCorrelation", serialize(safeMap(variables)), marshaller.getFormat().getType(), new Object[]{containerId, processId, correlationKey.toExternalForm()}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = deserialize(response.getResult(), Object.class);
        }

        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public void abortProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "abortProcessInstance", new Object[]{containerId, processInstanceId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void abortProcessInstances(String containerId, List<Long> processInstanceIds) {
        if( config.isRest() ) {
            String queryStr = buildQueryString("instanceId", processInstanceIds);

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + ABORT_PROCESS_INSTANCES_DEL_URI, valuesMap) + queryStr,
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "abortProcessInstances", new Object[]{containerId, processInstanceIds})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public Object getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName) {
        return getProcessInstanceVariable(containerId, processInstanceId, variableName, Object.class);
    }

    @Override
    public <T> T getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName, Class<T> type) {
        Object result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableName);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_GET_URI, valuesMap), type);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getProcessInstanceVariable",  marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, variableName}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = deserialize(response.getResult(), type);
        }

        if (result instanceof Wrapped) {
            return (T) ((Wrapped) result).unwrap();
        }

        return (T) result;
    }

    @Override
    public Map<String, Object> getProcessInstanceVariables(String containerId, Long processInstanceId) {
        Object variables = null;
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_VARS_GET_URI, valuesMap),
                    Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getProcessInstanceVariables", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            variables = deserialize(response.getResult(), Object.class);
        }

        if (variables instanceof Wrapped) {
            return (Map) ((Wrapped) variables).unwrap();
        }

        return (Map) variables;
    }

    @Override
    public void signalProcessInstance(String containerId, Long processInstanceId, String signalName, Object event) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(SIGNAL_NAME, signalName);

            Map<String, String> headers = new HashMap<String, String>();

            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + SIGNAL_PROCESS_INST_POST_URI, valuesMap), event, String.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "signalProcessInstance", serialize(event), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, signalName})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, Object event) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(SIGNAL_NAME, signalName);

            String queryStr = buildQueryString("instanceId", processInstanceIds);


            Map<String, String> headers = new HashMap<String, String>();
            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + SIGNAL_PROCESS_INSTANCES_PORT_URI, valuesMap) + queryStr
                    , event, String.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "signalProcessInstances", serialize(event), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceIds, signalName})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<String> getAvailableSignals(String containerId, Long processInstanceId) {
        Object signals = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            signals = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_SIGNALS_GET_URI, valuesMap), Object.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getAvailableSignals", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            signals = deserialize(response.getResult(), Object.class);
        }

        if (signals instanceof Wrapped) {
            return (List<String>) ((Wrapped)signals).unwrap();
        }

        return (List<String>) signals;
    }

    @Override
    public void setProcessVariable(String containerId, Long processInstanceId, String variableId, Object value) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableId);
            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_PUT_URI, valuesMap), value, String.class, getHeaders(null));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "setProcessVariable", serialize(value), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, variableId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void setProcessVariables(String containerId, Long processInstanceId, Map<String, Object> variables) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_VARS_POST_URI, valuesMap), variables,
                    String.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "setProcessVariables", serialize(variables), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public ProcessInstance getProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_GET_URI, valuesMap) , ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getProcessInstance", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, false} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return deserialize(response.getResult(), ProcessInstance.class);
        }
    }

    @Override
    public ProcessInstance getProcessInstance(String containerId, Long processInstanceId, boolean withVars) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_GET_URI, valuesMap) + "?withVars=" + withVars , ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getProcessInstance", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, withVars}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return deserialize(response.getResult(), ProcessInstance.class);
        }
    }

    @Override
    public void completeWorkItem(String containerId, Long processInstanceId, Long id, Map<String, Object> results) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI, valuesMap), results,
                    String.class, getHeaders(null));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "completeWorkItem", serialize(safeMap(results)), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, id})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void abortWorkItem(String containerId, Long processInstanceId, Long id) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI, valuesMap), null,
                    String.class, getHeaders(null));
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "abortWorkItem",  new Object[]{containerId, processInstanceId, id})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public WorkItemInstance getWorkItem(String containerId, Long processInstanceId, Long id) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI, valuesMap), WorkItemInstance.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getWorkItem", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, id}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return deserialize(response.getResult(), WorkItemInstance.class);
        }
    }

    @Override
    public List<WorkItemInstance> getWorkItemByProcessInstance(String containerId, Long processInstanceId) {
        WorkItemInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI, valuesMap), WorkItemInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessService", "getWorkItemByProcessInstance", marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            list = deserialize(response.getResult(), WorkItemInstanceList.class);
        }

        if (list != null && list.getWorkItems() != null) {
            return Arrays.asList(list.getWorkItems());
        }

        return Collections.emptyList();
    }
}
