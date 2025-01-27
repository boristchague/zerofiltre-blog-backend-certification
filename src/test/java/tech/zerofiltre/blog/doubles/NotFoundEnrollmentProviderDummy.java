package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;

import java.util.Optional;

public class NotFoundEnrollmentProviderDummy implements EnrollmentProvider {

    public boolean saveCalled;

    @Override
    public Enrollment save(Enrollment enrollment) {
        saveCalled = true;
        enrollment.setId(1);
        return enrollment;
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

    @Override
    public void delete(long userId, long courseId) {

    }

    @Override
    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        return null;
    }


    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        return Optional.empty();
    }
}
