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

package org.kie.server.jms;

/**
 * This exception is primarily used by the JMS code to indicate that an operation
 * that we expect to succeed has failed. In most of the cases, when this exception
 * is thrown, it is due to a failure by the underlying JMS framework ({@link javax.jms.Session},
 * {@link javax.jms.Connection}).
 */
public class JMSRuntimeException
        extends RuntimeException  {

    public JMSRuntimeException() {}

    public JMSRuntimeException(String message) {
        super( message );
    }

    public JMSRuntimeException(String message, Throwable cause) {
        super( message, cause );
    }

    public JMSRuntimeException(Throwable cause) {
        super( cause );
    }

}
