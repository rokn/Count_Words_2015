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

package org.jbpm.persistence.processinstance;

import static org.jbpm.persistence.util.PersistenceUtil.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.persistence.util.LoggingPrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class ParameterMappingTest {
    
    private HashMap<String, Object> context;
    
    private static final String PROCESS_ID = "org.jbpm.processinstance.subprocess";
    private static final String SUBPROCESS_ID = "org.jbpm.processinstance.helloworld";
    private StatefulKnowledgeSession ksession;
    private ProcessListener listener;

    // Want to see the System.out output? Set the debug level for console in log4j.xml to DEBUG.
    static { 
        System.setOut(new LoggingPrintStream(System.out));
    }
    
    @Before
    public void before() {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME, false);
        Environment env = createEnvironment(context);

        ksession = InfinispanKnowledgeService.newStatefulKnowledgeSession(createKnowledgeBase(), null, env);
        assertTrue("Valid KnowledgeSession could not be created.", ksession != null && ksession.getIdentifier() > 0);

        listener = new ProcessListener();
        ksession.addEventListener(listener);
    }

    private KnowledgeBase createKnowledgeBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("processinstance/Subprocess.rf"), ResourceType.DRF);
        kbuilder.add(ResourceFactory.newClassPathResource("processinstance/HelloWorld.rf"), ResourceType.DRF);
    
        return kbuilder.newKnowledgeBase();
    }

    @After
    public void after() {
        if (ksession != null) {
            ksession.dispose();
        }
        cleanUp(context);
    }

    //org.jbpm.processinstance.subprocess
    @Test
    public void testChangingVariableByScript() throws Exception {
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "script");
        mapping.put("var", "value");

        ksession.startProcess(PROCESS_ID, mapping);

        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testChangingVariableByEvent() throws Exception {
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "event");
        mapping.put("var", "value");

        ksession.startProcess(PROCESS_ID, mapping).getId();
        ksession.signalEvent("pass", "new value");

        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testChangingVariableByEventSignalWithProcessId() throws Exception {
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "event");
        mapping.put("var", "value");

        long processId = ksession.startProcess(PROCESS_ID, mapping).getId();
        ksession.signalEvent("pass", "new value", processId);

        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testNotChangingVariable() throws Exception {
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "default");
        mapping.put("var", "value");

        ksession.startProcess(PROCESS_ID, mapping);

        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testNotSettingVariable() throws Exception {
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "default");

        ksession.startProcess(PROCESS_ID, mapping);

        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    
    public static class ProcessListener extends DefaultProcessEventListener {
        private final List<String> processesStarted = new ArrayList<String>();
        private final List<String> processesCompleted = new ArrayList<String>();

        public void afterProcessStarted(ProcessStartedEvent event) {
            processesStarted.add(event.getProcessInstance().getProcessId());
        }

        public void afterProcessCompleted(ProcessCompletedEvent event) {
            processesCompleted.add(event.getProcessInstance().getProcessId());
        }

        public boolean isProcessStarted(String processId) {
            return processesStarted.contains(processId);
        }

        public boolean isProcessCompleted(String processId) {
            return processesCompleted.contains(processId);
        }
    }
}
