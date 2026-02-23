package com.example.demo.controller;

import com.example.demo.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeController {

    @GetMapping("/employee")
    public String getEmployee(Model model) {

        Employee emp = new Employee(101, "Sukumar", "IT");

        model.addAttribute("employee", emp);

        return "employee";   // returns employee.html
    }
}
