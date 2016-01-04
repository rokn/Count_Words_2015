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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;


public class UserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    @Category(Smoke.class)
    public void testProcessWithUserTasks() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("mary", valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testReleaseAndClaim() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.releaseTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Ready", taskSummary.getStatus());

            taskClient.claimTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testStartAndStop() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("InProgress", taskSummary.getStatus());

            taskClient.stopTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSuspendAndResume() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.suspendTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            if (taskList.size() > 0) {
                fail("Should not be any tasks for yoda as potential owner");
            }

            taskClient.resumeTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFailUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.failTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSkipUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertTrue(taskSummary.getSkipable().booleanValue());

            taskClient.skipTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskContentOperations() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
         ;
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // first verify task input
            Map<String, Object> taskInput = taskClient.getTaskInputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(5, taskInput.size());
            assertTrue(taskInput.containsKey("_string"));
            assertTrue(taskInput.containsKey("_person"));

            assertEquals("john is working on it", taskInput.get("_string"));
            assertEquals("john", valueOf(taskInput.get("_person"), "name"));

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            Long outputContentId = taskClient.saveTaskContent("definition-project", taskSummary.getId(), taskOutcome);

            taskInput = taskClient.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(2, taskInput.size());
            assertTrue(taskInput.containsKey("string_"));
            assertTrue(taskInput.containsKey("person_"));

            assertEquals("my custom data", taskInput.get("string_"));
            assertEquals("mary", valueOf(taskInput.get("person_"), "name"));

            // let's delete the content as we won't need it
            taskClient.deleteTaskContent("definition-project", taskSummary.getId(), outputContentId);

            taskInput = taskClient.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(0, taskInput.size());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcomeComplete);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("peter", valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data 2", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId());
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertNullOrEmpty(taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(taskSummary.getId(), taskInstance.getId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(true, taskInstance.getSkipable());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            assertNotNull(taskInstance.getWorkItemId());
            assertTrue(taskInstance.getWorkItemId().longValue() > 0);

            assertNull(taskInstance.getExcludedOwners());
            assertNull(taskInstance.getPotentialOwners());
            assertNull(taskInstance.getBusinessAdmins());
            assertNull(taskInstance.getInputData());
            assertNull(taskInstance.getOutputData());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskByIdWithDetails() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId(), true, true, true);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertNullOrEmpty(taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(taskSummary.getId(), taskInstance.getId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(true, taskInstance.getSkipable());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            assertNotNull(taskInstance.getWorkItemId());
            assertTrue(taskInstance.getWorkItemId().longValue() > 0);

            assertNotNull(taskInstance.getExcludedOwners());
            assertEquals(0, taskInstance.getExcludedOwners().size());
            assertNotNull(taskInstance.getPotentialOwners());
            assertEquals(1, taskInstance.getPotentialOwners().size());
            assertTrue(taskInstance.getPotentialOwners().contains("yoda"));

            assertNotNull(taskInstance.getBusinessAdmins());
            assertEquals(2, taskInstance.getBusinessAdmins().size());
            assertTrue(taskInstance.getBusinessAdmins().contains("Administrator"));
            assertTrue(taskInstance.getBusinessAdmins().contains("Administrators"));

            assertNotNull(taskInstance.getInputData());
            assertEquals(5, taskInstance.getInputData().size());

            Map<String, Object> inputs = taskInstance.getInputData();
            assertTrue(inputs.containsKey("ActorId"));
            assertTrue(inputs.containsKey("_string"));
            assertTrue(inputs.containsKey("Skippable"));
            assertTrue(inputs.containsKey("_person"));
            assertTrue(inputs.containsKey("NodeName"));

            assertEquals("yoda", inputs.get("ActorId"));
            assertEquals("john is working on it", inputs.get("_string"));
            assertEquals("true", inputs.get("Skippable"));
            assertEquals("First task", inputs.get("NodeName"));
            assertEquals("john", valueOf(inputs.get("_person"), "name"));

            assertNotNull(taskInstance.getOutputData());
            assertEquals(0, taskInstance.getOutputData().size());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    /**
     * Test verifying forwardTask method and its functionality.
     * Also check task status changes when forwarding task.
     *
     * @throws Exception
     */
    @Test
    public void testForwardUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // forwarding Reserved task to john (forwarding Reserved -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "yoda", "john");

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // forwarded task is set to Ready state and is assigned to john as potential owner
            changeUser("john");
            taskList = taskClient.findTasksAssignedAsPotentialOwner("john", 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "john");

            // forwarding task in Ready state back to yoda (forwarding Ready -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "john", "yoda");

            // forwarded task stays in Ready state and is assigned to yoda as potential owner
            changeUser("yoda");
            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "yoda");

            // starting task to change its status to In progress
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.InProgress, "yoda");

            // forwarding task In progress back to john (forwarding In progress -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "yoda", "john");

            // forwarded task change state to Ready and is assigned to john as potential owner
            changeUser("john");
            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "john");
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    /**
     * Test verifying delegateTask method and its functionality.
     * Also check task status changes when delegating task.
     *
     * @throws Exception
     */
    @Test
    public void testDelegateUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // delegating Reserved task to john (delegating Reserved -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "yoda", "john");

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // delegated task stays in Reserved state and is assigned to john as actual owner
            changeUser("john");
            taskList = taskClient.findTasksAssignedAsPotentialOwner("john", 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "john");

            // releasing task to change its state to Ready
            taskClient.releaseTask("definition-project", taskSummary.getId(), "john");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Ready, "");

            // delegating task in Ready state back to yoda (delegating Ready -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "john", "yoda");

            // delegated task change its state to Reserved and is assigned to yoda as actual owner
            changeUser("yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // starting task to change its status to In progress
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.InProgress, "yoda");

            // delegating task In progress back to john (delegating In progress -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "yoda", "john");

            // delegated task change state to Reserved and is assigned to john as actual owner
            changeUser("john");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "john");
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testUserTaskSetTaskProperties() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // verify current task properties
            assertEquals(0, taskSummary.getPriority().intValue());
            assertNull(taskSummary.getExpirationTime());
            assertTrue(taskSummary.getSkipable().booleanValue());
            assertEquals("First task", taskSummary.getName());
            assertNullOrEmpty(taskSummary.getDescription());

            // set task properties
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            Date expirationDate = currentTime.getTime();
            taskClient.setTaskDescription("definition-project", taskSummary.getId(), "Simple user task.");
            taskClient.setTaskExpirationDate("definition-project", taskSummary.getId(), expirationDate);
            taskClient.setTaskName("definition-project", taskSummary.getId(), "Modified name");
            taskClient.setTaskPriority("definition-project", taskSummary.getId(), 10);
            taskClient.setTaskSkipable("definition-project", taskSummary.getId(), false);

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId());

            // verify modified task properties
            assertEquals(10, taskInstance.getPriority().intValue());
            assertNotNull(taskInstance.getExpirationDate());
            assertFalse(taskInstance.getSkipable().booleanValue());
            assertEquals("Modified name", taskInstance.getName());
            assertEquals("Simple user task.", taskInstance.getDescription());
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskComments() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // Adding comment to user task as yoda.
            String firstComment = "First comment.";
            Calendar firstCommentTime = Calendar.getInstance();
            Long firstCommentId = taskClient.addTaskComment(CONTAINER_ID, taskSummary.getId(), firstComment, USER_YODA, firstCommentTime.getTime());

            // Adding second comment to user task as john.
            String secondComment = "Second comment.";
            Calendar secondCommentTime = Calendar.getInstance();
            secondCommentTime.add(Calendar.MINUTE, 5);
            Long secondCommentId = taskClient.addTaskComment(CONTAINER_ID, taskSummary.getId(), secondComment, USER_JOHN, secondCommentTime.getTime());

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying first comment returned by getTaskCommentById().
            TaskComment firstTaskComment = taskClient.getTaskCommentById(CONTAINER_ID, taskSummary.getId(), firstCommentId);
            assertNotNull(firstTaskComment.getAddedAt());
            assertEquals(USER_YODA, firstTaskComment.getAddedBy());
            assertEquals(firstCommentId, firstTaskComment.getId());
            assertEquals(firstComment, firstTaskComment.getText());

            // Verifying second comment returned by getTaskCommentsByTaskId().
            List<TaskComment> taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(2, taskComments.size());

            TaskComment secondTaskComment = null;
            if (secondCommentId.equals(taskComments.get(0).getId())) {
                secondTaskComment = taskComments.get(0);
            } else {
                secondTaskComment = taskComments.get(1);
            }
            assertNotNull(secondTaskComment.getAddedAt());
            assertEquals(USER_JOHN, secondTaskComment.getAddedBy());
            assertEquals(secondCommentId, secondTaskComment.getId());
            assertEquals(secondComment, secondTaskComment.getText());

            // Delete task comment.
            taskClient.deleteTaskComment(CONTAINER_ID, taskSummary.getId(), secondCommentId);

            // Now there is just one comment left.
            taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(1, taskComments.size());
            assertEquals(firstCommentId, taskComments.get(0).getId());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskAttachments() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            String attachment1Name = "First attachment";
            String attachment2Name = "Second attachment";

            // Adding attachments to user task.
            Object firstAttachmentContent = createPersonInstance("mary");
            Long firstAttachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_YODA, attachment1Name, firstAttachmentContent);

            changeUser(USER_JOHN);
            String secondAttachmentContent = "This is second attachment.";
            Long secondAttachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_JOHN, attachment2Name, secondAttachmentContent);

            changeUser(USER_YODA);
            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying first attachment returned by getTaskAttachmentById().
            TaskAttachment firstTaskAttachment = taskClient.getTaskAttachmentById(CONTAINER_ID, taskSummary.getId(), firstAttachmentId);
            assertNotNull(firstTaskAttachment.getAddedAt());
            assertEquals(USER_YODA, firstTaskAttachment.getAddedBy());
            assertNotNull(firstTaskAttachment.getAttachmentContentId());
            assertEquals(firstAttachmentContent.getClass().getName(), firstTaskAttachment.getContentType());
            assertEquals(firstAttachmentId, firstTaskAttachment.getId());
            assertNotNull(firstTaskAttachment.getName());
            assertEquals(attachment1Name, firstTaskAttachment.getName());
            assertNotNull(firstTaskAttachment.getSize());

            // Verifying second attachment returned by getTaskAttachmentsByTaskId().
            List<TaskAttachment> taskAttachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(2, taskAttachments.size());

            TaskAttachment secondTaskAttachment = null;
            if (secondAttachmentId.equals(taskAttachments.get(0).getId())) {
                secondTaskAttachment = taskAttachments.get(0);
            } else {
                secondTaskAttachment = taskAttachments.get(1);
            }
            assertNotNull(secondTaskAttachment.getAddedAt());
            assertEquals(USER_JOHN, secondTaskAttachment.getAddedBy());
            assertNotNull(secondTaskAttachment.getAttachmentContentId());
            assertEquals(String.class.getName(), secondTaskAttachment.getContentType());
            assertEquals(secondAttachmentId, secondTaskAttachment.getId());
            assertNotNull(secondTaskAttachment.getName());
            assertEquals(attachment2Name, secondTaskAttachment.getName());
            assertNotNull(secondTaskAttachment.getSize());

            // Verifying second attachment content returned by getTaskAttachmentContentById().
            Object taskAttachmentContent = taskClient.getTaskAttachmentContentById(CONTAINER_ID, taskSummary.getId(), secondAttachmentId);
            assertEquals(secondAttachmentContent, taskAttachmentContent);

            // Delete task attachment.
            taskClient.deleteTaskAttachment(CONTAINER_ID, taskSummary.getId(), firstAttachmentId);

            // Now there is just one attachment left.
            taskAttachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(1, taskAttachments.size());
            assertEquals(secondAttachmentId, taskAttachments.get(0).getId());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskAttachmentsAsByteArray() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // Adding attachments to user task.
            byte[] attachmentContent = new String("This is first attachment.").getBytes();
            Long attachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_YODA, "my attachment", attachmentContent);

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying attachment returned by getTaskAttachmentById().
            Object taskAttachmentContent = taskClient.getTaskAttachmentContentById(CONTAINER_ID, taskSummary.getId(), attachmentId);
            assertArrayEquals(attachmentContent, (byte[])taskAttachmentContent);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testExitUserTask() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals(Status.Reserved.toString(), taskSummary.getStatus());

            // exit task
            changeUser(USER_ADMINISTRATOR);
            taskClient.exitTask(CONTAINER_ID, taskSummary.getId(), USER_ADMINISTRATOR);
            changeUser(USER_YODA);

            TaskInstance task = taskClient.findTaskById(taskSummary.getId());
            assertNotNull(task);
            assertEquals(taskSummary.getId(), task.getId());
            assertEquals(Status.Exited.toString(), task.getStatus());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testGroupUserTask() throws Exception {
        String taskName = "Group task";

        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_GROUPTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals(taskName, taskSummary.getName());
            assertEquals(Status.Ready.toString(), taskSummary.getStatus());

            // User yoda isn't in group "engineering", can't claim task.
            try {
                taskClient.claimTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);
                fail("User yoda shouldn't be able to claim task as he doesn't belong to group which task is assigned to.");
            } catch (KieServicesException e) {
                // expected
            }

            // User john is in group "engineering", can work with task.
            changeUser(USER_JOHN);
            taskClient.claimTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN);
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN);

            taskList = taskClient.findTasksOwned(USER_JOHN, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals(taskName, taskSummary.getName());
            assertEquals(Status.InProgress.toString(), taskSummary.getStatus());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN, taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertNullOrEmpty("Found task to be processed!", taskList);

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testFindTasksByVariable() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskList = taskClient.findTasksByVariable("yoda", "_string", null, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.saveTaskContent("definition-project", taskSummary.getId(), taskOutcomeComplete);

            List<String> inprogressTasks = new ArrayList<String>();
            inprogressTasks.add(Status.InProgress.toString());

            taskList = taskClient.findTasksByVariable("yoda", "string_", inprogressTasks, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksByVariableAndValue() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskList = taskClient.findTasksByVariableAndValue("yoda", "_string", "john is working on it", null, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.saveTaskContent("definition-project", taskSummary.getId(), taskOutcomeComplete);

            List<String> inprogressTasks = new ArrayList<String>();
            inprogressTasks.add(Status.InProgress.toString());

            taskList = taskClient.findTasksByVariableAndValue("yoda", "string_", "my*", inprogressTasks, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }


    private void checkTaskStatusAndOwners(String containerId, Long taskId, Status status, String actualOwner, String potentialOwner) {
        TaskInstance task = taskClient.getTaskInstance(containerId, taskId, false, false, true);
        checkTaskStatusAndActualOwner(containerId, taskId, status, actualOwner);
        assertEquals(1, task.getPotentialOwners().size());
        assertEquals(potentialOwner, task.getPotentialOwners().get(0));
    }

    private void checkTaskStatusAndActualOwner(String containerId, Long taskId, Status status, String actualOwner) {
        TaskInstance task = taskClient.getTaskInstance(containerId, taskId);
        assertEquals(taskId, task.getId());
        assertEquals(status.toString(), task.getStatus());
        assertEquals(actualOwner, task.getActualOwner());
    }
}
