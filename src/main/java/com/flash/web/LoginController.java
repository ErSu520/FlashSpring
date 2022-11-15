package com.flash.web;

import com.flash.spring.annotation.AutoWired;
import com.flash.spring.annotation.Component;
import com.flash.spring.annotation.Scope;

@Scope(Scope.PROTOTYPE)
@Component
public class LoginController {

    @AutoWired
    private LoginService loginService;

    public void test(){
        loginService.test();
    }

}
