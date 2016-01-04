/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.simulation.fluent.test.impl;

import org.drools.simulation.fluent.test.CheckableFluent;
import org.drools.simulation.fluent.test.TestableFluent;
import org.hamcrest.Matcher;
import org.kie.api.command.Command;
import org.kie.internal.fluent.test.ReflectiveMatcherAssert;

import static org.junit.Assert.assertThat;

public abstract class AbstractTestableFluent<P> implements TestableFluent<P> {
    
    public AbstractTestableFluent() {
    }

    protected abstract TestableFluent<P> addCommand(Command command);

    public <T> P test(String reason,
                      T actual,
                      Matcher<T> matcher) {
        assertThat(reason, actual, matcher);
        return (P) this;
    }

    public <T> P test(T actual,
                      Matcher<T> matcher) {
        assertThat( actual, matcher);
        return (P) this;
    }
    
    public P test(String text) {
        MVELTestCommand testCmd = new MVELTestCommand();
        testCmd.setText( text );
        
        addCommand(testCmd);
        return (P) this;
    }
    
    public P test(ReflectiveMatcherAssert matcherAssert) {
        ReflectiveMatcherAssertCommand matcherCmd = new ReflectiveMatcherAssertCommand( matcherAssert );

        addCommand(matcherCmd);
        return (P) this;
    }

    public CheckableFluent<P> given(String name) {
        return new CheckableFactory<P>(this, name);
    }
}
