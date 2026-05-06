package com.smartcampus.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.smartcampus.entity.Registration;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    /**
     * Generates a Base64-encoded PNG QR code image for a registration.
     * Payload format: REG:<regId>|USER:<studentId>|EVENT:<eventId>
     */
    public String generateQrCodeBase64(Registration registration) {
        String payload = buildPayload(registration);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 250, 250, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the QR payload string for a registration.
     */
    public String buildPayload(Registration registration) {
        return String.format("REG:%d|USER:%d|EVENT:%d",
                registration.getId(),
                registration.getStudent().getId(),
                registration.getEvent().getId());
    }

    /**
     * Parses a scanned QR payload and extracts registration ID.
     * Returns -1 if the payload is invalid.
     */
    public Long extractRegistrationId(String payload) {
        try {
            String[] parts = payload.split("\\|");
            for (String part : parts) {
                if (part.startsWith("REG:")) {
                    return Long.parseLong(part.substring(4));
                }
            }
        } catch (Exception e) {
            return -1L;
        }
        return -1L;
    }
}
