package com.smartcampus.controller;

import com.smartcampus.entity.Registration;
import com.smartcampus.service.QrCodeService;
import com.smartcampus.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
public class QrController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private QrCodeService qrCodeService;

    /**
     * Show the QR code for a registration (student view).
     * Accessible after quick-register redirect or from My Events.
     */
    @GetMapping("/student/qr/{registrationId}")
    public String showQrCode(@PathVariable Long registrationId,
                             HttpSession session,
                             Model model) {
        String email = (String) session.getAttribute("studentEmail");

        Optional<Registration> opt = registrationService.getRegistrationById(registrationId);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Registration not found.");
            return "error";
        }

        Registration reg = opt.get();

        // Verify access: session email must match the registration's student email
        if (email == null || !reg.getStudent().getEmail().equalsIgnoreCase(email)) {
            // No valid session — redirect to login
            return "redirect:/student/login";
        }

        String qrBase64 = qrCodeService.generateQrCodeBase64(reg);
        model.addAttribute("qrBase64", qrBase64);
        model.addAttribute("registration", reg);
        model.addAttribute("event", reg.getEvent());
        model.addAttribute("student", reg.getStudent());
        return "qr_code";
    }

    /**
     * Admin QR scanner page.
     */
    @GetMapping("/admin/checkin")
    public String checkinPage(Model model) {
        model.addAttribute("message", null);
        return "admin_checkin";
    }

    /**
     * Process QR code scan result (POST from scanner UI).
     */
    @PostMapping("/admin/checkin")
    public String processCheckin(@RequestParam("qrData") String qrData,
                                 RedirectAttributes redirectAttributes) {
        String result = registrationService.checkIn(qrData.trim());
        if (result.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("successMessage", result);
        } else if (result.startsWith("DUPLICATE")) {
            redirectAttributes.addFlashAttribute("warningMessage", result);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result);
        }
        return "redirect:/admin/checkin";
    }

    /**
     * AJAX endpoint for real-time QR scan results.
     */
    @PostMapping("/api/checkin")
    @ResponseBody
    public ResponseEntity<Map<String, String>> apiCheckin(@RequestBody Map<String, String> body) {
        String qrData = body.getOrDefault("qrData", "").trim();
        String result = registrationService.checkIn(qrData);
        String status = result.startsWith("SUCCESS") ? "success"
                : result.startsWith("DUPLICATE") ? "warning" : "error";
        return ResponseEntity.ok(Map.of("status", status, "message", result));
    }
}
