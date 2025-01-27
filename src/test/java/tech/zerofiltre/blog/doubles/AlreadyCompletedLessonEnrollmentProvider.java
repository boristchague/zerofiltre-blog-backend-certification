package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.CompletedLesson;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Lesson;

import java.util.Optional;

public class AlreadyCompletedLessonEnrollmentProvider implements EnrollmentProvider {


    public void delete(long userId, long courseId) {

    }

    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        return null;
    }

    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setId(224);
        enrollment1.setCourse(new Course());
        Lesson lesson = new Lesson.LessonBuilder().id(3).build();
        CompletedLesson completedLesson = new CompletedLesson();
        completedLesson.setLessonId(lesson.getId());
        enrollment1.getCompletedLessons().add(completedLesson);
        return Optional.of(enrollment1);
    }

    public Enrollment save(Enrollment enrollment) {
        return null;
    }

    @Override
    public boolean isCompleted(long userId, long courseId) { return false; }

    @Override
    public void setCertificatePath(String path, long id, long courseId) {

    }

    @Override
    public Enrollment enrollmentOf(String uuid) {
        return null;
    }
}
