package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.providers.database.SpringPageMapper;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.EnrollmentJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.EnrollmentJPA;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class DBEnrollmentProvider implements EnrollmentProvider {

    private final EnrollmentJPARepository repository;
    private final EnrollmentJPAMapper mapper = Mappers.getMapper(EnrollmentJPAMapper.class);
    private final SpringPageMapper<Enrollment> pageMapper = new SpringPageMapper<>();

    @Override
    public void delete(long userId, long courseId) {
        repository.deleteByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        org.springframework.data.domain.Page<EnrollmentJPA> page
                = repository.findByUserIdAndActiveAndCompleted(
                PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "enrolledAt"),
                userId,
                !FinderRequest.Filter.INACTIVE.equals(filter),
                FinderRequest.Filter.COMPLETED.equals(filter));
        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));

    }

    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        return repository.findByUserIdAndCourseIdAndActive(userId, courseId, isActive)
                .map(mapper::fromJPA);
    }

    @Override
    public Enrollment save(Enrollment enrollment) throws ZerofiltreException {
        try {
            EnrollmentJPA enrollmentJPA = mapper.toJPA(enrollment);
            enrollmentJPA = repository.save(enrollmentJPA);
            return mapper.fromJPA(enrollmentJPA);
        } catch (DataIntegrityViolationException e) {
            throw new ZerofiltreException("You are already enrolled", e);
        }
    }

    @Override
    public boolean isCompleted(long userId, long courseId) {
        return repository.getCompletedByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public void setCertificatePath(String certificatePath, long userId, long courseId) {
        repository.updateCertificatePathByUserIdAndCourseId(certificatePath, userId, courseId);
    }

    @Override
    public Enrollment enrollmentOf(String uuid) {
        return null;
    }
}
