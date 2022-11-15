package com.flash.web;

import com.flash.spring.annotation.ComponentScan;
import com.flash.spring.application.FlashSpringApplication;

@ComponentScan
public class Main {

    public static void main(String[] args) throws Exception {
        FlashSpringApplication application = new FlashSpringApplication(Main.class);

        LoginController loginController = (LoginController) application.getBean("loginController");
        loginController.test();

    }

}
