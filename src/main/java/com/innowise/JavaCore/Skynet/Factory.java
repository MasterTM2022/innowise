package com.innowise.JavaCore.Skynet;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;

public class Factory extends Thread {
    private final BlockingQueue<Parts> queue;
    private final Random random = new Random();
    private final CyclicBarrier barrier;

    public Factory(BlockingQueue<Parts> queue, CyclicBarrier barrier) {
        this.queue = queue;
        this.barrier = barrier;
    }


    @Override
    public void run() {
        try {
            Random random = new Random();
            for (int i = 1; i <=ArmsRace.TOTAL_DAYS; i++) {
                System.out.println("\n📅 Day #" + i + " started!!!");
                int partSize = random.nextInt(10) + 1;
                for (int j = 0; j < partSize; j++) {
                    Parts part = randomPart();
                    queue.add(part);
                    System.out.println("🏭 Factory produced part #" + (j + 1) + " of " + partSize + ": " + part.name());
                }
                System.out.println("🏭 Factory produced " + queue.size() + " parts.");

                barrier.await();

                while (!queue.isEmpty()) {
                    Thread.sleep(100);
                }

                if (i != ArmsRace.TOTAL_DAYS) {
                    System.out.println("🌙 Night is over. All factions have collected their parts. Proceeding to the next day.");
                } else {
                    System.out.println("🌙 Last night is over. All factions have collected their parts. ArmsRase has finished!");
                }
            }

            Thread.sleep(100);
            ArmsRace.IS_RUNNING = false;
            barrier.await();

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Parts randomPart() {
        Parts[] values = Parts.values();
        return values[random.nextInt(values.length)];
    }
}
