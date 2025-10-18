package Skynet;

import java.util.concurrent.CountDownLatch;

public class ArmsRace {
    public static void main(String[] args) throws InterruptedException {
        Factory factory = new Factory();

        Faction world = new Faction("World", factory, "🎯", Colors.RED);
        Faction wednesday = new Faction("Wednesday", factory, "ℹ️", Colors.BLUE);

        world.start();
        wednesday.start();

        CountDownLatch nightLatch = new CountDownLatch(10);

        world.setNightLatch(nightLatch);
        wednesday.setNightLatch(nightLatch);

        for (int day = 1; day <= 100; day++) {
            System.out.println("\n📅 Day " + day);

            nightLatch = new CountDownLatch(10);

            // Set the latch — the factions will wake up
            synchronized (world) {
                world.setNightLatch(nightLatch);
            }
            synchronized (wednesday) {
                wednesday.setNightLatch(nightLatch);
            }

            // The factory produces and waits
            factory.produce(nightLatch);

            Thread.sleep(50); // Pause between days
        }

        String worldColor = world.color;
        String wednesdayColor = wednesday.color;
        String worldIcon = world.icon;
        String wednesdayIcon = wednesday.icon;



        factory.stop();
        world.join();
        wednesday.join();

        // Results
        int resWorld = 0;
        int resWednesday = 0;
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