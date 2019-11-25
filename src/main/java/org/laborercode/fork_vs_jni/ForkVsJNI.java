package org.laborercode.fork_vs_jni;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ForkVsJNI {
    @Parameter(names={ "--type", "-t" }, required=true)
    private Type type;

    @Parameter(names={ "--warm-up", "-w" })
    private int warmUp = 0;

    @Parameter(names={ "--loop", "-l" }, required=true)
    private int loop;

    @Parameter(names={ "--concurrent", "-c" }, required=true)
    private int concurrent;

    private ExecutorService executor;

    private enum Type {
        fork, jni
    }

    public static void main(String[] args) throws Exception {
        ForkVsJNI fvj = new ForkVsJNI();
        JCommander.newBuilder()
            .addObject(fvj)
            .build()
            .parse(args);

        fvj.run();
    }

    private void run() {
        Runnable r = (type == Type.fork) ? new Fork() : new JNI();

        init(r, warmUp);

        long start = System.currentTimeMillis();
        run(r, loop);
        long executionTime = System.currentTimeMillis() - start;

        System.out.println("Execution Time: " + executionTime);
    }

    private void init(Runnable r, int warmUp) {
        executor = Executors.newFixedThreadPool(concurrent);
        run(r, warmUp);
    }

    private void run(Runnable r, int loop) {
        CountDownLatch latch = new CountDownLatch(loop);
        for(int i = 0 ; i < loop ; i++) {
            executor.submit(() -> {
                r.run();
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class Fork implements Runnable {
        @Override
        public void run() {
            ProcessBuilder builder = new ProcessBuilder("");
            Process process;
            try {
                process = builder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class JNI implements Runnable {
        @Override
        public void run() {

        }
    }
}
