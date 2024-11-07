package tech.zerofiltre.blog.domain.course.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    private String path;
    private String courseTitle;
    private String ownerFullName;
    private byte[] content;
    private String uuid;   // Champ UUID pour le code de vérification unique
    private String hash;   // Champ pour stocker le hachage des données du certificat
}
