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

package org.kie.remote.services.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.runtime.GetFactCountCommand;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.GetIdCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.command.runtime.process.GetWorkItemCommand;
import org.drools.core.command.runtime.process.SetProcessInstanceVariablesCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartCorrelatedProcessCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.jbpm.process.audit.command.AuditCommand;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindActiveProcessInstancesCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesByNameCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.AddCommentCommand;
import org.jbpm.services.task.commands.AddContentFromUserCommand;
import org.jbpm.services.task.commands.AddTaskCommand;
import org.jbpm.services.task.commands.CancelDeadlineCommand;
import org.jbpm.services.task.commands.ClaimNextAvailableTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.DeleteCommentCommand;
import org.jbpm.services.task.commands.ExecuteTaskRulesCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.ForwardTaskCommand;
import org.jbpm.services.task.commands.GetAllCommentsCommand;
import org.jbpm.services.task.commands.GetAttachmentCommand;
import org.jbpm.services.task.commands.GetCommentCommand;
import org.jbpm.services.task.commands.GetContentByIdCommand;
import org.jbpm.services.task.commands.GetContentByIdForUserCommand;
import org.jbpm.services.task.commands.GetContentMapForUserCommand;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTaskAssignedAsPotentialOwnerCommand;
import org.jbpm.services.task.commands.GetTaskByWorkItemIdCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.GetTaskContentCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksByStatusByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.NominateTaskCommand;
import org.jbpm.services.task.commands.ProcessSubTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.ResumeTaskCommand;
import org.jbpm.services.task.commands.SetTaskPropertyCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.StopTaskCommand;
import org.jbpm.services.task.commands.SuspendTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskSummaryQueryCommand;
import org.kie.api.command.Command;
import org.kie.internal.jaxb.CorrelationKeyXmlAdapter;
import org.kie.internal.process.CorrelationKey;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.services.shared.ServicesVersion;

@XmlRootElement(name = "command-request")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("rawtypes")
public class JaxbCommandsRequest {

    @XmlElement(name = "deployment-id")
    @XmlSchemaType(name = "string")
    private String deploymentId;

    @XmlElement(name = "process-instance-id")
    @XmlSchemaType(name = "long")
    private Long processInstanceId;

    @XmlElement(name = "ver")
    @XmlSchemaType(name = "string")
    private String version = ServicesVersion.VERSION;

    @XmlElement
    @XmlSchemaType(name = "string")
    private String user;

    @XmlElement
    @XmlSchemaType(name = "string")
    private String correlationKeyString;
    
    // This array is set during server-side processing of the JMS
    private transient String [] userPass;

