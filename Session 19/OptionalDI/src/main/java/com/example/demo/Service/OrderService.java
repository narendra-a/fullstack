package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired(required = false)
    private DiscountService discountService;

    public String placeOrder() {

        if (discountService != null) {
            return "Order Placed Successfully. " + discountService.getDiscount();
        } else {
            return "Order Placed Successfully. No Discount Available.";
        }
    }
}
