/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.sworddance.util.perf;


/** This provides a simple way to measure elapsed time.
 * @author Patrick Moore
 */
public class AveLapTimer extends LapTimer {

    private static final long serialVersionUID = -7377079093373138614L;

    private long totalLapTime;
    private long totalLaps;
    private long minimumTime=Long.MAX_VALUE;
    private long maximumTime=Long.MIN_VALUE;
    /**
     * How long since lap was last called. 0 if a pause is in effect.
     */
    @Override
    public long lap()
    {
        long lapTime = super.lap();
        if ( lapTime > this.maximumTime ) {
            this.maximumTime= lapTime;
        }
        if ( lapTime < this.minimumTime ) {
            this.minimumTime = lapTime;
        }
        this.totalLapTime += lapTime;
        this.totalLaps++;
        return lapTime;
    }

    @Override
    public LapTimer reset() {
        super.reset();
        this.totalLaps = this.totalLapTime= this.minimumTime = this.maximumTime =0;
        return this;
    }
    public long aveTime() {
        return this.totalLapTime/this.totalLaps;
    }
    public long minTime() {
        return this.minimumTime;
    }
    public long maxTime() {
        return this.maximumTime;
    }
    public long lapCnt() {
        return this.totalLaps;
    }
    public long totalTime() {
        return this.totalLapTime;
    }
}