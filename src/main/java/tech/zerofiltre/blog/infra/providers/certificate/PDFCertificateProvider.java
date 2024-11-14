package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PDFCertificateProvider implements CertificateProvider {

    private final StorageProvider storageProvider;
    private final CourseProvider courseProvider;
    private final PDFCertificateEngine pdfCertificateEngine;

    @Override
    public Certificate generate(User user, long courseId) throws ZerofiltreException {

        Certificate certificate = new Certificate();
        String fullName = user.getFullName();
        String courseTitle = courseProvider.getTitle(courseId);
        String fileName = "certificates/" + ZerofiltreUtils.sanitizeString(fullName) + "_" + ZerofiltreUtils.sanitizeString(courseTitle) + ".pdf";
        certificate.setPath(fileName);

        Optional<byte[]> storedCertificate = storageProvider.get(fileName);
        if (storedCertificate.isPresent()) {
            certificate.setContent(storedCertificate.get());
            return certificate;
        }

        try {
            String language = user.getLanguage() != null ? user.getLanguage() : Locale.FRANCE.getLanguage();
            byte[] content = pdfCertificateEngine.process(new Locale(language), fullName, courseTitle, fileName);
            storageProvider.store(content, fileName);
            certificate.setContent(content);
            return certificate;
        } catch (IOException | WriterException e) {
            throw new ZerofiltreException("Error creating certificate for " + fullName, e);
        }

    }


}
