package com.flash.spring.application;

import com.flash.spring.annotation.AutoWired;
import com.flash.spring.annotation.Component;
import com.flash.spring.annotation.ComponentScan;
import com.flash.spring.annotation.Scope;
import com.sun.xml.internal.ws.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlashSpringApplication {

    public FlashSpringApplication(Class configClass) throws Exception {
        // 扫描bean对象
        scanBeans(configClass);

        // 生成单例bean
        createSingleTonBeans();
    }

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private void scanBeans(Class configClass){
        if(configClass.isAnnotationPresent(ComponentScan.class)) {
            ClassLoader classLoader = FlashSpringApplication.class.getClassLoader();

            String canonicalName = configClass.getCanonicalName();
            String packagePath = canonicalName.replace("." + configClass.getSimpleName(), "");
            String scanPath = packagePath.replace(".", "/");

            URL resource = classLoader.getResource(scanPath);
            String realPath = resource.getFile();

            File file = new File(realPath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (!f.isDirectory() && f.getName().endsWith(".class")) {
                        String classPath = f.getPath().replace("\\", ".");
                        String className = classPath.substring(classPath.indexOf(packagePath), classPath.lastIndexOf("."));
                        try {
                            Class<?> beanClass = classLoader.loadClass(className);
                            if (beanClass.isAnnotationPresent(Component.class)) {
                                // 获取bean名字
                                Component component = beanClass.getAnnotation(Component.class);
                                String beanName = component.value();
                                if("".equals(beanName)){
                                    beanName = StringUtils.decapitalize(beanClass.getSimpleName());
                                }
                                // 获取bean类型 单例或是多例
                                String type = null;
                                if (beanClass.isAnnotationPresent(Scope.class)) {
                                    Scope scope = beanClass.getAnnotation(Scope.class);
                                    type = scope.value();
                                }
                                // 构造bean定义对象
                                BeanDefinition beanDefinition = new BeanDefinition(type, beanClass);
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void createSingleTonBeans() throws Exception {
        for (String s : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(s);
            if(!Scope.PROTOTYPE.equals(beanDefinition.getType())){
                Object bean = singleTonBeans.get(s);
                if (bean == null) {
                    bean = create(beanDefinition);
                    singleTonBeans.put(s, bean);
                }

                initBean(bean);
            }
        }
    }

    private void initBean(Object bean) throws Exception {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoWired.class)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object bean1 = getBean(fieldName);
                field.set(bean, bean1);
                field.setAccessible(false);
            }
        }
    }

    private final Map<String, Object> singleTonBeans = new ConcurrentHashMap<>();

    public Object getBean(String beanName) throws Exception {
        Object bean = null;
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition != null){
            // 是否为多例对象
            if(Scope.PROTOTYPE.equals(beanDefinition.getType())){
                bean = create(beanDefinition);
                initBean(bean);
            }else{
                bean = singleTonBeans.get(beanName);
                if (bean == null) {
                    bean = create(beanDefinition);
                    singleTonBeans.put(beanName, bean);
                }
            }
        }else{
            throw new Exception("Bean instance was not been defined");
        }
        return bean;
    }

    private Object create(BeanDefinition beanDefinition) {
        try {
            return beanDefinition.getBeanClass().getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
