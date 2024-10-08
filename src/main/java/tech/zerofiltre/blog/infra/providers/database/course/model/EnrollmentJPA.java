package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "enrollment", uniqueConstraints = {@UniqueConstraint(name = "UniqueUserPerCourseId", columnNames = {"user_id", "course_id"})})
@EqualsAndHashCode(callSuper = true)
public class EnrollmentJPA extends BaseEntityJPA {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id")
    private UserJPA user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "course_id")
    private CourseJPA course;
    private boolean completed;
    private boolean active;
    private LocalDateTime enrolledAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime lastModifiedAt;
    private boolean forLife;
    private String certificatePath;
    private String certificateHash;
    private String certificateUUID;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "enrollment", orphanRemoval = true)
    private Set<CompletedLessonJPA> completedLessons;


}
