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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pat
 * TODO: needs to actually check result.
 */
@Test(enabled=false)
public class TestLapTimer extends Assert {

    public void testLapTimer() {
        LapTimer t = new LapTimer("simple");
        t.start();
        t.stop();
        System.out.println(t);
    }
    public void testLapTimer1Lap1() {
        LapTimer t = new LapTimer("1lap1").start();
        t.lap();
        t.stop();
        System.out.println(t);
    }
    public void testLapTimer1Lap2() {
        LapTimer t = new LapTimer("1lap2").start();
        System.out.println(t.toLapString("lap name"));
        t.stop();
        System.out.println(t);
    }
    public void testLapTimer1LapWithName() {
        LapTimer t = new LapTimer("1lap").start();
        t.lap("lap name");
        t.stop();
        System.out.println(t);
    }
    public void testPause() throws Exception {
        LapTimer t = new LapTimer("pause test");
        t.start();
        Thread.sleep(100);
        t.pause();
        Thread.sleep(100);
        t.cont();
        t.stop();
        System.out.println(t);
    }
    public void testCSV() {
        LapTimer t = new LapTimer("1lap").start();
        t.lap("lap name");
        t.stop();
        System.out.println(t.toCSV());
    }
    public void testCollection() throws IOException {
        LapTimerCollection c = new LapTimerCollection("lap collection");
        LapTimer t = c.getNewTimer().start();
        t.lap("lap1");
        t.stop();
        t =  c.getNewTimer().start();
        t.lap("lap1");
        t.stop();
        System.out.println(c);
        OutputStreamWriter w = new OutputStreamWriter(System.out);
        c.writeCSV(w);
        w.flush();
    }
    public void testCSVOutputFormat() throws Exception {
        LapTimerCollection c = new LapTimerCollection("lap collection");
        LapTimer t = c.getNewTimer("first").start();
        Thread.sleep(400);
        t.lap("lap1");
        Thread.sleep(200);
        t.stop("lap2");
        t =  c.getNewTimer("second").start();
        Thread.sleep(100);
        t.lap("lap1");
        t.stop("lap2");
        System.out.println("testcollection="+c);
        OutputStreamWriter w = new OutputStreamWriter(System.out);
        c.writeCSV(w);
        w.write(c.toCSV());
        w.flush();
        c.writeCSV("test.csv");
    }
    public void testStartPause() throws InterruptedException {
        LapTimer t = new LapTimer();
        t.cont();
        Thread.sleep(500);
        t.lap();
        Thread.sleep(500);
        t.pause();
        Assert.assertTrue(t.elapsed() < 1100 && t.elapsed() > 990);
        // should not be counted in lap time
        Thread.sleep(500);
        t.cont();
        Thread.sleep(500);
        long time = t.stop().getPrevLapTime();
        Assert.assertTrue(time > 990 && time < 1100);
        Assert.assertTrue(t.elapsed() < 1600 && t.elapsed() > 1400);
    }
    public void testStreaming() throws Exception {
        LapTimer t = LapTimer.getThreadTimer();
        t.start();
        Thread.sleep(100);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(bo);
        o.writeObject(t);
        o.close();
        Thread.sleep(300);
        System.out.println("Sending");
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        LapTimer t1 = (LapTimer)oi.readObject();
        Assert.assertTrue(t1.isRunning());
        t1.stop();
        System.out.println("sent="+t1);
    }
    /**
     * Test to make sure that a LapTimer can traverse multiple remoting calls.
     * @throws Exception
     */
    public void testSerialization() throws Exception {
        FakeRecipient f = new FakeRecipient(0,0);
        f.start();
        LapTimer t = new LapTimer().start();
        Socket outbound = new Socket("localhost", f.getIncomingPort());
        ObjectOutputStream out = new ObjectOutputStream(outbound
                .getOutputStream());
        out.writeObject(t);
        out.flush();
        ObjectInputStream in = new ObjectInputStream(outbound.getInputStream());
        LapTimer t1 = (LapTimer) in.readObject();
        System.out.println("sent="+t);
        System.out.println("Rcv="+t1);
        Assert.assertEquals(t,t1);
        f.stop();
        outbound.close();
    }
}
class FakeRecipient implements Runnable {
    private ServerSocket serverSocket;
    private boolean running;
    private int outboundport;
    private Thread thread;
    FakeRecipient(int serverport, int outboundport) throws IOException {
        this.serverSocket = new ServerSocket(serverport);
        this.outboundport = outboundport;
    }
    int getIncomingPort() {
        return this.serverSocket.getLocalPort();
    }
    void start() {
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.running = true;
        this.thread.start();
    }
    void stop() {
        this.running = false;
        this.thread.interrupt();
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        while (this.running) {
            try {
                Socket s = this.serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                LapTimer tin = (LapTimer) in.readObject();
                LapTimer tout;
                if (outboundport != 0) {
                    // send it on!
                    Socket s1 = new Socket("localhost", this.outboundport);
                    ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream());
                    out.writeObject(tin);
                    out.close();
                    ObjectInputStream in1 = new ObjectInputStream(s1.getInputStream());
                    tout = (LapTimer)in1.readObject();
                    s1.close();
                } else {
                    tout = tin;
                }
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.writeObject(tout);
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
