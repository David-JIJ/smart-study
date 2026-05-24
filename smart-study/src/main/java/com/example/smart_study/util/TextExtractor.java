package com.example.smart_study.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TextExtractor {

    public static String extractText(String filePath) throws Exception {
        String fileName = filePath.toLowerCase();

        // 1. 处理 PDF (.pdf)
        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(new File(filePath))) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }

        // 2. 处理老版 Word (.doc)
        else if (fileName.endsWith(".doc")) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 HWPFDocument doc = new HWPFDocument(fis);
                 WordExtractor extractor = new WordExtractor(doc)) {
                return extractor.getText();
            }
        }

        // 3. 处理新版 Word (.docx)
        else if (fileName.endsWith(".docx")) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 XWPFDocument document = new XWPFDocument(fis)) {
                StringBuilder sb = new StringBuilder();
                List<XWPFParagraph> paragraphs = document.getParagraphs();
                for (XWPFParagraph para : paragraphs) {
                    sb.append(para.getText()).append("\n");
                }
                return sb.toString();
            }
        }

        // 4. 处理 文本文件 (.txt)
        else if (fileName.endsWith(".txt")) {
            // 尝试用 UTF-8 读取
            return new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        }

        return ""; // 不支持的格式返回空
    }
}
