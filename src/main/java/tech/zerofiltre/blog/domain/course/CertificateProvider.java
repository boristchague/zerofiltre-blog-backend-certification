package tech.zerofiltre.blog.domain.course;

import com.google.zxing.WriterException;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.model.CertificateJPA;

import java.awt.image.BufferedImage;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;


public interface CertificateProvider {

    Certificate save(Certificate certificate) throws NoSuchAlgorithmException;

    void delete(Certificate certificate);

    Certificate generate(User user, long courseId) throws ZerofiltreException;

    Certificate findByUuid(String uuid) throws ZerofiltreException;

    Certificate findByOwnerFullNameAndCourseTitle(String ownerFullName, String courseTitle);

}
