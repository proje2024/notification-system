package com.example.notification_system.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OcrService {

    private final Tesseract tesseract;
    private static final int TEXT_LENGTH_LIMIT = 1000;

    @Autowired
    public OcrService(Tesseract tesseract) {
        this.tesseract = tesseract;
        this.tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata/");
        this.tesseract.setLanguage("tur");
    }

    public Map<String, Object> extractDataFromUploadedFile(MultipartFile file) {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);

            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                return extractDataFromPdf(convFile);
            } else {
                BufferedImage img = ImageIO.read(convFile);
                return extractDataFromImage(img);
            }

        } catch (IOException | IllegalStateException e) {
            return null;
        }
    }

    private Map<String, Object> extractDataFromPdf(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) { 
            Map<String, Object> extractedData = new HashMap<>();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
    
            StringBuilder ocrResult = new StringBuilder();
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String result = tesseract.doOCR(bufferedImage);
                ocrResult.append(result).append("\n");
    
                if (ocrResult.length() > TEXT_LENGTH_LIMIT) {
                    extractedData = parseOcrResult(ocrResult.substring(0, TEXT_LENGTH_LIMIT));
                    if (extractedData.isEmpty()) {
                        return null; 
                    }
                } else {
                    extractedData = parseOcrResult(ocrResult.toString());
                }
            }
    
            return extractedData.isEmpty() ? null : extractedData;
    
        } catch (java.io.IOException | net.sourceforge.tess4j.TesseractException e) {
            return null;
        }
    }
    

    private Map<String, Object> extractDataFromImage(BufferedImage img) {
        try {
            String result = tesseract.doOCR(img);

            if (result.length() > TEXT_LENGTH_LIMIT) {
                Map<String, Object> extractedData = parseOcrResult(result.substring(0, TEXT_LENGTH_LIMIT));
                return extractedData.isEmpty() ? null : extractedData;
            } else {
                return parseOcrResult(result);
            }
      } catch (TesseractException e) {
        return null;
    }
    }

    private Map<String, Object> parseOcrResult(String result) {
        Map<String, Object> extractedData = new HashMap<>();

        String[] lines = result.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("Etkinlik İsmi:")) {
                extractedData.put("eventName", line.substring(14).trim());
            }
            if (line.startsWith("Etkinlik Tarihi:")) {
                extractedData.put("eventTime", line.substring(17).trim());
            }
            if (line.startsWith("Bildirim Sıklığı:")) {
                extractedData.put("notificationFrequency", line.substring(17).trim());
            }
            if (line.startsWith("Bildirim Aralığı:")) {
                extractedData.put("notificationInterval", line.substring(17).trim());
            }
            if (line.startsWith("Bildirim Başlangıç Tarihi:")) {
                extractedData.put("notificationStartTime", line.substring(28).trim());
            }
        }
        return extractedData;
    }
}
