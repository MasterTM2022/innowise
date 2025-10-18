package Skynet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Faction extends Thread {
    private final String name;
    private final Factory factory;
    private final List<Parts> inventory = new ArrayList<>();
    private int robotsBuilt = 0;
    String icon;
    String color;
    private CountDownLatch nightLatch; // will be set before night

    public Faction(String name, Factory factory, String icon, String color) {
        super(name);
        this.name = name;
        this.factory = factory;
        this.icon = icon;
        this.color = color;
    }


    // Set the latch before night
    public void setNightLatch(CountDownLatch latch) {
        this.nightLatch = latch;
    }

    @Override
    public void run() {
        try {
            for (int day = 1; day <= 100 && !Thread.currentThread().isInterrupted(); day++) {
                CountDownLatch currentLatch;

                // Waiting for nightLatch from main
                synchronized (this) {
                    while (nightLatch == null && !Thread.currentThread().isInterrupted()) {
                        this.wait(100); // Short wait to allow for interruption possibility
                    }
                    currentLatch = nightLatch;
                }

                if (currentLatch != null) {
                    collectParts();
                }
                nightLatch = null; // reset for next day
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Method for night actions
    public void collectParts() {
        List<Parts> parts = factory.takeParts(this.icon, this.color); // ← теперь просто берёт и уходит
        inventory.addAll(parts);
        buildRobots();
    }

    private void buildRobots() {
        while (canBuildRobot()) {
            removePart(Parts.HEAD);
            removePart(Parts.TORSO);
            removePart(Parts.HAND);
            removePart(Parts.HAND);
            removePart(Parts.FOOT);
            removePart(Parts.FOOT);

            robotsBuilt++;
            System.out.println(this.color + icon + " " + name + " built robot! Total: " + robotsBuilt + Colors.RESET);
        }
    }

    private boolean canBuildRobot() {
        Map<Parts, Integer> counts = countParts();
        return counts.getOrDefault(Parts.HEAD, 0) >= 1 &&
                counts.getOrDefault(Parts.TORSO, 0) >= 1 &&
                counts.getOrDefault(Parts.HAND, 0) >= 2 &&
                counts.getOrDefault(Parts.FOOT, 0) >= 2;
    }

    private Map<Parts, Integer> countParts() {
        Map<Parts, Integer> counts = new HashMap<>();
        for (Parts part : inventory) {
            counts.merge(part, 1, Integer::sum);
        }
        return counts;
    }

    private void removePart(Parts part) {
        inventory.remove(part);
    }

    public int getRobotsBuilt() {
        return robotsBuilt;
    }
}