    // This list should match the list in AcceptedCommands
    @XmlElements({
            @XmlElement(name = "complete-work-item", type = CompleteWorkItemCommand.class),
            @XmlElement(name = "abort-work-item", type = AbortWorkItemCommand.class),
            @XmlElement(name = "get-workitem", type = GetWorkItemCommand.class),
            
            @XmlElement(name = "abort-process-instance", type = AbortProcessInstanceCommand.class),
            @XmlElement(name = "get-process-ids", type = GetProcessIdsCommand.class),
            @XmlElement(name = "get-process-instance-by-correlation-key", type = GetProcessInstanceByCorrelationKeyCommand.class),
            @XmlElement(name = "get-process-instance", type = GetProcessInstanceCommand.class),
            @XmlElement(name = "get-process-instances", type = GetProcessInstancesCommand.class),
            @XmlElement(name = "set-process-instance-vars", type = SetProcessInstanceVariablesCommand.class),
            @XmlElement(name = "signal-event", type = SignalEventCommand.class),
            @XmlElement(name = "start-correlated-process", type = StartCorrelatedProcessCommand.class),
            @XmlElement(name = "start-process", type = StartProcessCommand.class),
            
            @XmlElement(name = "get-variable", type = GetVariableCommand.class),
            @XmlElement(name = "get-fact-count", type = GetFactCountCommand.class),
            @XmlElement(name = "get-global", type = GetGlobalCommand.class),
            @XmlElement(name = "get-id", type = GetIdCommand.class),
            @XmlElement(name = "set-global", type = SetGlobalCommand.class),
            
            @XmlElement(name = "delete", type = DeleteCommand.class),
            @XmlElement(name = "fire-all-rules", type = FireAllRulesCommand.class),
            @XmlElement(name = "insert-object", type = InsertObjectCommand.class),
            @XmlElement(name = "update", type = UpdateCommand.class),
           
            // task
            @XmlElement(name = "activate-task", type = ActivateTaskCommand.class),
            @XmlElement(name = "add-task", type = AddTaskCommand.class),
            @XmlElement(name = "claim-next-available-task", type = ClaimNextAvailableTaskCommand.class),
            @XmlElement(name = "claim-task", type = ClaimTaskCommand.class),
            @XmlElement(name = "complete-task", type = CompleteTaskCommand.class),
            @XmlElement(name = "delegate-task", type = DelegateTaskCommand.class),
            @XmlElement(name = "exit-task", type = ExitTaskCommand.class),
            @XmlElement(name = "fail-task", type = FailTaskCommand.class),
            @XmlElement(name = "forward-task", type = ForwardTaskCommand.class),
            @XmlElement(name = "get-attachment", type = GetAttachmentCommand.class),

            @XmlElement(name = "get-content", type = GetContentByIdCommand.class),
            @XmlElement(name = "get-task-content", type = GetTaskContentCommand.class),

            @XmlElement(name = "delete-comment", type = DeleteCommentCommand.class),
            @XmlElement(name = "add-comment", type = AddCommentCommand.class),
            @XmlElement(name = "get-all-comments", type = GetAllCommentsCommand.class),
            @XmlElement(name = "get-comment", type = GetCommentCommand.class),
            @XmlElement(name = "set-task-property", type = SetTaskPropertyCommand.class),
           
            @XmlElement(name = "add-content-from-user", type = AddContentFromUserCommand.class),
            @XmlElement(name = "get-content-by-id", type = GetContentByIdForUserCommand.class),
            @XmlElement(name = "get-content-map-for-user", type = GetContentMapForUserCommand.class),
            
            @XmlElement(name = "get-task-as-business-admin", type = GetTaskAssignedAsBusinessAdminCommand.class),
            @XmlElement(name = "get-task-as-potential-owner", type = GetTaskAssignedAsPotentialOwnerCommand.class),
            @XmlElement(name = "get-task-by-workitemid", type = GetTaskByWorkItemIdCommand.class),
            @XmlElement(name = "get-task", type = GetTaskCommand.class),
            @XmlElement(name = "get-tasks-by-processinstanceid", type = GetTasksByProcessInstanceIdCommand.class),
            @XmlElement(name = "get-tasks-by-status-by-processinstanceid", type = GetTasksByStatusByProcessInstanceIdCommand.class),
            @XmlElement(name = "get-tasks-by-various", type = GetTasksByVariousFieldsCommand.class),
            @XmlElement(name = "get-tasks-owned", type = GetTasksOwnedCommand.class),
            @XmlElement(name = "task-query-where", type = TaskSummaryQueryCommand.class),
            
            @XmlElement(name = "nominate-task", type = NominateTaskCommand.class),
            @XmlElement(name = "release-task", type = ReleaseTaskCommand.class),
            @XmlElement(name = "resume-task", type = ResumeTaskCommand.class),
            @XmlElement(name = "skip-task", type = SkipTaskCommand.class),
            @XmlElement(name = "start-task", type = StartTaskCommand.class),
            @XmlElement(name = "stop-task", type = StopTaskCommand.class),
            @XmlElement(name = "suspend-task", type = SuspendTaskCommand.class),
            @XmlElement(name = "process-sub-tasks", type = ProcessSubTaskCommand.class),
            @XmlElement(name = "execute-task-rules", type = ExecuteTaskRulesCommand.class),
            @XmlElement(name = "cancel-deadline", type = CancelDeadlineCommand.class),
            
            // audit
            @XmlElement(name = "clear-history-logs", type = ClearHistoryLogsCommand.class),
            @XmlElement(name = "find-active-process-instances", type = FindActiveProcessInstancesCommand.class),
            @XmlElement(name = "find-node-instances", type = FindNodeInstancesCommand.class),
            @XmlElement(name = "find-process-instance", type = FindProcessInstanceCommand.class),
            @XmlElement(name = "find-process-instances", type = FindProcessInstancesCommand.class),
            @XmlElement(name = "find-subprocess-instances", type = FindSubProcessInstancesCommand.class),
            @XmlElement(name = "find-variable-instances", type = FindVariableInstancesCommand.class),
            @XmlElement(name = "find-variable-instances-by-name", type = FindVariableInstancesByNameCommand.class)
    })
    protected List<Command> commands;

