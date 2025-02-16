package tech.zerofiltre.blog.infra.providers.certificate;


import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.CertificateJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.CertificateJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.CertificateJPA;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PDFCertificateProvider implements CertificateProvider {

    private final StorageProvider storageProvider;
    private final CourseProvider courseProvider;
    private final PDFCertificateEngine pdfCertificateEngine;
    private final CertificateJPARepository certificateJPARepository;
    private final CertificateJPAMapper certificateMapper = Mappers.getMapper(CertificateJPAMapper.class);


    @Override
    public Certificate generate(User user, long courseId) throws ZerofiltreException {

        String fullName = user.getFullName()  + UUID.randomUUID();
        try {
            Certificate certificate = new Certificate();
            String courseTitle = courseProvider.getTitle(courseId);

            String fileName = "certificates/" + fullName.replaceAll("\\s+", "") + "_" + courseTitle + ".pdf";
            certificate.setPath(fileName);
            certificate.setCourseTitle(courseTitle);
            certificate.setOwnerFullName(fullName);

            Optional<byte[]> storedCertificate = storageProvider.get(fileName);
        if (storedCertificate.isPresent()) {
            certificate.setContent(storedCertificate.get());
            return certificate;
        }

            String language = user.getLanguage() != null ? user.getLanguage() : Locale.FRANCE.getLanguage();
            String uuid = UUID.randomUUID().toString();
            byte[] content = pdfCertificateEngine.process(new Locale(language), fullName, courseTitle, fileName, uuid);
            certificate.setContent(content);
            storageProvider.store(content, fileName);
            certificate.setUuid(uuid);
            certificate.setHash(ZerofiltreUtils.generateHash(fullName, courseTitle));
            return certificate;
        } catch (IOException | WriterException | NoSuchAlgorithmException e) {
            throw new ZerofiltreException("Error creating certificate for " + fullName, e);
        }

    }

    @Override
    public Certificate save(Certificate certificate) throws NoSuchAlgorithmException {
        CertificateJPA certificateJPA = certificateMapper.toJPA(certificate);
        return  certificateMapper.fromJPA(certificateJPARepository.save(certificateJPA));
    }

    @Override
    public void delete(Certificate certificate) {
        CertificateJPA certificateJPA = certificateMapper.toJPA(certificate);
        certificateJPARepository.delete(certificateJPA);
    }

    @Override
    public Certificate findByUuid(String uuid) throws ZerofiltreException {
        Optional<CertificateJPA> certificateJPA = certificateJPARepository.findByUuid(uuid);
        return certificateMapper.fromJPA(certificateJPA.get());
    }

    @Override
    public Certificate findByOwnerFullNameAndCourseTitle(String ownerFullName, String courseTitle) {
        Optional<CertificateJPA> certificateJPA = certificateJPARepository.findByOwnerFullNameAndCourseTitle(ownerFullName, courseTitle);
        return certificateMapper.fromJPA(certificateJPA.get());
    }


}
