package tech.zerofiltre.blog.domain.course.features.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.CertificateVerificationResponseVM;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateService {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificateProvider certificateProvider;
    private final MessageSource messageSource;


    public Certificate get(User user, long courseId) throws ZerofiltreException {
        if (enrollmentProvider.isCompleted(user.getId(), courseId)) {
            Certificate certificate = certificateProvider.generate(user, courseId);
            enrollmentProvider.setCertificatePath(certificate.getPath(), user.getId(), courseId);
            certificateProvider.save(certificate);
            return certificate;
        }
        throw new ZerofiltreException("The certificate cannot be issued. The course of id " + courseId + " has not yet been completed.");
    }

    public CertificateVerificationResponseVM verify(String uuid, String fullname, String courseTitle, HttpServletRequest request) throws ZerofiltreException {

        CertificateVerificationResponseVM response = new CertificateVerificationResponseVM();
        try {
            Optional<Certificate> dbCertificate = certificateProvider.findByUuid(uuid);
            if (dbCertificate.isEmpty()) {
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.notfound", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.notfound", new Object[]{}, request.getLocale()));
                return response;
            }
            String collectedHash = ZerofiltreUtils.generateHash(fullname, courseTitle);
            String dbHash = dbCertificate.get().getHash();

            if (collectedHash.equals(dbHash)) {
                response.setOwnerFullName(dbCertificate.get().getOwnerFullName());
                response.setCourseTitle(dbCertificate.get().getCourseTitle());
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.valid", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.valid", new Object[]{}, request.getLocale()));
                return response;
            }
            response.setResponse(messageSource.getMessage("message.certificate.verification.response.invalid", new Object[]{}, request.getLocale()));
            response.setDescription(messageSource.getMessage("message.certificate.verification.description.invalid", new Object[]{}, request.getLocale()));
            return response;

        } catch (NoSuchAlgorithmException e) {
            log.error("Hash generation failure !", e);
            throw new ZerofiltreException("Hash generation failure !", e);
        } catch (Exception e) {
            log.error("An error occurred when validating the certificate with uuid: {}", uuid, e);
            response.setResponse(messageSource.getMessage("message.certificate.verification.response.error", new Object[]{}, request.getLocale()));
            response.setDescription(messageSource.getMessage("message.certificate.verification.description.error", new Object[]{}, request.getLocale()));
            return response;
        }
    }


}
