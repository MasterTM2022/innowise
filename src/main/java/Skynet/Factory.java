package Skynet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Factory {
    private final List<Parts> parts = new ArrayList<>();
    private final Random random = new Random();
    private volatile boolean running = true;
    private CountDownLatch nightLatch; // CountDown for waiting for factions


    // Producing 10 parts per day
    public void produce(CountDownLatch nightLatch) throws InterruptedException {
        if (!running) return;

        // Step 1: produce 10 parts
        for (int i = 0; i < 10; i++) {
            parts.add(randomPart());
        }
        System.out.println("🏭 Factory produced 10 parts. Total parts in inventory: " + parts.size());

        // Step 2: Set the latch for two factions
        this.nightLatch = nightLatch;

        // Step 3: wait for finishing collecting parts by factions
        nightLatch.await();
        Thread.sleep(100);

        System.out.println("🌙 Night is over. All factions have collected their parts. Proceeding to the next day.");
    }

    private Parts randomPart() {
        Parts[] values = Parts.values();
        return values[random.nextInt(values.length)];
    }

    // The faction takes up to 5 parts
    public synchronized List<Parts> takeParts(String icon, String color) {
        List<Parts> taken = new ArrayList<>();

        // Take up to 5, but only if available
        int toTake = Math.min(5, parts.size());

        for (int i = 0; i < toTake; i++) {
            taken.add(parts.removeFirst());
            if (nightLatch != null) {
                nightLatch.countDown();
            }
        }

        System.out.println(color + icon + " " +Thread.currentThread().getName() +
                " took up " + taken.size() + " parts." + Colors.RESET);

        notifyAll();
        return taken;
    }

    public synchronized void stop() {
        running = false;
        notifyAll();
    }
}
