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

package org.drools.benchmark;

public class BenchmarkResult {

    private final BenchmarkDefinition definition;
    private long duration;
    private long usedMemoryBeforeStart;
    private long usedMemoryAfterEnd;
    private long usedMemoryAfterGC;

    public BenchmarkResult(BenchmarkDefinition definition) {
        this.definition = definition;
    }

    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getUsedMemoryBeforeStart() {
        return usedMemoryBeforeStart;
    }
    public void setUsedMemoryBeforeStart(long usedMemoryBeforeStart) {
        this.usedMemoryBeforeStart = usedMemoryBeforeStart;
    }

    public long getUsedMemoryAfterEnd() {
        return usedMemoryAfterEnd;
    }
    public void setUsedMemoryAfterEnd(long usedMemoryAfterEnd) {
        this.usedMemoryAfterEnd = usedMemoryAfterEnd;
    }

    public long getUsedMemoryAfterGC() {
        return usedMemoryAfterGC;
    }
    public void setUsedMemoryAfterGC(long usedMemoryAfterGC) {
        this.usedMemoryAfterGC = usedMemoryAfterGC;
    }

    public long memoryUsedByBenchmark() {
        return usedMemoryAfterEnd - usedMemoryBeforeStart;
    }

    public long unfreedMemory() {
        return Math.max(0L, usedMemoryAfterGC - usedMemoryBeforeStart);
    }

    public String getDescription() {
        return definition.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*** Execution of " + definition + "\n");
        sb.append("Done in " + duration + " msecs" + "\n");
        sb.append("Memory used by benchmark " + memoryUsedByBenchmark() + " bytes" + "\n");
        sb.append("Memory unfreed after benchmark run " + unfreedMemory() + " bytes" + "\n");
        return sb.toString();
    }
}
