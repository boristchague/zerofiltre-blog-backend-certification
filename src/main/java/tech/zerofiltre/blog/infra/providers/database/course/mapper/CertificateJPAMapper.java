package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.infra.providers.database.course.model.CertificateJPA;

@Mapper
public interface CertificateJPAMapper {

    CertificateJPA toJPA(Certificate certificate);

    Certificate fromJPA(CertificateJPA certificateJPA);

}
