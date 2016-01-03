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

package org.kie.server.integrationtests.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;


public class DeploymentDescriptorIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "deployment-descriptor-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "deployment-descriptor-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";
    private static final String PERSON_NAME_FIELD = "name";
    private static final String GLOBAL_PERSON_IDENTIFIER = "person";
    private static final String GLOBAL_PERSON_NAME = "Bob";


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/deployment-descriptor-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testGlobalVariableFromDeploymentDescriptor() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), marshallingFormat, kieContainer.getClassLoader());

        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID);

        // retrieve global variable set in deployment descriptor
        commands.add(commandsFactory.newGetGlobal(GLOBAL_PERSON_IDENTIFIER));

        ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResultImpl actualData = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        assertNotNull(actualData);
        Object personVar = actualData.getResults().get(GLOBAL_PERSON_IDENTIFIER);
        assertNotNull(personVar);
        assertEquals(GLOBAL_PERSON_NAME, valueOf(personVar, PERSON_NAME_FIELD));
    }

    @Test
    public void testPerRequestRuntimeStrategy() throws Exception {
        String personOutIdentifier = "personOut";
        String personName = "yoda";

        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), marshallingFormat, kieContainer.getClassLoader());

        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID);

        // insert person object to working memory
        Object createPersonInstance = createPersonInstance(personName);
        commands.add(commandsFactory.newInsert(createPersonInstance, personOutIdentifier));
        commands.add(commandsFactory.newGetObjects(personOutIdentifier));
        ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        assertNotNull(actualData);
        ArrayList<Object> personVar = (ArrayList<Object>) actualData.getValue(personOutIdentifier);
        assertEquals(1, personVar.size());
        assertEquals(personName, valueOf(personVar.get(0), PERSON_NAME_FIELD));

        // try to retrieve person object by new request
        commands.clear();
        commands.add(commandsFactory.newGetObjects(personOutIdentifier));
        reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        actualData = marshaller.unmarshall(reply.getResult(), ExecutionResultImpl.class);
        assertNotNull(actualData);
        personVar = (ArrayList<Object>) actualData.getValue(personOutIdentifier);
        assertNullOrEmpty("Person object was returned!", personVar);
    }
}
