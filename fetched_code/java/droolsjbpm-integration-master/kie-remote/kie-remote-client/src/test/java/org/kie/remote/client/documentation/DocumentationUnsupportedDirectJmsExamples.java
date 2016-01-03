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

package org.kie.remote.client.documentation;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsPotentialOwnerCommand;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationConstants;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: changed, add to documentation
public class DocumentationUnsupportedDirectJmsExamples {

    protected static final Logger logger = LoggerFactory.getLogger(DocumentationUnsupportedDirectJmsExamples.class);

    public void sendAndReceiveJmsMessage() {

        String USER = "charlie";
        String PASSWORD = "ch0c0licious";

        String DEPLOYMENT_ID = "test-project";
        String PROCESS_ID_1 = "oompa-processing";
        URL serverUrl;
        try {
            serverUrl = new URL("http://localhost:8080/jbpm-console/");
        } catch (MalformedURLException murle) {
            logger.error("Malformed URL for the server instance!", murle); 
            return;
        }

        // Create JaxbCommandsRequest instance and add commands
        Command<?> cmd = new StartProcessCommand();
        ((StartProcessCommand) cmd).setProcessId(PROCESS_ID_1);
        int oompaProcessingResultIndex = 0;
        JaxbCommandsRequest req = new JaxbCommandsRequest(DEPLOYMENT_ID, cmd);
        cmd = new GetTaskAssignedAsPotentialOwnerCommand();
        ((GetTaskAssignedAsPotentialOwnerCommand) cmd).setUserId(USER);
        req.getCommands().add(cmd);
        int loompaMonitoringResultIndex = 1;

        // Get JNDI context from server
        InitialContext context = getRemoteJbossInitialContext(serverUrl, USER, PASSWORD);
            
        // Create JMS connection 
        ConnectionFactory connectionFactory;
        try {
            connectionFactory = (ConnectionFactory) context.lookup("jms/RemoteConnectionFactory");
        } catch (NamingException ne) {
            throw new RuntimeException("Unable to lookup JMS connection factory.", ne);
        }

        // Setup queues
        Queue sendQueue, responseQueue;
        try {
            sendQueue = (Queue) context.lookup("jms/queue/KIE.SESSION");
            responseQueue = (Queue) context.lookup("jms/queue/KIE.RESPONSE");
        } catch (NamingException ne) {
            throw new RuntimeException("Unable to lookup send or response queue", ne);
        }

        // Send command request
        Long processInstanceId = null; // needed if you're doing an operation on a PER_PROCESS_INSTANCE deployment
        String humanTaskUser = USER;
        JaxbCommandsResponse cmdResponse = sendJmsCommands(
                DEPLOYMENT_ID, processInstanceId, humanTaskUser, req, 
                connectionFactory, sendQueue, responseQueue, 
                USER, PASSWORD, 5);

        // Retrieve results
        ProcessInstance oompaProcInst = null;
        List<TaskSummary> charliesTasks = null;
        for (JaxbCommandResponse<?> response : cmdResponse.getResponses()) {
            if (response instanceof JaxbExceptionResponse) {
                // something went wrong on the server side
                JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) response;
                throw new RuntimeException(exceptionResponse.getMessage());
            }

            if (response.getIndex() == oompaProcessingResultIndex) {
                oompaProcInst = (ProcessInstance) response.getResult();
            } else if (response.getIndex() == loompaMonitoringResultIndex) {
                charliesTasks = (List<TaskSummary>) response.getResult();
            }
        }
    }

    private JaxbCommandsResponse sendJmsCommands(String deploymentId, Long processInstanceId, String user, JaxbCommandsRequest req,
            ConnectionFactory factory, Queue sendQueue, Queue responseQueue, String jmsUser, String jmsPassword, int timeout) {
        req.setProcessInstanceId(processInstanceId);
        req.setUser(user);

        Connection connection = null;
        Session session = null;
        String corrId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = '" + corrId + "'";
        JaxbCommandsResponse cmdResponses = null;
        try {

            // setup
            MessageProducer producer;
            MessageConsumer consumer;
            try {
                if (jmsPassword != null) {
                    connection = factory.createConnection(jmsUser, jmsPassword);
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch (JMSException jmse) {
                throw new RemoteCommunicationException("Unable to setup a JMS connection.", jmse);
            }

            JaxbSerializationProvider serializationProvider = ClientJaxbSerializationProvider.newInstance();
            // if necessary, add user-created classes here:
            // xmlSerializer.addJaxbClasses(MyType.class, AnotherJaxbAnnotatedType.class);

            // Create msg
            TextMessage msg;
            try {

                // serialize request
                String xmlStr = serializationProvider.serialize(req);
                msg = session.createTextMessage(xmlStr);

                // set properties
                msg.setJMSCorrelationID(corrId);
                msg.setIntProperty(SerializationConstants.SERIALIZATION_TYPE_PROPERTY_NAME, JaxbSerializationProvider.JMS_SERIALIZATION_TYPE);
                Collection<Class<?>> extraJaxbClasses = serializationProvider.getExtraJaxbClasses();
                if (!extraJaxbClasses.isEmpty()) {
                    String extraJaxbClassesPropertyValue = serializationProvider.classSetToCommaSeperatedString(extraJaxbClasses);
                    msg.setStringProperty(SerializationConstants.EXTRA_JAXB_CLASSES_PROPERTY_NAME, extraJaxbClassesPropertyValue);
                    msg.setStringProperty(SerializationConstants.DEPLOYMENT_ID_PROPERTY_NAME, deploymentId);
                }
            } catch (JMSException jmse) {
                throw new RemoteCommunicationException("Unable to create and fill a JMS message.", jmse);
            } catch (SerializationException se) {
                throw new RemoteCommunicationException("Unable to deserialze JMS message.", se.getCause());
            }

            // send
            try {
                producer.send(msg);
            } catch (JMSException jmse) {
                throw new RemoteCommunicationException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                response = consumer.receive(timeout);
            } catch (JMSException jmse) {
                throw new RemoteCommunicationException("Unable to receive or retrieve the JMS response.", jmse);
            }

            if (response == null) {
                logger.warn("Response is empty, leaving");
                return null;
            }
            // extract response
            assert response != null : "Response is empty.";
            try {
                String xmlStr = ((TextMessage) response).getText();
                cmdResponses = (JaxbCommandsResponse) serializationProvider.deserialize(xmlStr);
            } catch (JMSException jmse) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jmse);
            } catch (SerializationException se) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", se.getCause());
            }
            assert cmdResponses != null : "Jaxb Cmd Response was null!";
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    if( session != null ) { 
                        session.close();
                    }
                } catch (JMSException jmse) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
        return cmdResponses;
    }

    private InitialContext getRemoteJbossInitialContext(URL url, String user, String password) { 
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        String jbossServerHostName = url.getHost();
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://"+ jbossServerHostName + ":4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, user);
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, password);

        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RemoteCommunicationException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
    

}
