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

package org.drools.simulation.fluent.test;

public interface RuleTestFluent<T> {

    T assertRuleFired(String ruleName);

    /**
     * Only applies to the last {@link #fireAllRules()} in this step.
     * @param ruleName never null
     * @param fireCount at least 0
     * @return this
     * throws IllegalArgumentException if {@link #fireAllRules()} has not been called in this step yet.
     */
    T assertRuleFired(String ruleName, int fireCount);
     
}
