package com.flash.spring.application;

import lombok.Data;

@Data
public class BeanDefinition {

    private String type;

    private Class beanClass;

    public BeanDefinition(String name, Class type){
        this.type = name;
        this.beanClass = type;
    }

}
