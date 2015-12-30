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

package org.kie.remote.services.rest.exception;


public class KieRemoteRestOperationException extends RuntimeException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 3065096836333886139L;

    protected Integer status = null;

    public KieRemoteRestOperationException(String s) {
        super(s);
    }

    protected KieRemoteRestOperationException(String s, Status status) {
        super(s);
        this.status = status.getValue();
    }

    public KieRemoteRestOperationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    protected KieRemoteRestOperationException(String s, Throwable throwable, Status status) {
        super(s, throwable);
        this.status = status.getValue();
    }

    public int getStatus() {
        return status;
    }

    public enum Status { 
        /** === SYNTAX === */
        
        // The request could not be understood by the server due to malformed syntax. 
        // The client SHOULD NOT repeat the request without modifications. 
        BAD_REQUEST(400), 
       
        /** === FUNCTIONAL === */
        
        // The server understood the request, but is refusing to fulfill it. 
        // Authorization will not help and the request SHOULD NOT be repeated.
        FORBIDDEN(403),
       
        // The server has not found anything matching the Request-URI. 
        // No indication is given of whether the condition is temporary or permanent. 
        NOT_FOUND(404), 
        
        // The request could not be completed due to a conflict with the current state of the resource. 
        // This code is only allowed in situations where it is expected that 
        // the user might be able to resolve the conflict and resubmit the request. 
        CONFLICT(409), 

        /** === TECHNICAL === */
        
        // The server encountered an unexpected condition which prevented it from fulfilling the request. 
        INTERNAL_SERVER_ERROR(500),
        
        /**
         * SHOULD NOT BE USED! (because these are inappropriate) 
         * Added for documentation
         */
        
        // The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. 
        // The response MUST include an Allow header containing a list of valid methods for the requested resource. 
        METHOD_NOT_ALLOWED(405),
        
        // The requested resource is no longer available at the server and no forwarding address is known. 
        // This condition is expected to be considered permanent.
        GONE(410),
        
        // The precondition given in one or more of the request-header fields evaluated to false when it was tested on the server.
        PRE_CONDITION_FAILED(412),

        /** === FORMAT === */
        
        // The resource identified by the request is only capable of generating response entities which have content characteristics 
        // not acceptable according to the accept headers sent in the request.  (e.g. XML, JSON)
        NOT_ACCEPTABLE(406),
        
        //  The server is refusing to service the request because the entity of the request is 
        // in a format not supported by the requested resource for the requested method.
        UNSUPPORTED_MEDIA_TYPE(415),
        
        /** === AUTH === */
        
        // The request requires user authentication. 
        UNAUTHORIZED(401);
        
        private final int status;
        
        Status(int s) { 
           this.status = s; 
        }
        
        public int getValue() { 
            return this.status;
        }
    }
   
    // Syntax
    public static KieRemoteRestOperationException badRequest(String msg) { 
        return new KieRemoteRestOperationException(msg, Status.BAD_REQUEST);
    }
   
    // Command not accepted 
    public static KieRemoteRestOperationException forbidden(String msg) { 
        return new KieRemoteRestOperationException(msg, Status.FORBIDDEN);
    }
   
    // permission problem
    public static KieRemoteRestOperationException conflict(String msg, Exception e) { 
        return new KieRemoteRestOperationException(msg, e, Status.CONFLICT);
    }
 
    public static KieRemoteRestOperationException conflict(String msg) { 
        return new KieRemoteRestOperationException(msg, Status.CONFLICT);
    } 
        
    // instance does not exist
    public static KieRemoteRestOperationException notFound(String msg) { 
        return new KieRemoteRestOperationException(msg, Status.NOT_FOUND);
    }
   
    public static KieRemoteRestOperationException notFound(String msg, Exception e) { 
        return new KieRemoteRestOperationException(msg, e, Status.NOT_FOUND);
    }
   
    // technical exception (including serialization problems)
    public static KieRemoteRestOperationException internalServerError(String msg) { 
        return new KieRemoteRestOperationException(msg, Status.INTERNAL_SERVER_ERROR);
    }
    
    public static KieRemoteRestOperationException internalServerError(String msg, Exception e) { 
        return new KieRemoteRestOperationException(msg, e, Status.INTERNAL_SERVER_ERROR);
    }
}
