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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import static com.sworddance.util.perf.ThreadHistoryTrackerFormatter.*;

public class CSVThreadHistoryTrackerFormatter implements Iterator<String> {
    private int lineCount;

    private int columnCount;

    private int singleThreadTimeColumn;

    private int multiThreadTimeColumn;

    private boolean doneWithData;

    private int postDataLine;

    private String runtimeCol;

    private String singleThreadedCol;

    private Iterator<Map.Entry<String,long[]>> taskSummary;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private ThreadHistoryTrackerFormatter threadHistoryTrackerFormatter;
    /**
     * @param tracker
     */
    public CSVThreadHistoryTrackerFormatter(ThreadHistoryTracker tracker) {
        this.threadHistoryTrackerFormatter = new ThreadHistoryTrackerFormatter(tracker);
    }
    public String next() {
        StringBuilder sb = new StringBuilder(100);
        if (doneWithData) {
            threadUtilizationSummary(sb);
        } else {
            String[] output = this.threadHistoryTrackerFormatter.next();
            lineCount++;
            for (int i = 0; i < output.length; i++) {
                if (i == 0) {
                    // so excel does not misunderstand the time string
                    sb.append("'");
                }
                // '\n' will screw up the way CVS files are interpreted.
                sb.append(output[i].replace('\n', ' '));
                sb.append(",");
            }
            if (lineCount == 1) {
                sb.append("Single-Threaded Time,All Threads Used Time,");
                columnCount = output.length;
                singleThreadTimeColumn = columnCount;
                multiThreadTimeColumn = columnCount + 1;
            } else {
                String deltaTimeCol = getColumn(DELTA_TIME_COL) + lineCount;
                String activeThreadCol = getColumn(ACTIVE_THREADS_COL)
                        + lineCount;
                String nextActiveThreadCol = getColumn(ACTIVE_THREADS_COL)
                        + (lineCount + 1);
                // formula to calculate time just being single threaded
                sb.append("\"=IF(OR(").append(activeThreadCol)
                        .append("=0,AND(");
                sb.append(activeThreadCol).append("=1,");
                sb.append(nextActiveThreadCol).append("=1)),");
                sb.append(deltaTimeCol).append(",0)\",");
                // formula to calculate time running at max
                sb.append("\"=IF(AND(");
                sb.append(activeThreadCol).append(">=").append(this.threadHistoryTrackerFormatter.maxThreads - 1);
                sb.append(',').append(nextActiveThreadCol);
                sb.append('=').append(this.threadHistoryTrackerFormatter.maxThreads).append("),");
                sb.append(deltaTimeCol).append(",0)\"");
            }
        }
        return sb.toString();
    }
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void threadUtilizationSummary(StringBuilder sb) {
        String column;
        postDataLine++;
        int linePos = lineCount + postDataLine;
        switch (postDataLine) {
        case 1:
        case 6:
            sb.append(",");
            break;
        case 2:
            column = getColumn(DELTA_TIME_COL);
            sb.append("Total time for run,\"=SUM(");
            sb.append(column).append("2:");
            sb.append(column).append(lineCount).append(")\"");
            runtimeCol = "B" + linePos;
            column = getColumn(SEQUENTIAL_TIME);
            sb.append(",\"=SUM(");
            sb.append(column).append("2:");
            sb.append(column).append(lineCount).append(")\",=C");
            sb.append(linePos).append("/B").append(linePos);
            break;
        case 3:
            column = getColumn(singleThreadTimeColumn);
            sb.append("Total time single threaded,\"=SUM(");
            sb.append(column).append("2:");
            sb.append(column).append(lineCount).append(")\",");
            singleThreadedCol = "B" + linePos;
            sb.append("\"=").append(singleThreadedCol).append("/");
            sb.append(runtimeCol).append('"');
            break;
        case 4:
            column = getColumn(multiThreadTimeColumn);
            sb.append("Total time max multi-threaded,\"=SUM(");
            sb.append(column).append("2:");
            sb.append(column).append(lineCount).append(")\",");
            sb.append("\"=B").append(linePos).append("/");
            sb.append(runtimeCol).append('"');
            break;
        case 5:
            sb.append("Total time multi-threaded,\"=").append(runtimeCol);
            sb.append("-").append(singleThreadedCol);
            sb.append("\",");
            sb.append("\"=B").append(linePos).append("/");
            sb.append(runtimeCol).append('"');
            break;
        case 7:
            sb.append("Task,Single-Threaded,Multi-Threaded");
            taskSummary = this.threadHistoryTrackerFormatter.timeMap.entrySet().iterator();
            break;
        default:
            if (taskSummary != null && taskSummary.hasNext()) {
                Map.Entry<String,long[]> entry = taskSummary.next();
                long[] times = entry.getValue();
                sb.append(entry.getKey()).append(",").append(times[0]);
                sb.append(",").append(times[1]);
            }
        }
    }
    public boolean hasNext() {
        doneWithData = !this.threadHistoryTrackerFormatter.hasNext();
        if (doneWithData && taskSummary != null) {
            return taskSummary.hasNext();
        } else {
            return true;
        }
    }

    public void dumpToFile(String fileName) {
        File f = new File(fileName);

        FileWriter writer = null;
        try {
            f.delete();
            writer = new FileWriter(f);
            for (; this.hasNext();) {
                writer.write(this.next().toString());
                writer.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getColumn(int column) {
        int tens = column / 26;
        int ones = column % 26;

        if (tens >= LETTERS.length()) {
            return "--";
        } else if (tens > 0 && tens < LETTERS.length()) {
            return LETTERS.substring(tens, tens + 1)
                    + LETTERS.substring(ones, ones + 1);
        } else {
            return LETTERS.substring(ones, ones + 1);
        }
    }

}
