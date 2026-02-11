package com.example.paymentapp.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.paymentapp.Service.PaymentService;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    // 🔹 Constructor Injection
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/pay/{amount}")
    public String makePayment(@PathVariable double amount) {
        return paymentService.processPayment(amount);
    }
}
