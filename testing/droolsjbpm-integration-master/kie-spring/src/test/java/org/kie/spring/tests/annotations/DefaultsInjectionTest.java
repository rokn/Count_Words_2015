/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.tests.annotations;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.spring.beans.annotations.DefaultValuesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultsInjectionTest {

    static ApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/annotations/kbase-with-default.xml");
    }

    @Test
    public void testContext() throws Exception {
        assertNotNull(context);
    }

    @Test
    public void testKieBase() throws Exception {
        KieBase kbase = (KieBase) context.getBean("drl_kiesample3");
        assertNotNull(kbase);
        DefaultValuesBean sampleBean = (DefaultValuesBean) context.getBean("sampleBean");
        assertNotNull(sampleBean);
        assertNotNull(sampleBean.getKieBase() );
        assertTrue(sampleBean.getKieBase() instanceof KieBase);
    }

    @Test
    public void testSetterKieBase() throws Exception {
        KieBase kbase = (KieBase) context.getBean("drl_kiesample3");
        assertNotNull(kbase);
        DefaultValuesBean sampleBean = (DefaultValuesBean) context.getBean("sampleBean");
        assertNotNull(sampleBean);
        assertNotNull(sampleBean.getKieBase2() );
        assertTrue(sampleBean.getKieBase2() instanceof KieBase);
    }


    @AfterClass
    public static void tearDown() {

    }

}
