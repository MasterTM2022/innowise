package MiniSpring.Demo;

import MiniSpring.MiniApplicationContext;

public class MiniSpringDemo {
    public static void main(String[] args) throws Exception {
        try (MiniApplicationContext ctx = new MiniApplicationContext("MiniSpring.Demo")) {

            // Singleton
            DatabaseService db = ctx.getBean(DatabaseService.class);
            db.connect();

            // Singleton with injection
            UserRepository repo = ctx.getBean(UserRepository.class);
            repo.save();

            // Prototype — каждый вызов даёт новый объект
            UserController c1 = ctx.getBean(UserController.class);
            UserController c2 = ctx.getBean(UserController.class);

            System.out.println("c1 == c2? " + (c1 == c2)); // false

            c1.createUser();
            c2.createUser();
        }
    }
}
