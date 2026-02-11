package com.example.autowiredapp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.autowiredapp.service.HelloService;

@RestController
@RequestMapping("/api")
public class HelloController {

    // 🔹 Field Injection using @Autowired
    @Autowired
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello() {
        return helloService.sayHello();
    }

    @GetMapping("/greet/{name}")
    public String greet(@PathVariable String name) {
        return helloService.greetUser(name);
    }
}