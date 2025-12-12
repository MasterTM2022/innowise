package com.innowise.JavaCore.MiniSpring;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class MiniApplicationContext implements AutoCloseable {
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Set<Class<?>> beanClasses = new HashSet<>();

    public MiniApplicationContext(String packageName) throws Exception {
        scanPackage(packageName);
        instantiateBeans();
        injectDependencies();
        initializeBeans();
    }

    private void scanPackage(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            findClassesInDirectory(file, packageName);
        }
    }

    private void findClassesInDirectory(File dir, String packageName) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Component.class)) {
                        beanClasses.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load class: " + className, e);
                }
            }
        }
    }

    private void instantiateBeans() throws Exception {
        for (Class<?> clazz : beanClasses) {
            Scope scope = clazz.getAnnotation(Scope.class);
            boolean isPrototype = scope != null && "prototype".equals(scope.value());

            if (!isPrototype) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object instance = constructor.newInstance();
                beans.put(clazz, instance);
            }
        }
    }

    private void injectDependencies() throws Exception {
        for (Object bean : beans.values()) {
            Class<?> clazz = bean.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);

                    Class<?> fieldType = field.getType();
                    Object dependency = getBean(fieldType);
                    if (dependency != null) {
                        field.set(bean, dependency);
                    } else {
                        throw new IllegalStateException(
                                "No bean found for injection into field: " + field.getName());
                    }
                }
            }
        }
    }

    private void initializeBeans() throws Exception {
        for (Object bean : beans.values()) {
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        for (Class<?> clazz : beanClasses) {
            if (type.isAssignableFrom(clazz)) {
                Scope scope = clazz.getAnnotation(Scope.class);
                boolean isPrototype = scope != null && "prototype".equals(scope.value());

                if (isPrototype) {
                    try {
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        Object instance = constructor.newInstance();

                        injectDependenciesInto(instance);
                        if (instance instanceof InitializingBean) {
                            ((InitializingBean) instance).afterPropertiesSet();
                        }

                        return (T) instance;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create prototype bean", e);
                    }
                } else {
                    return (T) beans.get(clazz);
                }
            }
        }
        return null;
    }

    private void injectDependenciesInto(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Object dependency = getBean(field.getType());
                if (dependency != null) {
                    field.set(instance, dependency);
                }
            }
        }
    }

    public void close() {
    }
}