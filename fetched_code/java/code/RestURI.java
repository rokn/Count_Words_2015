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

package org.kie.server.api.rest;

import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

public class RestURI {

    //parameters
    public static final String CONTAINER_ID = "id";
    public static final String PROCESS_ID = "pId";
    public static final String PROCESS_INST_ID = "pInstanceId";
    public static final String SIGNAL_NAME = "sName";
    public static final String VAR_NAME = "varName";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_INSTANCE_ID = "tInstanceId";
    public static final String CONTENT_ID = "contentId";
    public static final String COMMENT_ID = "commentId";
    public static final String ATTACHMENT_ID = "attachmentId";
    public static final String CORRELATION_KEY = "correlationKey";
    public static final String WORK_ITEM_ID = "workItemId";
    public static final String JOB_ID = "jobId";
    public static final String JOB_CMD_NAME = "cmd";
    public static final String JOB_KEY = "key";

    public static final String PROCESS_URI = "containers/{" + CONTAINER_ID + "}/processes";
    public static final String PROCESS_DEF_URI = "containers/{" + CONTAINER_ID + "}/processes/definitions";
    public static final String JOB_URI = "jobs";
    public static final String TASK_URI = "containers/{" + CONTAINER_ID + "}/tasks";
    public static final String QUERY_URI = "queries";
    public static final String FORM_URI = "containers/{" + CONTAINER_ID + "}/forms";
    public static final String IMAGE_URI = "containers/{" + CONTAINER_ID + "}/images";

    // uris
    // process related prefixed by PROCESS_URI
    public static final String START_PROCESS_POST_URI = "{" + PROCESS_ID +"}/instances";
    public static final String START_PROCESS_WITH_CORRELATION_KEY_POST_URI = "{" + PROCESS_ID +"}/instances/correlation/{" + CORRELATION_KEY + "}";
    public static final String ABORT_PROCESS_INST_DEL_URI = "instances/{" + PROCESS_INST_ID +"}";
    public static final String ABORT_PROCESS_INSTANCES_DEL_URI = "instances";
    public static final String SIGNAL_PROCESS_INST_POST_URI = "instances/{" + PROCESS_INST_ID +"}/signal/{" + SIGNAL_NAME + "}";
    public static final String SIGNAL_PROCESS_INSTANCES_PORT_URI = "instances/signal/{" + SIGNAL_NAME + "}";
    public static final String PROCESS_INSTANCE_GET_URI = "instances/{" + PROCESS_INST_ID + "}";
    public static final String PROCESS_INSTANCE_VAR_PUT_URI = "instances/{" + PROCESS_INST_ID + "}/variable/{" + VAR_NAME + "}";
    public static final String PROCESS_INSTANCE_VARS_POST_URI = "instances/{" + PROCESS_INST_ID + "}/variables";
    public static final String PROCESS_INSTANCE_VAR_GET_URI = "instances/{" + PROCESS_INST_ID + "}/variable/{" + VAR_NAME + "}";
    public static final String PROCESS_INSTANCE_VARS_GET_URI = "instances/{" + PROCESS_INST_ID + "}/variables";
    public static final String PROCESS_INSTANCE_SIGNALS_GET_URI = "instances/{" + PROCESS_INST_ID + "}/signals";

    public static final String PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI = "instances/{" + PROCESS_INST_ID + "}/workitems/{" + WORK_ITEM_ID +"}/completed";
    public static final String PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI = "instances/{" + PROCESS_INST_ID + "}/workitems/{" + WORK_ITEM_ID +"}/aborted";
    public static final String PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI = "instances/{" + PROCESS_INST_ID + "}/workitems/{" + WORK_ITEM_ID +"}";
    public static final String PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI = "instances/{" + PROCESS_INST_ID + "}/workitems";

