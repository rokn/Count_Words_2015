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

package org.jbpm.simulation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.simulation.impl.simulators.EndEventSimulator;
import org.jbpm.simulation.impl.simulators.EventSimulator;
import org.jbpm.simulation.impl.simulators.GatewaySimulator;
import org.jbpm.simulation.impl.simulators.HumanTaskActivitySimulator;
import org.jbpm.simulation.impl.simulators.StartEventSimulator;
import org.jbpm.simulation.impl.simulators.StateBasedActivitySimulator;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;



public class SimulationRegistry {
    
    protected static SimulationRegistry instance;
    
    private Map<Class<? extends Node>, ActivitySimulator> simulators = new HashMap<Class<? extends Node>, ActivitySimulator>();
    
    protected SimulationRegistry() {
        simulators.put(StartNode.class, new StartEventSimulator());
        simulators.put(EndNode.class, new EndEventSimulator());
        simulators.put(HumanTaskNode.class, new HumanTaskActivitySimulator());
        simulators.put(Split.class, new GatewaySimulator());
        simulators.put(Join.class, new GatewaySimulator());
        simulators.put(EventNode.class, new EventSimulator());
    }
    
    public static SimulationRegistry getInstance() {
        if (instance == null) {
            instance = new SimulationRegistry();
        }
        
        return instance;
    }

    public ActivitySimulator getSimulator(Node node) {
        if (this.simulators.containsKey(node.getClass())) {
            return this.simulators.get(node.getClass());
        }
        return new StateBasedActivitySimulator();
    }
    
    public void registerSimulator(Node node, ActivitySimulator simulator) {
        
    }
}
