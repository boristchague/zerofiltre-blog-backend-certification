package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.zerofiltre.blog.domain.course.features.enrollment.CertificateService;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.providers.database.CertificateRepository;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class PDFCertificateEngine {

    private final ITemplateEngine templateEngine;
    private final CertificateDigitalSignature certificateDigitalSignature;
    private final CertificateService certificateService;
    private final SecurityContextManager securityContextManager;
    private final CourseJPARepository courseJPARepository;

    public PDFCertificateEngine(ITemplateEngine templateEngine, CertificateDigitalSignature certificateDigitalSignature, CertificateService certificateService, SecurityContextManager securityContextManager, CourseJPARepository courseJPARepository) {
        this.templateEngine = templateEngine;
        this.certificateDigitalSignature = certificateDigitalSignature;
        this.certificateService = certificateService;
        this.securityContextManager = securityContextManager;
        this.courseJPARepository = courseJPARepository;
    }


    public byte[] process(Locale locale, String fullName, String courseTitle, String pdfFileName) throws IOException, ZerofiltreException, WriterException {
        Resource attachment = new ClassPathResource("mail_header_image.png");

        User user = securityContextManager.getAuthenticatedUser();
        long courseId = courseJPARepository.getCourseByCourseTitle(courseTitle);
        Certificate certificate = certificateService.get(user, courseId);

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("attachement", attachment.getURL().toString());
        templateModel.put("fullName", fullName);
        templateModel.put("courseTitle", courseTitle);
        templateModel.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        templateModel.put("path_to_qrcode_image", certificateDigitalSignature.generateQrCode(certificate));
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);

        String certificateHtml = templateEngine.process("certificate_course_completed.html", thymeleafContext);

        log.trace("certificateHtml = {}", certificateHtml);

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
}
