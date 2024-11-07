package tech.zerofiltre.blog.domain.course.features.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.CertificateVerificationFailedException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateService {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificateProvider certificateProvider;

    public Certificate get(User user, long courseId) throws ZerofiltreException {
        if (enrollmentProvider.isCompleted(user.getId(), courseId)) {
            Certificate certificate = certificateProvider.generate(user, courseId);
            //generer uuid
            // generer le hash
            enrollmentProvider.setCertificatePath(certificate.getPath(), user.getId(), courseId);
            return certificate;
        }
        throw new ZerofiltreException("The certificate cannot be issued. The course has not yet been completed.");
    }

    public List<String> verify(String uuid, String fullname, String courseTitle) throws CertificateVerificationFailedException {

        Enrollment enrollment = enrollmentProvider.enrollmentOf(uuid);

        //hash
        //fullName
        //courseTitle

        //newHash = hash(fullName,courseTitle);

        //compare(hash,newHash) ==> ok ou ko;

        return new ArrayList<>();
    }


}
