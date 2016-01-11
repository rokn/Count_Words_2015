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

package org.drools.simulation.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.core.command.GetDefaultValue;
import org.drools.core.command.NewKieSessionCommand;
import org.drools.core.command.ResolvingKnowledgeCommandContext;
import org.drools.core.command.impl.ContextImpl;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.time.SessionPseudoClock;
import org.kie.api.command.Command;
import org.kie.internal.command.Context;
import org.kie.internal.command.World;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.simulation.Simulation;
import org.kie.internal.simulation.SimulationPath;
import org.kie.internal.simulation.SimulationStep;

public class Simulator
        implements World, GetDefaultValue {

    private PriorityQueue<SimulationStep>           queue;
    private SimulationImpl                simulation;
    //    private SessionPseudoClock  clock;
    private long                          startTime;

    private Context                       root;
    private Map<String, Context>          contexts;

    private static String                 ROOT             = "ROOT";

    private Set<StatefulKnowledgeSession> ksessions;

    private CommandExecutionHandler       executionHandler = new DefaultCommandExecutionHandler();

    private Object                        lastReturnValue;

    public Simulator( Simulation simulation,
                      //SessionPseudoClock clock,
                      long startTime ) {
        //  this.clock = clock;
        this.ksessions = new HashSet<StatefulKnowledgeSession>();

        this.startTime = startTime;
        this.simulation = (SimulationImpl) simulation;
        this.root = new ContextImpl( ROOT,
                                     this );
        
        this.root.set( "simulator", 
                       this );

        this.contexts = new HashMap<String, Context>();
        this.contexts.put( ROOT,
                           this.root );

        Map<String, SimulationPath> paths = this.simulation.getPaths();

        // calculate capacity
        int capacity = 0;
        for ( SimulationPath path : paths.values() ) {
            this.contexts.put( path.getName(),
                               new ContextImpl( path.getName(),
                                                this,
                                                root ) );

            capacity += path.getSteps().size();
        }

        if ( capacity == 0 ) {
            return;
        }

        this.queue = new PriorityQueue( capacity,
                                        new Comparator<SimulationStep>() {
                                            public int compare(SimulationStep s1,
                                                               SimulationStep s2) {
                                                return (int) (s1.getDistanceMillis() - s2.getDistanceMillis());
                                            }
                                        } );

        for ( SimulationPath path : paths.values() ) {
            for ( SimulationStep step : path.getSteps() )
                this.queue.add( step );
        }
    }

    public void run() {
        SimulationStep step;
        while ( (step = executeNextStep()) != null ) {

        }
    }

    public SimulationStep executeNextStep() {
        if ( this.queue.isEmpty() ) {
            return null;
        }
        SimulationStepImpl step = (SimulationStepImpl) this.queue.remove();
        SimulationPathImpl path = (SimulationPathImpl) step.getPath();

        Context pathContext = new ResolvingKnowledgeCommandContext( this.contexts.get( path.getName() ) );

        // increment the clock for all the registered ksessions
        for ( StatefulKnowledgeSession ksession : this.ksessions ) {
            SessionPseudoClock clock = (SessionPseudoClock) ksession.getSessionClock();
            long newTime = startTime + step.getDistanceMillis();
            long currentTime = clock.getCurrentTime();

            clock.advanceTime( newTime - currentTime,
                               TimeUnit.MILLISECONDS );
        }

        for ( Command cmd : step.getCommands() ) {
            if ( cmd instanceof NewKieSessionCommand ) {
                // instantiate the ksession, set it's clock and register it
                StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) executionHandler.execute( (GenericCommand) cmd,
                                                                                                         pathContext );
                if ( ksession != null ) {
                    SessionPseudoClock clock = (SessionPseudoClock) ksession.getSessionClock();
                    long newTime = startTime + step.getDistanceMillis();
                    long currentTime = clock.getCurrentTime();
                    clock.advanceTime( newTime - currentTime,
                                       TimeUnit.MILLISECONDS );
                    this.ksessions.add( ksession );
                    this.lastReturnValue = ksession;
                }
            } else if ( cmd instanceof DisposeCommand) {
                this.ksessions.remove(getLastReturnValue());
                executionHandler.execute( (GenericCommand) cmd, pathContext );
            } else if ( cmd instanceof GenericCommand ) {
                this.lastReturnValue = executionHandler.execute( (GenericCommand) cmd,
                                                                 pathContext );
            }
        }

        return step;
    }

    public void setCommandExecutionHandler(CommandExecutionHandler executionHandler) {
        this.executionHandler = executionHandler;
    }

    public Context getContext(String identifier) {
        return this.contexts.get( identifier );
    }

    public Context getRootContext() {
        return this.root;
    }

    public Simulation getSimulation() {
        return this.simulation;
    }
    
    public Object getLastReturnValue() {
        return this.lastReturnValue;
    }

    public static interface CommandExecutionHandler {
        public Object execute(GenericCommand command,
                              Context context);
    }

    public static class DefaultCommandExecutionHandler
        implements
        CommandExecutionHandler {
        public Object execute(GenericCommand command,
                              Context context) {
            return command.execute( context );
        }
    }

    public Object getObject() {
        return lastReturnValue;
    }

	public World getContextManager() {
		return this;
	}

	public String getName() {
		return root.getName();
	}

	public Object get(String identifier) {
		return root.get( identifier );
	}

	public void set(String identifier, Object value) {
		root.set( identifier, value );
	}

	public void remove(String identifier) {
		root.remove( identifier );
	}

    public void dispose() {
        for (StatefulKnowledgeSession ksession : this.ksessions) {
            ksession.dispose();
        }
    }

    //    public static interface CommandExecutorService<T> {
    //        T execute(Command command);
    //    }
    //    
    //    public static class SimulatorCommandExecutorService<T> implements CommandExecutorService {
    //        Map map = new HashMap() {
    //            {
    //               put( KnowledgeBuilderAddCommand.class, null);
    //            }
    //        };
    //        
    //        public  T execute(Command command) {
    //            return null;
    //        }
    //    }
    //    
    //    public static interface CommandContextAdapter {
    //        Context getContext();
    //    }
    //    
    //    public static class KnowledgeBuilderCommandContextAdapter implements CommandContextAdapter {
    //
    //        public Context getContext() {
    //            return new KnowledgeBuilderCommandContext();
    //        }
    //        
    //    }

    //    public void runUntil(SimulationStep step) {
    //        
    //    }
    //    
    //    public void runForTemporalDistance(long distance) {
    //        
    //    }
}
