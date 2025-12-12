package com.innowise.JavaCore.Skynet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Faction extends Thread {
    private final String name;
    private final BlockingQueue<Parts> queue;
    private final CyclicBarrier barrier;
    private final List<Parts> inventory;
    String icon;
    String color;
    private int robotsBuilt = 0;

    public Faction(String name, BlockingQueue<Parts> queue, CyclicBarrier barrier, String icon, String color) {
        super(name);
        this.name = name;
        this.queue = queue;
        this.barrier = barrier;
        this.inventory = new ArrayList<>();
        this.icon = icon;
        this.color = color;
    }

    @Override
    public void run() {
        try {
            while (ArmsRace.IS_RUNNING) {
                barrier.await();

                List<Parts> taken = new ArrayList<>();
                while (!queue.isEmpty() && taken.size() < 5) {
                    Parts part = queue.poll();
                    if (part == null) {
                        break;
                    } else {
                        taken.add(part);
                        System.out.println(color + icon + " " + Thread.currentThread().getName() + " took up next part: " + taken.getLast().name() + Colors.RESET);
                    }
                }
                if (ArmsRace.IS_RUNNING) {
                    System.out.println(color + icon + " " + Thread.currentThread().getName() + " took up " + taken.size() + " parts." + Colors.RESET);
                    inventory.addAll(taken);
                }
                if (canBuildRobot()) {
                    buildRobots();
                }
            }
        } catch (BrokenBarrierException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
