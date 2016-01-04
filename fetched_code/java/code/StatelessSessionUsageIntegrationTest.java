/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.integrationtests.drools;

import static org.junit.Assert.*;

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

public class StatelessSessionUsageIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateless-kjar1";
    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String PERSON_OUT_IDENTIFIER = "person1";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testStatelessCall() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

        ServiceResponse<String> reply = ruleClient.executeCommands(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        String result = reply.getResult();
        ExecutionResults actualData = marshaller.unmarshall(result, ExecutionResultImpl.class);
        Object value = actualData.getValue(PERSON_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'. Got response: " + result,
                PERSON_EXPECTED_SURNAME, valueOf(value, PERSON_SURNAME_FIELD));
    }

}
