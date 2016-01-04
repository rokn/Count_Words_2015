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

package org.jbpm.persistence.session;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.ClockType;
import org.drools.core.SessionConfiguration;
import org.drools.core.audit.WorkingMemoryLogger;
import org.drools.core.audit.event.LogEvent;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.RegisterWorkItemHandlerCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.WorkImpl;
import org.drools.core.time.SessionPseudoClock;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.persistence.infinispan.InfinispanTimeJobFactoryManager;
import org.drools.persistence.infinispan.ManualPersistInterceptor;
import org.drools.persistence.infinispan.processinstance.InfinispanWorkItemManagerFactory;
import org.drools.persistence.jpa.JpaJDKTimerService;
import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.compiler.ProcessBuilderImpl;
import org.jbpm.persistence.InfinispanProcessPersistenceContextManager;
import org.jbpm.persistence.ManualPersistProcessInterceptor;
import org.jbpm.persistence.processinstance.InfinispanProcessInstanceManagerFactory;
import org.jbpm.persistence.processinstance.InfinispanSignalManagerFactory;
import org.jbpm.persistence.session.objects.TestWorkItemHandler;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.event.KnowledgeRuntimeEventManager;
import org.kie.internal.logger.KnowledgeRuntimeLoggerFactory;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.jbpm.persistence.util.PersistenceUtil.*;
import static org.junit.Assert.*;

public class SingleSessionCommandServiceTest {

	private HashMap<String, Object> context;
	private Environment env;
    
