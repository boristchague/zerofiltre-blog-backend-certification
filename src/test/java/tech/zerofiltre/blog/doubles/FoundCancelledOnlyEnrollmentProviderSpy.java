package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class FoundCancelledOnlyEnrollmentProviderSpy implements EnrollmentProvider {

    public boolean saveCalled = false;
    public boolean ofCalled = false;
    public boolean enrollmentOfCalled = false;
    public FinderRequest.Filter ofFilter = null;

    @Override
    public Enrollment save(Enrollment enrollment) {
        saveCalled = true;
        enrollment.setId(1);
        enrollment.setCourse(ZerofiltreUtils.createMockCourse(false, Status.DRAFT, ZerofiltreUtils.createMockUser(false),
                Collections.emptyList(), Collections.emptyList()));
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
        ofFilter = filter;
        User mockUser = ZerofiltreUtils.createMockUser(false);
        Course mockCourse2 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, mockUser,
                Collections.emptyList(), Collections.emptyList());
        Course mockCourse1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, mockUser,
                Collections.emptyList(), Collections.emptyList());

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1);
        enrollment.setCourse(mockCourse1);
        enrollment.setUser(mockUser);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setId(2);
        enrollment2.setCourse(mockCourse2);
        enrollment2.setUser(mockUser);
        Page<Enrollment> result = new Page<>();

        result.setContent(Arrays.asList(enrollment, enrollment2));
        result.setTotalNumberOfElements(10);
        result.setNumberOfElements(2);
        result.setTotalNumberOfPages(4);
        result.setPageNumber(1);
        result.setPageSize(2);
        result.setHasNext(true);
        result.setHasPrevious(true);

        ofCalled = true;
        return result;
    }


    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        if(isActive) {
            enrollmentOfCalled = true;
            return Optional.empty();
        }
        Enrollment value = new Enrollment();
        value.setEnrolledAt(LocalDateTime.now().minusDays(2));
        value.setLastModifiedAt(value.getEnrolledAt());
        value.setSuspendedAt(LocalDateTime.now().minusDays(1));
        value.setActive(false);
        value.setUser(ZerofiltreUtils.createMockUser(false));
        value.setCourse(ZerofiltreUtils.createMockCourse(false, Status.DRAFT, ZerofiltreUtils.createMockUser(false),
                Collections.emptyList(), Collections.emptyList()));
        enrollmentOfCalled = true;
        return Optional.of(value);
    }
}
