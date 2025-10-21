package com.innowise.JavaCore.MiniSpring.Demo;

import com.innowise.JavaCore.MiniSpring.Autowired;
import com.innowise.JavaCore.MiniSpring.Component;
import com.innowise.JavaCore.MiniSpring.Scope;

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