    // process definition related prefixed by PROCESS_DEF_URI
    public static final String PROCESS_DEF_GET_URI = "{" + PROCESS_ID +"}";
    public static final String PROCESS_DEF_SUBPROCESS_GET_URI = "{" + PROCESS_ID +"}/subprocesses";
    public static final String PROCESS_DEF_VARIABLES_GET_URI = "{" + PROCESS_ID +"}/variables";
    public static final String PROCESS_DEF_SERVICE_TASKS_GET_URI = "{" + PROCESS_ID +"}/tasks/service";
    public static final String PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI = "{" + PROCESS_ID +"}/entities";
    public static final String PROCESS_DEF_USER_TASKS_GET_URI = "{" + PROCESS_ID +"}/tasks/users";
    public static final String PROCESS_DEF_USER_TASK_INPUT_GET_URI = "{" + PROCESS_ID +"}/tasks/users/{" + TASK_NAME + "}/inputs";
    public static final String PROCESS_DEF_USER_TASK_OUTPUT_GET_URI = "{" + PROCESS_ID +"}/tasks/users/{" + TASK_NAME + "}/outputs";

    // runtime data related prefixed by QUERY_URI
    public static final String PROCESS_INSTANCES_GET_URI = "processes/instances";
    public static final String PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI = "processes/{" + PROCESS_ID +"}/instances";
    public static final String PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI = "containers/{" + CONTAINER_ID + "}/process/instances";
    public static final String PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI = "processes/instance/correlation/{" + CORRELATION_KEY + "}";
    public static final String PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI = "processes/instances/correlation/{" + CORRELATION_KEY + "}";
    public static final String PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}";
    public static final String PROCESS_INSTANCE_BY_VAR_NAME_GET_URI = "processes/instances/variables/{" + VAR_NAME + "}";

    public static final String PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI = "containers/{" + CONTAINER_ID + "}/processes/definitions";
    public static final String PROCESS_DEFINITIONS_GET_URI = "processes/definitions";
    public static final String PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI = "containers/{" + CONTAINER_ID + "}/processes/definitions/{" + PROCESS_ID + "}";
    public static final String PROCESS_DEFINITIONS_BY_ID_GET_URI = "processes/definitions/{" + PROCESS_ID + "}";

    public static final String NODE_INSTANCES_BY_INSTANCE_ID_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}/nodes/instances";
    public static final String NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}/wi-nodes/instances/{" + WORK_ITEM_ID +"}";

    public static final String VAR_INSTANCES_BY_INSTANCE_ID_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}/variables/instances";
    public static final String VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}/variables/instances/{" + VAR_NAME +"}";

    // task search related prefixed by QUERY_URI
    public static final String TASKS_ASSIGN_POT_OWNERS_GET_URI = "tasks/instances/pot-owners";
    public static final String TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI = "tasks/instances/admins";
    public static final String TASKS_OWNED_GET_URI = "tasks/instances/owners";
    public static final String TASKS_GET_URI = "tasks/instances";
    public static final String TASKS_EVENTS_GET_URI = "tasks/instances/{" + TASK_INSTANCE_ID + "}/events";
    public static final String TASK_GET_URI = "tasks/instances/{" + TASK_INSTANCE_ID + "}";
    public static final String TASK_BY_WORK_ITEM_ID_GET_URI = "tasks/instances/workitem/{" + WORK_ITEM_ID + "}";
    public static final String TASK_BY_PROCESS_INST_ID_GET_URI = "tasks/instances/process/{" + PROCESS_INST_ID + "}";
    public static final String TASKS_BY_VAR_NAME_GET_URI = "tasks/instances/variables/{" + VAR_NAME + "}";

    // task related prefixed by TASK_URI
    public static final String TASK_INSTANCE_ACTIVATE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/activated";
    public static final String TASK_INSTANCE_CLAIM_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/claimed";
    public static final String TASK_INSTANCE_START_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/started";
    public static final String TASK_INSTANCE_STOP_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/stopped";
    public static final String TASK_INSTANCE_COMPLETE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/completed";
    public static final String TASK_INSTANCE_DELEGATE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/delegated";
    public static final String TASK_INSTANCE_EXIT_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/exited";
    public static final String TASK_INSTANCE_FAIL_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/failed";
    public static final String TASK_INSTANCE_FORWARD_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/forwarded";
    public static final String TASK_INSTANCE_RELEASE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/released";
    public static final String TASK_INSTANCE_RESUME_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/resumed";
    public static final String TASK_INSTANCE_SKIP_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/skipped";
    public static final String TASK_INSTANCE_SUSPEND_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/suspended";
    public static final String TASK_INSTANCE_NOMINATE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/states/nominated";

    public static final String TASK_INSTANCE_PRIORITY_PUT_URI = "{" + TASK_INSTANCE_ID + "}/priority";
    public static final String TASK_INSTANCE_DESCRIPTION_PUT_URI = "{" + TASK_INSTANCE_ID + "}/description";
    public static final String TASK_INSTANCE_NAME_PUT_URI = "{" + TASK_INSTANCE_ID + "}/name";
    public static final String TASK_INSTANCE_EXPIRATION_DATE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/expiration";
    public static final String TASK_INSTANCE_SKIPABLE_PUT_URI = "{" + TASK_INSTANCE_ID + "}/skipable";

    public static final String TASK_INSTANCE_OUTPUT_DATA_PUT_URI = "{" + TASK_INSTANCE_ID + "}/contents/output";
    public static final String TASK_INSTANCE_OUTPUT_DATA_GET_URI = "{" + TASK_INSTANCE_ID + "}/contents/output";
    public static final String TASK_INSTANCE_INPUT_DATA_GET_URI = "{" + TASK_INSTANCE_ID + "}/contents/input";

    public static final String TASK_INSTANCE_CONTENT_DATA_DELETE_URI = "{" + TASK_INSTANCE_ID + "}/contents/{" + CONTENT_ID +"}";

    public static final String TASK_INSTANCE_COMMENT_ADD_POST_URI = "{" + TASK_INSTANCE_ID + "}/comments";
    public static final String TASK_INSTANCE_COMMENTS_GET_URI = "{" + TASK_INSTANCE_ID + "}/comments";
    public static final String TASK_INSTANCE_COMMENT_GET_URI = "{" + TASK_INSTANCE_ID + "}/comments/{" + COMMENT_ID + "}";
    public static final String TASK_INSTANCE_COMMENT_DELETE_URI = "{" + TASK_INSTANCE_ID + "}/comments/{" + COMMENT_ID + "}";

    public static final String TASK_INSTANCE_ATTACHMENT_ADD_POST_URI = "{" + TASK_INSTANCE_ID + "}/attachments";
    public static final String TASK_INSTANCE_ATTACHMENTS_GET_URI = "{" + TASK_INSTANCE_ID + "}/attachments";
    public static final String TASK_INSTANCE_ATTACHMENT_GET_URI = "{" + TASK_INSTANCE_ID + "}/attachments/{" + ATTACHMENT_ID + "}";
    public static final String TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI = "{" + TASK_INSTANCE_ID + "}/attachments/{" + ATTACHMENT_ID + "}/content";
    public static final String TASK_INSTANCE_ATTACHMENT_DELETE_URI = "{" + TASK_INSTANCE_ID + "}/attachments/{" + ATTACHMENT_ID + "}";

    public static final String TASK_INSTANCE_GET_URI = "{" + TASK_INSTANCE_ID + "}";

    // job executor prefixed by JOB_URI
    public static final String CANCEL_JOB_DEL_URI = "{" + JOB_ID + "}";
    public static final String REQUEUE_JOB_PUT_URI = "{" + JOB_ID + "}";
    public static final String JOB_INSTANCES_BY_KEY_GET_URI = "keys/{" + JOB_KEY + "}";
    public static final String JOB_INSTANCES_BY_CMD_GET_URI = "commands/{" + JOB_CMD_NAME + "}";
    public static final String JOB_INSTANCE_GET_URI = "{" + JOB_ID + "}";

    // form prefixed by FORM_URI
    public static final String PROCESS_FORM_GET_URI = "processes/{" + PROCESS_ID + "}";
    public static final String TASK_FORM_GET_URI = "tasks/{" + TASK_INSTANCE_ID + "}";

    // image prefixed by IMAGE_URI
    public static final String PROCESS_IMG_GET_URI = "processes/{" + PROCESS_ID + "}";
    public static final String PROCESS_INST_IMG_GET_URI = "processes/instances/{" + PROCESS_INST_ID + "}";

    public static String build(String baseUrl, String template, Map<String, Object> parameters) {
        StrSubstitutor sub = new StrSubstitutor(parameters, "{", "}");
        String resourceUrl = sub.replace(template);

        return baseUrl + "/" + resourceUrl;
    }
}
