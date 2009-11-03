package com.sworddance.util.perf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
     * This will store a collection of LapTimers. All the lap name for the collected LapTimers
     * should be identical for the writeCVS methods to work correctly.
     *
     * @author pat
     */
    public class LapTimerCollection {
        private String collectionName;
        private final List<LapTimer> lapTimers;
        private int maxTimers;
        private Comparator<LapTimer> comparator;
        private int lapHistorySize=100;
        /**
         * All LapTimers created by this Collection are remotable.
         */
        private boolean remotable;

        /**
         * Create a collection of LapTimers with the supplied name
         *
         * @param collectionName
         */
        public LapTimerCollection(String collectionName) {
            this.collectionName = collectionName;
            this.lapTimers = new ArrayList<LapTimer>();
            this.maxTimers = Integer.MAX_VALUE;
        }

        public LapTimerCollection(String collectionName, int maxTimers) {
            this.collectionName = collectionName;
            this.lapTimers = new ArrayList<LapTimer>(maxTimers);
            this.maxTimers = (maxTimers > 0) ? maxTimers : 1;
        }

        /**
         * @param collectionName
         * @param maxTimers
         * @param comparator     use this comparator to always sort the list and drop the 'least'
         *                       LapTimer as defined by the comparator when adding to a full list.
         */
        public LapTimerCollection(String collectionName, int maxTimers, Comparator<LapTimer> comparator) {
            this(collectionName, maxTimers);
            this.comparator = comparator;
        }
        public void setLapHistorySize(int lapHistorySize) {
            this.lapHistorySize = lapHistorySize;
        }

        public List<LapTimer> getTimers() {
            return this.lapTimers;
        }

        /**
         * This will add a LapTimer to the Collection. If there is now more than
         * the allowed number of LapTimers in the Collection, the Collection will be sorted
         * and the 'least' LapTimer will be removed as determined by the Collection's
         * comparator. The passed LapTimer will never be removed.
         *
         * @param lapTimer
         */
        public void add(LapTimer lapTimer) {
            if (this.maxTimers != Integer.MAX_VALUE && this.lapTimers.size() == this.maxTimers) {
                this.sort(this.comparator);
                for (int i = 0; i < this.lapTimers.size(); i++) {
                    LapTimer t = this.lapTimers.get(i);
                    if (!t.isRunning()) {
                        this.lapTimers.remove(i);
                        break;
                    }
                }
            }
            this.lapTimers.add(lapTimer);
        }

        /**
         * Get a new LapTimer and add it to this collection.
         *
         * @param name all LapTimers in a Collection should have a name so that
         *             they can be tracked after sorting.
         * @return the create LapTimer
         * @see LapTimer#LapTimer(String)
         */
        public LapTimer getNewTimer(String name) {
            LapTimer timer = new LapTimer(name);
            this.add(timer);
            return timer;
        }

        public LapTimer getNewTimer() {
            String name = this.collectionName + " timer #" + (this.lapTimers.size() + 1);
            return this.getNewTimer(name);
        }

        /**
         * Get a new LapTimer and add it to this collection. The timer is also assigned to this thread.
         * Use {LapTimer.popThreadTimer()}
         *
         * @param name all LapTimers in a Collection should have a name so that
         *             they can be tracked after sorting.
         * @return the create LapTimer
         * @see LapTimer#pushNewThreadTimer(String)
         */
        public LapTimer pushNewThreadTimer(String name) {
            LapTimer timer = new LapTimer(name, null, this.lapHistorySize);
            LapTimer.pushThreadTimerAndStart(timer);
            this.add(timer);
            timer.setRemotable(this.remotable);
            timer.start();
            return timer;
        }

        /**
         * The name of the LapTimer is the name of this collection combined with the number of
         * LapTimers in this collection (including this LapTimer)
         *
         * @return the new laptimer.
         * @see #pushNewThreadTimer(String)
         */
        public LapTimer pushNewThreadTimer() {
            String name = this.collectionName + " timer #" + (this.lapTimers.size() + 1);
            return this.pushNewThreadTimer(name);
        }

        /**
         * Write out the stored LapTimers in Excel CVS format
         *
         * @param fileName
         * @throws IOException
         */
        public void writeCSV(String fileName) throws IOException {
            FileOutputStream f = new FileOutputStream(fileName);
            OutputStreamWriter f0 = new OutputStreamWriter(f);
            this.writeCSV(f0);
            f0.close();
            f.close();
        }

        public void writeCSV(Writer output) throws IOException {
            this.writeCSV(output, true);
        }

        /**
         * Write out the collection of LapTimers to output.  In CSV (Excel)
         * format.
         *
         * @param output    Writer to use to output CSV information
         * @param firstLine if true a header line for column names is displayed
         * @throws IOException
         */
        public void writeCSV(Writer output, boolean firstLine)
                throws IOException {
            if (firstLine) {
                LapTimer top = this.lapTimers.get(0);
                String[] names = top.getLapNames();
                output.write("LapTimerName,Start Time,Hop Count,Total Time,");
                for (String element : names) {
                    if (element != null) {
                        output.write(element);
                        output.write(',');
                    }
//                    output.write("Averaged Time,");
                }
                output.write("\n");
            }
            for (LapTimer lt : this.lapTimers) {
                output.write(lt.toCSV());
                output.write('\n');
            }
        }

        /**
         * Create the CVS format into a string
         *
         * @return the CVS formated string
         * @see #writeCSV(Writer)
         */
        public String toCSV() {
            try {
                StringWriter sw = new StringWriter();
                this.writeCSV(sw);
                return sw.toString();
            } catch (IOException e) {
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(this.collectionName);
            sb.append(":[");
            for (LapTimer lapTimer : this.lapTimers) {
                sb.append(lapTimer.toString()).append('\n');
            }
            sb.append(']');
            return sb.toString();
        }

        /**
         * Sort the list of LapTimers. Does not sort the stored list.
         *
         * @param comp
         * @return sorted list of LapTimers
         */
        public List<LapTimer> sort(Comparator<LapTimer> comp) {
            // need to make copy so that the use of maxTimers
            // continues to work correctly by dropping oldest
            ArrayList<LapTimer> timers = new ArrayList<LapTimer>(this.lapTimers);
            Collections.sort(timers, comp);
            return timers;
        }

        /**
         * Sort the LapTimers based on the Comparator passed into the
         * constructor.
         *
         * @return sorted list of laptimers
         */
        public List<LapTimer> sort() {
            return sort(this.comparator);
        }

        /**
         * Clear the list of stored laptimers.
         */
        public void clear() {
            this.lapTimers.clear();
        }

        public void setRemotable(boolean remotable) {
            this.remotable = remotable;
        }

        public boolean isRemotable() {
            return remotable;
        }
    }