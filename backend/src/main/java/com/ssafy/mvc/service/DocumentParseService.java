package com.ssafy.mvc.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DocumentParseService {

    public String parseFile(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null) return "";
        String lower = name.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return parsePdf(file);
        } else if (lower.endsWith(".docx")) {
            return parseDocx(file);
        } else if (lower.endsWith(".doc")) {
            return parseDoc(file);
        }
        return "";
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String parseDocx(MultipartFile file) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                sb.append(para.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private String parseDoc(MultipartFile file) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(file.getInputStream())) {
            return doc.getDocumentText();
        }
    }
}
