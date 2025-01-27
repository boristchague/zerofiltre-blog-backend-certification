package tech.zerofiltre.blog.domain.course.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    private String path;
    private String courseTitle;
    private String ownerFullName;
    private byte[] content;
    private String uuid;
    private String hash;
}
