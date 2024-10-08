package tech.zerofiltre.blog.domain.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    private long id;
    private User user;
    private Course course;
    private boolean completed;
    private boolean active = true;
    private LocalDateTime enrolledAt = LocalDateTime.now();
    private LocalDateTime lastModifiedAt = LocalDateTime.now();
    private LocalDateTime suspendedAt;
    private boolean forLife = false;
    private String certificatePath;
    private String certificateHash;
    private String certificateUUID;
    private List<CompletedLesson> completedLessons = new ArrayList<>();


}
