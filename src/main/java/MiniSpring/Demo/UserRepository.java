package MiniSpring.Demo;

import MiniSpring.Autowired;
import MiniSpring.Component;

// Repository
@Component
class UserRepository {
    @Autowired
    private DatabaseService db;

    public void save() {
        db.connect();
        System.out.println("💾 User saved");
    }
}