package com.saferide.SaferideBackend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QRCodeService {

    /**
     * Generates a QR Code containing the studentID and returns the Base64 encoded PNG image.
     */
    public String generateQRCodeBase64(String studentId) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter(); //ZXing library tool that generates QR codes
            BitMatrix bitMatrix = qrCodeWriter.encode(studentId, BarcodeFormat.QR_CODE, 250, 250); //encode studentId into QR matrix

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream(); //create output stream,instead of saving image to file,store it in memory (RAM/memory container)
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream); //convert matrix into a PNG image
            byte[] pngData = pngOutputStream.toByteArray(); //Extracts raw image data(the actual QR image)

            // Return Base64 encoded image string(convert to text string/easy to send via API,sotre in DB,atttach in email)
            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) { //encoding, memory issues(throws runtime error and stops execution safely)
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}
