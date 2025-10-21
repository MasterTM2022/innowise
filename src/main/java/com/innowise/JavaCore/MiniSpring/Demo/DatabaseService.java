package com.innowise.JavaCore.MiniSpring.Demo;

import com.innowise.JavaCore.MiniSpring.Component;
import com.innowise.JavaCore.MiniSpring.InitializingBean;

// Service
@Component
class DatabaseService implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        System.out.println("✅ DatabaseService initialized");
    }

    public void connect() {
        System.out.println("🔌 Connected to DB");
    }
}

