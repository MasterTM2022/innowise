package MiniSpring.Demo;

import MiniSpring.Component;
import MiniSpring.InitializingBean;

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

