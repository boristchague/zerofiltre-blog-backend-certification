package tech.zerofiltre.blog.domain.course.features.enrollment;

import com.google.zxing.WriterException;
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
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.validation.constraints.AssertTrue;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateService {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificateProvider certificateProvider;


    public Certificate get(User user, long courseId) throws ZerofiltreException, NoSuchAlgorithmException, WriterException {
        if (enrollmentProvider.isCompleted(user.getId(), courseId)) {
            Certificate certificate = certificateProvider.generate(user, courseId);
            enrollmentProvider.setCertificatePath(certificate.getPath(), user.getId(), courseId);
            certificateProvider.save(certificate);
            return certificate;
        }
        throw new ZerofiltreException("The certificate cannot be issued. The course has not yet been completed.");
    }

    public List<String> verify(String uuid, String fullname, String courseTitle) throws ZerofiltreException, NoSuchAlgorithmException {


        //hash - fullName - courseTitle //mettre genererHash dans un utils pour
        String collectedHash = String.valueOf(ZerofiltreUtils.generateHash(fullname, courseTitle));
        Certificate collectedCertificate = certificateProvider.findByUuid(uuid);

        Certificate dbCertificate = certificateProvider.findByOwnerFullNameAndCourseTitle(fullname, courseTitle);
        String dbHash = dbCertificate.getHash();

        //compare(hash,newHash) ==> ok ou ko;
        List<String> array = new ArrayList<>();

        if(dbHash.equals(collectedHash) == true){
            array.add(fullname + "OK");
            array.add(courseTitle + "OK");
        }else {
            array.add(fullname + "KO");
            array.add(courseTitle + "KO");
        }
        return array;
    }





}
