package com.innowise.JavaCore.Skynet;

import java.util.concurrent.*;

public class ArmsRace {

    public static final int TOTAL_DAYS = 15;
    public static volatile boolean IS_RUNNING = true;


    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<Parts> queue = new ArrayBlockingQueue<>(10);
        CyclicBarrier barrier = new CyclicBarrier(3);

        Factory factory = new Factory(queue, barrier);
        Faction world = new Faction("World", queue, barrier, "🎯", Colors.RED);
        Faction wednesday = new Faction("Wednesday", queue, barrier, "ℹ️", Colors.BLUE);


        factory.start();
        world.start();
        wednesday.start();

        String worldColor = world.color;
        String wednesdayColor = wednesday.color;
        String worldIcon = world.icon;
        String wednesdayIcon = wednesday.icon;

        world.join();
        wednesday.join();

        // Results
        int resWorld;
        int resWednesday;
        System.out.println("\n🏆 Results:");
        System.out.println(worldIcon + " World: " + (resWorld = world.getRobotsBuilt()) + " robots");
        System.out.println(wednesdayIcon + " Wednesday: " + (resWednesday = wednesday.getRobotsBuilt()) + " robots");

        if (resWorld == resWednesday) {
            System.out.println("Draw!");
        } else if (resWorld > resWednesday) {
            System.out.println(worldColor + worldIcon + " World wins!" + Colors.RESET);
        } else {
            System.out.println(wednesdayColor + wednesdayIcon + " Wednesday wins!" + Colors.RESET);
        }
    }
}