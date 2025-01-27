package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "certificate")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CertificateJPA extends BaseEntityJPA {

    private String path;
    private String courseTitle;
    private String ownerFullName;

    @Column(unique = true)
    private String uuid;
    @Column(unique = true)
    private String hash;
}
