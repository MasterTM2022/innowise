package MiniSpring.Demo;

import MiniSpring.Autowired;
import MiniSpring.Component;
import MiniSpring.Scope;

// Controller
@Component
@Scope("prototype") // Новый экземпляр каждый раз
class UserController {
    @Autowired
    private UserRepository userRepo;

    public void createUser() {
        System.out.println("🆕 Creating user...");
        userRepo.save();
    }
}