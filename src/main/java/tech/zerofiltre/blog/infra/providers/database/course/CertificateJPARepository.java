package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.providers.database.course.model.CertificateJPA;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CertificateJPARepository extends JpaRepository<CertificateJPA, Long> {

    //TODO UUID to string and not in UUID monter en compétence sur requêtes inférées et struct pour les mapper.
    Optional<CertificateJPA> findByUuid(String uuid) throws ZerofiltreException;

    Optional<CertificateJPA> findByOwnerFullNameAndCourseTitle(String ownerFullName, String courseTitle);

}
