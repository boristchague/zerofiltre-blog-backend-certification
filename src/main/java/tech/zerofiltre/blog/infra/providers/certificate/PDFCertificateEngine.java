package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class PDFCertificateEngine {

    private final ITemplateEngine templateEngine;
    private final InfraProperties infraProperties;

    public PDFCertificateEngine(ITemplateEngine templateEngine, InfraProperties infraProperties) {
        this.templateEngine = templateEngine;
        this.infraProperties = infraProperties;
    }


    public byte[] process(Locale locale, String fullName, String courseTitle, String pdfFileName, String uuid) throws IOException, ZerofiltreException, WriterException, NoSuchAlgorithmException {
        Resource attachment = new ClassPathResource("mail_header_image.png");

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("attachement", attachment.getURL().toString());
        templateModel.put("fullName", fullName);
        templateModel.put("courseTitle", courseTitle);
        templateModel.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        generateQrCodeImageAsBase64(uuid, fullName, courseTitle);// 1. créer un thread sleep => valider créer normalement


        // 2. comment trouver pourquoi ça prend plus de temps ?
        Resource qrcodeResource = new ClassPathResource("qrcode.png");

        templateModel.put("path_to_qrcode_image",  qrcodeResource.getURL().toString());
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);

        String certificateHtml = templateEngine.process("certificate_course_completed.html", thymeleafContext);

        log.debug("certificateHtml = {}", certificateHtml);

        if (certificateHtml == null) throw new ZerofiltreException("Error during certificate generation.");

        return convertHtmlToPdf(certificateHtml, pdfFileName);
    }

    private byte[] convertHtmlToPdf(String certificateHtml, String pdfFileName) throws ZerofiltreException {
        org.jsoup.nodes.Document documentJsoup = Jsoup.parse(certificateHtml);
        documentJsoup.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(documentJsoup.html());
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new ZerofiltreException("Error during certificate generation.", e);
        }

        log.info("Course completion certificate for file: {} converted to pdf completed.", pdfFileName);

        return outputStream.toByteArray();
    }

    public String generateQrCodeImageAsBase64(String uuid, String fullName, String courseTitle) throws WriterException {

        BufferedImage qrCodeImage = generateQrCode(uuid, fullName, courseTitle);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrCodeImage, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] imageBytes = outputStream.toByteArray();

        // Chemin du fichier
        String filePath = "src/main/resources/qrcode.png";

        // Sauvegarde de l'image
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            fos.write(imageBytes);
            System.out.println("Image sauvegardée avec succès : " + filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de l'image : " + e.getMessage());
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(imageBytes).replaceAll("\\s", "");
    }


    public BufferedImage generateQrCode(String uuid, String fullName, String courseTitle ) throws WriterException {

        String contents = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv()) +"/certificate/verification?fullname="+fullName+"&courseTitle="+courseTitle+"&uuid="+uuid;
        BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

        int width = 285;
        int height = 285;

        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BitMatrix matrix = barcodeWriter.encode(contents, barcodeFormat, width, height);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }



}
