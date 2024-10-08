package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CertificateVerificationResponseVM {
    private String response;
    private String description;
    private String courseTitle;
    private String ownerFullName;
}
