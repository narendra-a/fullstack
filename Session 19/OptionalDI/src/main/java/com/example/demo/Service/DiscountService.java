package com.example.demo.Service;

import org.springframework.stereotype.Component;

@Component   // Comment this line to test absence
public class DiscountService {

    public String getDiscount() {
        return "10% Discount Applied!";
    }
}
