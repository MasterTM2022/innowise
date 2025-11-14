package com.innowise.JavaCore.MiniSpring.Demo;

import com.innowise.JavaCore.MiniSpring.Autowired;
import com.innowise.JavaCore.MiniSpring.Component;

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