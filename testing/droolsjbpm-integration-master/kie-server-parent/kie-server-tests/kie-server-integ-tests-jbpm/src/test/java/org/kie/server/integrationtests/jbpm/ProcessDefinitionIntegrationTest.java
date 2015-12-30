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

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.client.KieServicesException;

import static org.junit.Assert.*;


public class ProcessDefinitionIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }


    @Test
    public void testEvaluationProcessDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        ProcessDefinition result = processClient.getProcessDefinition("definition-project", "definition-project.evaluation");

        assertNotNull(result);
        assertEquals("definition-project.evaluation", result.getId());
        assertEquals("evaluation", result.getName());
        assertEquals("org.jbpm", result.getPackageName());
        assertEquals("1.0", result.getVersion());
        assertEquals("definition-project", result.getContainerId());

        // assert variable definitions
        Map<String, String> variables = result.getProcessVariables();
        assertNotNull(variables);
        assertEquals(3, variables.size());

        assertTrue(variables.containsKey("name"));
        assertTrue(variables.containsKey("item"));
        assertTrue(variables.containsKey("outcome"));

        assertEquals("String", variables.get("name"));
        assertEquals("java.util.List", variables.get("item"));
        assertEquals("Boolean", variables.get("outcome"));

        // assert associated entities - users and groups
        Map<String, String[]> entities = result.getAssociatedEntities();
        assertNotNull(entities);

        assertTrue(entities.containsKey("Evaluate items"));

        String[] evaluateItemsEntities = entities.get("Evaluate items");
        assertEquals(2, evaluateItemsEntities.length);
        assertEquals("john", evaluateItemsEntities[0]);
        assertEquals("HR,PM", evaluateItemsEntities[1]);

        // assert reusable subprocesses
        assertEquals(0, result.getReusableSubProcesses().size());

        // assert services tasks
        assertEquals(1, result.getServiceTasks().size());
        assertTrue(result.getServiceTasks().containsKey("Email results"));
        // assert type of the services task for 'Email results' name
        assertEquals("Email", result.getServiceTasks().get("Email results"));

    }

    @Test
    public void testCallEvaluationProcessDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        ProcessDefinition result = processClient.getProcessDefinition("definition-project", "definition-project.call-evaluation");

        assertNotNull(result);
        assertEquals("definition-project.call-evaluation", result.getId());
        assertEquals("call-evaluation", result.getName());
        assertEquals("org.jbpm", result.getPackageName());
        assertEquals("1.0", result.getVersion());
        assertEquals("definition-project", result.getContainerId());

        // assert variable definitions
        Map<String, String> variables = result.getProcessVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());

        assertTrue(variables.containsKey("items"));

        assertEquals("java.util.List", variables.get("items"));

        // assert associated entities - users and groups
        Map<String, String[]> entities = result.getAssociatedEntities();
        assertNotNull(entities);

        assertTrue(entities.containsKey("Prepare"));

        String[] evaluateItemsEntities = entities.get("Prepare");
        assertEquals(1, evaluateItemsEntities.length);
        assertEquals("john", evaluateItemsEntities[0]);

        // assert reusable subprocesses
        assertEquals(1, result.getReusableSubProcesses().size());
        assertEquals("definition-project.evaluation", result.getReusableSubProcesses().iterator().next());

        // assert services tasks
        assertEquals(0, result.getServiceTasks().size());

    }


    @Test(expected = KieServicesException.class)
    public void testNonExistingProcessDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        processClient.getProcessDefinition("definition-project", "non-existing-process");

    }


    @Test
    public void testReusableSubProcessDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        SubProcessesDefinition result = processClient.getReusableSubProcessDefinitions("definition-project", "definition-project.call-evaluation");

        assertNotNull(result);
        // assert reusable subprocesses
        assertEquals(1, result.getSubProcesses().size());
        assertEquals("definition-project.evaluation", result.getSubProcesses().iterator().next());

    }


    @Test
    public void testProcessVariableDefinitions() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        // assert variable definitions
        VariablesDefinition variablesDefinition = processClient.getProcessVariableDefinitions("definition-project", "definition-project.evaluation");

        Map<String, String> variables = variablesDefinition.getVariables();
        assertNotNull(variables);
        assertEquals(3, variables.size());

        assertTrue(variables.containsKey("name"));
        assertTrue(variables.containsKey("item"));
        assertTrue(variables.containsKey("outcome"));

        assertEquals("String", variables.get("name"));
        assertEquals("java.util.List", variables.get("item"));
        assertEquals("Boolean", variables.get("outcome"));
    }

    @Test
    public void testServiceTasksDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        ServiceTasksDefinition result = processClient.getServiceTaskDefinitions("definition-project", "definition-project.evaluation");
        // assert services tasks
        assertEquals(1, result.getServiceTasks().size());
        assertTrue(result.getServiceTasks().containsKey("Email results"));
        // assert type of the services task for 'Email results' name
        assertEquals("Email", result.getServiceTasks().get("Email results"));
    }


    @Test
    public void testAssociatedEntitiesDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        AssociatedEntitiesDefinition result = processClient.getAssociatedEntityDefinitions("definition-project", "definition-project.evaluation");

        // assert associated entities - users and groups
        Map<String, String[]> entities = result.getAssociatedEntities();
        assertNotNull(entities);

        assertTrue(entities.containsKey("Evaluate items"));
        String[] evaluateItemsEntities = entities.get("Evaluate items");

        assertEquals(2, evaluateItemsEntities.length);
        assertEquals("john", evaluateItemsEntities[0]);
        assertEquals("HR,PM", evaluateItemsEntities[1]);
    }

    @Test
    public void testUserTasksDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        UserTaskDefinitionList result = processClient.getUserTaskDefinitions("definition-project", "definition-project.evaluation");

        assertNotNull(result);
        UserTaskDefinition[] tasks = result.getTasks();

        // assert user tasks
        assertNotNull(tasks);
        assertEquals(1, tasks.length);

        UserTaskDefinition task = tasks[0];

        assertNotNull(task);
        assertEquals("Evaluate items", task.getName());
        assertNull(task.getComment());
        assertNull(task.getCreatedBy());
        assertEquals(0, task.getPriority().intValue());
        assertEquals(false, task.isSkippable());

        // assert associated entities - users and groups
        String[] evaluateItemsEntities = task.getAssociatedEntities();

        assertEquals(2, evaluateItemsEntities.length);
        assertEquals("john", evaluateItemsEntities[0]);
        assertEquals("HR,PM", evaluateItemsEntities[1]);

        // assert task inputs and outputs

        Map<String, String> inputs = task.getTaskInputMappings();
        assertNotNull(inputs);
        assertEquals(4, inputs.size());

        assertTrue(inputs.containsKey("name_in"));
        assertTrue(inputs.containsKey("list_in"));
        assertTrue(inputs.containsKey("GroupId"));
        assertTrue(inputs.containsKey("Skippable"));


        assertEquals("String", inputs.get("name_in"));
        assertEquals("java.util.List", inputs.get("list_in"));
        assertEquals("java.lang.String", inputs.get("GroupId"));
        assertEquals("java.lang.String", inputs.get("Skippable"));

        Map<String, String> outputs = task.getTaskOutputMappings();
        assertNotNull(outputs);
        assertEquals(1, outputs.size());

        assertTrue(outputs.containsKey("outcome"));

        assertEquals("Boolean", outputs.get("outcome"));

    }

    @Test
    public void testUserTaskInputDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        TaskInputsDefinition result = processClient.getUserTaskInputDefinitions("definition-project", "definition-project.evaluation", "Evaluate items");

        assertNotNull(result);
        // assert task inputs and outputs

        Map<String, String> inputs = result.getTaskInputs();
        assertNotNull(inputs);
        assertEquals(4, inputs.size());

        assertTrue(inputs.containsKey("name_in"));
        assertTrue(inputs.containsKey("list_in"));
        assertTrue(inputs.containsKey("GroupId"));
        assertTrue(inputs.containsKey("Skippable"));


        assertEquals("String", inputs.get("name_in"));
        assertEquals("java.util.List", inputs.get("list_in"));
        assertEquals("java.lang.String", inputs.get("GroupId"));
        assertEquals("java.lang.String", inputs.get("Skippable"));
    }

    @Test
    public void testTaskOutputsDefinition() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        TaskOutputsDefinition result = processClient.getUserTaskOutputDefinitions("definition-project", "definition-project.evaluation", "Evaluate items");

        assertNotNull(result);
        // assert task inputs and outputs
        Map<String, String> outputs = result.getTaskOutputs();
        assertNotNull(outputs);
        assertEquals(1, outputs.size());

        assertTrue(outputs.containsKey("outcome"));

        assertEquals("Boolean", outputs.get("outcome"));

    }

}
