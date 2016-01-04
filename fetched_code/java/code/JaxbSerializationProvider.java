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

package org.kie.services.client.serialization;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.kie.internal.jaxb.StringKeyObjectValueEntry;
import org.kie.internal.jaxb.StringKeyObjectValueMap;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbRestRequestException;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceFormResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItemResponse;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.runtime.JaxbCorrelationKey;
import org.kie.services.client.serialization.jaxb.impl.runtime.JaxbCorrelationProperty;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbArray;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbBoolean;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbByte;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbCharacter;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbDouble;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbFloat;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbInteger;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbList;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbLong;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbMap;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbSet;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbShort;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbString;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public abstract class JaxbSerializationProvider implements SerializationProvider {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbSerializationProvider.class);

    // Classes -------------------------------------------------------------------------------------------------------------------

    public static Set<Class<?>> KIE_JAXB_CLASS_SET;
    static {
        Class<?> [] kieJaxbClasses = {
                // command response
                JaxbTaskContentResponse.class,
                JaxbTaskFormResponse.class,
                JaxbProcessInstanceListResponse.class,
                JaxbProcessInstanceResponse.class,
                JaxbProcessInstanceWithVariablesResponse.class,
                JaxbProcessInstanceFormResponse.class,

                // REST other
                JaxbGenericResponse.class,
                JaxbLongListResponse.class,
                JaxbStringListResponse.class,
                JaxbOtherResponse.class,
                JaxbPrimitiveResponse.class,
                JaxbVariablesResponse.class,
                JaxbExceptionResponse.class,
                JaxbGenericResponse.class,
                JaxbRequestStatus.class,

                // deployment
                JaxbDeploymentJobResult.class,
                JaxbDeploymentUnit.class,
                JaxbDeploymentUnitList.class,
                JaxbDeploymentDescriptor.class,

                // correlation key
                JaxbCorrelationKey.class,
                JaxbCorrelationProperty.class,

                // process
                JaxbProcessDefinition.class,
                JaxbProcessDefinitionList.class,

                // workitem
                JaxbWorkItemResponse.class,

                // history
                JaxbHistoryLogList.class,
                JaxbNodeInstanceLog.class,
                JaxbProcessInstanceLog.class,
                JaxbVariableInstanceLog.class,

                // task
                JaxbTaskSummary.class,

                // query
                JaxbQueryTaskResult.class,
                JaxbQueryProcessInstanceResult.class,

                // exception
                JaxbRestRequestException.class,

                // variable wrapper element for primitives and their wrappers
                JaxbBoolean.class,
                JaxbByte.class,
                JaxbCharacter.class,
                JaxbDouble.class,
                JaxbFloat.class,
                JaxbInteger.class,
                JaxbLong.class,
                JaxbShort.class,
                JaxbString.class,
                JaxbArray.class,
                JaxbList.class,
                JaxbSet.class,
                JaxbMap.class,

                // old map classes
                StringKeyObjectValueMap.class,
                StringKeyObjectValueEntry.class,
        };
        KIE_JAXB_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(Arrays.asList(kieJaxbClasses));
    }

    public static Set<Class<?>> PRIMITIVE_ARRAY_CLASS_SET;
    static {
        Class<?> [] primitiveClasses = {
                new Boolean[]{}.getClass(),
                new Byte[]{}.getClass(),
                new Character[]{}.getClass(),
                new Double[]{}.getClass(),
                new Float[]{}.getClass(),
                new Integer[]{}.getClass(),
                new Long[]{}.getClass(),
                new Math[]{}.getClass(),
                new Number[]{}.getClass(),
                new Short[]{}.getClass(),
                new String[]{}.getClass()
        };
        PRIMITIVE_ARRAY_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(Arrays.asList(primitiveClasses));
    };

    // Other fields, methods, etc -------------------------------------------------------------------------------------------------

    public final static int JMS_SERIALIZATION_TYPE = 0;

    @Override
    public int getSerializationType() {
       return JMS_SERIALIZATION_TYPE;
    }

    public final static String EXECUTE_DEPLOYMENT_ID_HEADER = "Kie-Deployment-Id";

    private boolean prettyPrint = false;

    public void setPrettyPrint( boolean prettyPrint ) {
       this.prettyPrint = true;
    }

    public boolean getPrettyPrint() {
        return prettyPrint;
    }

    public abstract void addJaxbClasses( Class... jaxbClass );

    public abstract void addJaxbClassesAndReinitialize( Class... jaxbClass );

    public abstract Collection<Class<?>> getExtraJaxbClasses();

    public abstract JAXBContext getJaxbContext();

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#serialize(java.lang.Object)
     */
    @Override
    public synchronized String serialize(Object object) {
        return serialize(getJaxbContext(), getPrettyPrint(), object);
    }

    public static String serialize(JAXBContext jaxbContext, boolean prettyPrint, Object object) {
        Marshaller marshaller = configureMarshaller(jaxbContext, prettyPrint);

        StringWriter stringWriter = new StringWriter();
        try {
            marshaller.marshal(object, stringWriter);
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to marshall " + object.getClass().getSimpleName() + " instance.", jaxbe);
        }

        return stringWriter.toString();
    }

    public static Marshaller configureMarshaller(JAXBContext jaxbContext, boolean prettyPrint) {
        Marshaller marshaller = null;
        try {
            marshaller = jaxbContext.createMarshaller();
            if( prettyPrint ) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to create JAXB marshaller", jaxbe);
        }

        try {
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), XmlCharacterHandler.getInstance());
        } catch (PropertyException e) {
            throw new SerializationException("Unable to set CharacterEscapeHandler", e);
        }
        return marshaller;
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#deserialize(java.lang.String)
     */
    @Override
    public synchronized Object deserialize(String xmlStr) {
       return deserialize(getJaxbContext(), xmlStr);
    }

    public static Object deserialize(JAXBContext jaxbContext, String xmlStr) {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch( JAXBException jaxbe ) {
            throw new SerializationException("Unable to create unmarshaller.", jaxbe);
        }
        ByteArrayInputStream xmlStrInputStream = new ByteArrayInputStream(xmlStr.getBytes(Charset.forName("UTF-8")));

        Object jaxbObj = null;
        try {
            jaxbObj = unmarshaller.unmarshal(xmlStrInputStream);
        } catch( JAXBException jaxbe ) {
           throw new SerializationException("Unable to unmarshal string.", jaxbe);
        }

        return jaxbObj;
    }

    // methods for class set properties (JMS messages) ----------------------------------------------------------------------------

    public static Set<Class<?>> commaSeperatedStringToClassSet(ClassLoader classloader, String extraClassNames) throws SerializationException {
        Set<Class<?>> classList = new HashSet<Class<?>>();

        extraClassNames = extraClassNames.trim();
        if( extraClassNames.isEmpty() ) {
            return classList;
        }
        String [] extraClassNameList = split(extraClassNames);
        if( extraClassNameList.length == 0 ) {
            return classList;
        }

        // non-empty string/list
        for( String extraClassName : extraClassNameList ) {
            if( extraClassName.endsWith("[]") ) {
                continue;
            }
            try {
                classList.add(classloader.loadClass(extraClassName));
            } catch( ClassNotFoundException cnfe ) {
                throw new SerializationException("Unable to load JAXB class '" + extraClassName, cnfe);
            }
        }
        return classList;
    }

    public static String classSetToCommaSeperatedString(Collection<Class<?>> extraClassList) throws SerializationException {
        StringBuilder out = new StringBuilder("");
        Set<Class<?>> extraClassSet = new HashSet<Class<?>>();
        extraClassSet.addAll(extraClassList);
        for( Class<?> extraClass : extraClassSet ) {
            if (out.length() > 0) {
                out.append(",");
            }
            String extraClassName = extraClass.getCanonicalName();
            if( extraClassName == null ) {
                throw new SerializationException("Only classes with canonical names can be used for serialization");
            }
            out.append(extraClassName);
        }
        return out.toString();
    }

    static String[] split(String in) {
        String[] splitIn = in.split(",");
        List<String> outList = new ArrayList<String>();
        for (int i = 0; i < splitIn.length; ++i) {
            splitIn[i] = splitIn[i].trim();
            if (!splitIn[i].isEmpty()) {
                outList.add(splitIn[i]);
            }
        }
        return outList.toArray(new String[outList.size()]);
    }

    public static <T> T unsupported(Class<?> realClass, Class<T> returnType) {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + realClass.getSimpleName() + " implementation.");
    }

}