    public JaxbCommandsRequest() {
        // Default constructor
    }

    public JaxbCommandsRequest(Command command) {
        checkThatCommandIsAccepted(command);
        this.commands = new ArrayList<Command>();
        this.commands.add(command);
        checkThatCommandsContainDeploymentIdIfNeeded(this.commands);
    }
    
    public JaxbCommandsRequest(List<Command> commands) {
        checkThatCommandsAreAccepted(commands);
        this.commands = new ArrayList<Command>();
        this.commands.addAll(commands);
        checkThatCommandsContainDeploymentIdIfNeeded(this.commands);
    }

    private void checkThatCommandsContainDeploymentIdIfNeeded(List<Command> checkCommands) {
        for( Command<?> command : checkCommands ) { 
            if( ! (command instanceof TaskCommand<?>) && ! (command instanceof AuditCommand<?>) ) { 
                throw new UnsupportedOperationException( "A " + command.getClass().getSimpleName() + " requires that the deployment id has been set!" );
            }
        }
    }

    public JaxbCommandsRequest(String deploymentId, Command<?> command) {
        checkThatCommandIsAccepted(command);
        this.deploymentId = deploymentId;
        this.commands = new ArrayList<Command>();
        this.commands.add(command);
    }

    public JaxbCommandsRequest(String deploymentId, List<Command> commands) {
        checkThatCommandsAreAccepted(commands);
        this.deploymentId = deploymentId;
        this.commands = new ArrayList<Command>();
        this.commands.addAll(commands);
    }

    private void checkThatCommandsAreAccepted(Collection<Command> cmds) { 
       for( Command cmd : cmds ) { 
          checkThatCommandIsAccepted(cmd); 
       }
    }
    
    private void checkThatCommandIsAccepted(Command<?> cmd) { 
        if( ! AcceptedServerCommands.isAcceptedCommandClass(cmd.getClass()) ) {
           throw new UnsupportedOperationException(cmd.getClass().getName() + " is not an accepted command." ); 
        }
    }
    
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCorrelationKeyString() {
        return correlationKeyString;
    }

    public void setCorrelationKeyString(String correlationKeyString) {
        this.correlationKeyString = correlationKeyString;
    }
    
    public CorrelationKey getCorrelationKey() {
        return CorrelationKeyXmlAdapter.unmarshalCorrelationKey(this.correlationKeyString);
    }
    
    public void setCommands(List<Command> commands) {
        checkThatCommandsAreAccepted(commands);
        this.commands = commands;
    }

    public List<Command> getCommands() {
        if( this.commands == null ) { 
            this.commands = new ArrayList<Command>();
        }
        return this.commands;
    }
   
    @JsonIgnore
    public String [] getUserPass() {
        return userPass;
    }

    @JsonIgnore
    public void setUserPass(String [] userPass) {
        this.userPass = userPass;
    }

    public String toString() {
    	StringBuffer result = new StringBuffer("JaxbCommandsRequest " + deploymentId + "\n");
    	if (commands != null) {
	    	for (Command<?> command: commands) {
	    		result.append(command + "\n");
	    	}
    	}
    	return result.toString();
    }

}
