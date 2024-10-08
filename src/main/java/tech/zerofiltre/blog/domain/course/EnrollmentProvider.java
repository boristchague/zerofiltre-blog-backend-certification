package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.util.Optional;

public interface EnrollmentProvider {

    void delete(long userId, long courseId);

    Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag);

    Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive);

    Enrollment save(Enrollment enrollment) throws ZerofiltreException;

    boolean isCompleted(long userId, long courseId);

    void setCertificatePath(String path, long id, long courseId);

    Enrollment enrollmentOf(String uuid);
}