	@Before
    public void setUp() {
        String testMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        if( testMethodName.startsWith("testPersistenceTimer") ) { 
            context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME, false);
        }
        else { 
            context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
        }
        env = createEnvironment(context);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new InfinispanProcessPersistenceContextManager(env));
        env.set(EnvironmentName.TRANSACTION_MANAGER, 
        		new JtaTransactionManager(env.get(EnvironmentName.TRANSACTION), 
        				env.get(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY), 
        				env.get(EnvironmentName.TRANSACTION_MANAGER)
        		)
        );
        
    }

    @After
    public void tearDown() {
    	try {
    		cleanUp(context);
    	} catch (RuntimeException e) { /* ignore */ }
    }

    @Test
    public void testPersistenceWorkItems() throws Exception {
        setUp();
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                InfinispanProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                InfinispanWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                InfinispanSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
                                JpaJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );

        SingleSessionCommandService service = createSingleSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        long sessionId = service.getSessionId();

        registerWorkItemHandlers(service);
        
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        System.out.println( "Started process instance " + processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        registerWorkItemHandlers(service);
        
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
        service.dispose();
    }
    
    
    
    @Test
    public void testPersistenceWorkItemsUserTransaction() throws Exception {
        setUp();
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                InfinispanProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                InfinispanWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                InfinispanSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
                                JpaJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );

        SingleSessionCommandService service = createSingleSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        long sessionId = service.getSessionId();

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        System.out.println( "Started process instance " + processInstance.getId() );
        ut.commit();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        ut.commit();
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNull( processInstance );
        service.dispose();
    }

	private Collection<KnowledgePackage> getProcessWorkItems() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode2 = new WorkItemNode();
        workItemNode2.setId( 4 );
        workItemNode2.setName( "WorkItem2" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode2.setWork( work );
        process.addNode( workItemNode2 );
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode3 = new WorkItemNode();
        workItemNode3.setId( 5 );
        workItemNode3.setName( "WorkItem3" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode3.setWork( work );
        process.addNode( workItemNode3 );
        new ConnectionImpl( workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( packageBuilder.getPackage() );
        return list;
    }

    @Test
    public void testPersistenceSubProcess() {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                InfinispanProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                InfinispanWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                InfinispanSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
                                JpaJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );

        KnowledgeBase ruleBase = KnowledgeBaseFactory.newKnowledgeBase();
        InternalKnowledgePackage pkg = getProcessSubProcess();
        ((KnowledgeBaseImpl)ruleBase).addPackage( pkg );

        SingleSessionCommandService service = createSingleSessionCommandService( ruleBase,
                                                                               config,
                                                                               env );
        long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) service.execute( startProcessCommand );
        System.out.println( "Started process instance " + processInstance.getId() );
        long processInstanceId = processInstance.getId();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
        		                                   ruleBase,
                                                   config,
                                                   env );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );

        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        assertEquals( 1,
                      nodeInstances.size() );
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        long subProcessInstanceId = subProcessNodeInstance.getProcessInstanceId();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNotNull( subProcessInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   ruleBase,
                                                   config,
                                                   env );
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   ruleBase,
                                                   config,
                                                   env );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNull( subProcessInstance );

        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
        service.dispose();
    }

	private InternalKnowledgePackage getProcessSubProcess() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        
        SubProcessNode subProcessNode = new SubProcessNode();
        subProcessNode.setId( 3 );
        subProcessNode.setName( "SubProcess" );
        subProcessNode.setProcessId( "org.drools.test.SubProcess" );
        process.addNode( subProcessNode );
        
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        
        EndNode end = new EndNode();
        end.setId( 4 );
        end.setName( "End" );
        process.addNode( end );
        
        new ConnectionImpl( subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );

        process = new RuleFlowProcess();
        process.setId( "org.drools.test.SubProcess" );
        process.setName( "SubProcess" );
        process.setPackageName( "org.drools.test" );
        
        start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        
        actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        
        action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        
        end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        processBuilder.buildProcess( process,
                                     null );
        return packageBuilder.getPackage();
    }

    @Test @Ignore
    public void testPersistenceTimer() throws Exception {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                InfinispanProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                InfinispanWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                InfinispanSignalManagerFactory.class.getName() );
        
        SessionConfiguration config = SessionConfiguration.newInstance( properties );
        java.lang.reflect.Field timeManagerField = SessionConfiguration.class.getDeclaredField("timerJobFactoryManager");
        timeManagerField.setAccessible(true);
        timeManagerField.set(config, new InfinispanTimeJobFactoryManager());

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = createSingleSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        System.out.println( "Started process instance " + processInstance.getId() );
        Thread.sleep(500);
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        Thread.sleep(5000);
        
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
    }

	private List<KnowledgePackage> getProcessTimer() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "2000" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( packageBuilder.getPackage() );
        return list;
    }

    @Test @Ignore
    public void testPersistenceTimer2() throws Exception {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                InfinispanProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                InfinispanWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                InfinispanSignalManagerFactory.class.getName() );

        SessionConfiguration config = SessionConfiguration.newInstance( properties );
        config.setOption( ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()) );
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer2();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = createSingleSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        System.out.println( "Started process instance " + processInstance.getId() );

        SessionPseudoClock clock = service.getKieSession().getSessionClock();
        clock.advanceTime(2000, TimeUnit.MILLISECONDS);

        service = createSingleSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
    }

	private List<KnowledgePackage> getProcessTimer2() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "0" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "try { Thread.sleep(1000); } catch (Throwable t) {} System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( packageBuilder.getPackage() );
        return list;
    }

	private SingleSessionCommandService createSingleSessionCommandService(KnowledgeBase kbase, SessionConfiguration conf, Environment env) {
		SingleSessionCommandService service = new SingleSessionCommandService(kbase, conf, env);
		service.addInterceptor(new ManualPersistInterceptor(service));
		service.addInterceptor(new ManualPersistProcessInterceptor(service));
		KnowledgeRuntimeLoggerFactory.newConsoleLogger((KnowledgeRuntimeEventManager) service.getKieSession());
		return service;
	}

	private SingleSessionCommandService createSingleSessionCommandService(long sessionId, KnowledgeBase kbase, SessionConfiguration conf, Environment env) {
		SingleSessionCommandService service = new SingleSessionCommandService(sessionId, kbase, conf, env);
		service.addInterceptor(new ManualPersistInterceptor(service));
		service.addInterceptor(new ManualPersistProcessInterceptor(service));
		new KnowledgeLogger(service.getKieSession());
		return service;
	}

	private SingleSessionCommandService createSingleSessionCommandService(KieBase ruleBase, SessionConfiguration conf, Environment env) {
		SingleSessionCommandService service = new SingleSessionCommandService(ruleBase, conf, env);
		service.addInterceptor(new ManualPersistInterceptor(service));
		service.addInterceptor(new ManualPersistProcessInterceptor(service));
		new KnowledgeLogger(service.getKieSession());
		return service;
	}

	private SingleSessionCommandService createSingleSessionCommandService(long sessionId, KieBase ruleBase, SessionConfiguration conf, Environment env) {
		SingleSessionCommandService service = new SingleSessionCommandService(sessionId, ruleBase, conf, env);
		service.addInterceptor(new ManualPersistInterceptor(service));
		service.addInterceptor(new ManualPersistProcessInterceptor(service));
		new KnowledgeLogger(service.getKieSession());
		return service;
	}
	
	private void registerWorkItemHandlers(SingleSessionCommandService service) {
		RegisterWorkItemHandlerCommand registerCommand1 = new RegisterWorkItemHandlerCommand();
		registerCommand1.setWorkItemName("MyWork");
        registerCommand1.setHandler(TestWorkItemHandler.getInstance());
        service.execute(registerCommand1);
	}
	
	private static class KnowledgeLogger extends WorkingMemoryLogger {
		public KnowledgeLogger(KieSession session) {
			super((KnowledgeRuntimeEventManager) session);
		}

		@Override
		public void logEventCreated(LogEvent logEvent) {
			System.out.println("event: " + logEvent.toString());
		}
	}
}
