package com.project.qrgen.qrcode;



import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
public class QRServiceController {

    @GetMapping("/api/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/qrcode")
    public ResponseEntity<?> qrcode(
            @RequestParam String contents,
            @RequestParam(defaultValue = "250") int size,
            @RequestParam(defaultValue = "png") String type,
            @RequestParam(defaultValue = "L") char correction
    ) throws IOException {

        if (contents == null || contents.isBlank()) {
            ErrorResponse errorResponse = new ErrorResponse("Contents cannot be null or blank");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }


        if (size < 150 || size > 350) {
            ErrorResponse errorResponse = new ErrorResponse("Image size must be between 150 and 350 pixels");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        ErrorCorrectionLevel errorCorrectionLevel;
        switch (correction) {
            case 'L':
                errorCorrectionLevel = ErrorCorrectionLevel.L;
                break;
            case 'M':
                errorCorrectionLevel = ErrorCorrectionLevel.M;
                break;
            case 'Q':
                errorCorrectionLevel = ErrorCorrectionLevel.Q;
                break;
            case 'H':
                errorCorrectionLevel = ErrorCorrectionLevel.H;
                break;
            default:
                ErrorResponse errorResponse = new ErrorResponse("Permitted error correction levels are L, M, Q, H");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (!type.equalsIgnoreCase("png") && !type.equalsIgnoreCase("jpeg") && !type.equalsIgnoreCase("gif")) {
            ErrorResponse errorResponse = new ErrorResponse("Only png, jpeg and gif image types are supported");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
            BitMatrix bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, type.toLowerCase(), outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/" + type.toLowerCase());

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException | WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}