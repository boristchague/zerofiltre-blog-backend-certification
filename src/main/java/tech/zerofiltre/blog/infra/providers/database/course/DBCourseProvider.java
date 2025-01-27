package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.SpringPageMapper;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.CourseJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class DBCourseProvider implements CourseProvider {
// Q1 : pourquoi dbcoursePro implémente provider de la couche domaine.
    private final CourseJPARepository repository;
    private final UserJPARepository userRepository;
    private final CourseJPAMapper mapper = Mappers.getMapper(CourseJPAMapper.class);
    private final SpringPageMapper<Course> pageMapper = new SpringPageMapper<>();


    @Override
    public Optional<Course> courseOfId(long id) {
        return repository.findById(id)
                .map(mapper::fromJPA)
                .map(course -> {
                    course.setEnrolledCount(getEnrolledCount(course.getId()));
                    course.setLessonsCount(getLessonsCount(course.getId()));
                    return course;
                });
    }

    @Override
    public Course save(Course course) {
        course = mapper.fromJPA(repository.save(mapper.toJPA(course)));
        course.setEnrolledCount(getEnrolledCount(course.getId()));
        course.setLessonsCount(getLessonsCount(course.getId()));
        return course;
    }

    @Override
    public void delete(Course existingCourse) {
        repository.delete(mapper.toJPA(existingCourse));
    }

    @Override
    @Cacheable("courses-list")
    public Page<Course> courseOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag) {
        org.springframework.data.domain.Page<CourseJPA> page;

        final var publishedAtPropertyName = "publishedAt";
        if (authorId == 0) {
            if (tag != null)
                page = repository.findByStatusAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsDesc(PageRequest.of(pageNumber, pageSize), status);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByEnrolledDesc(PageRequest.of(pageNumber, pageSize), status);
            else
                page = repository.findByStatus(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status);
        } else {
            if (tag != null)
                page = repository.findByStatusAndAuthorIdAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByEnrolledAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else
                page = repository.findByStatusAndAuthorId(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId);
        }
        return pageMapper.fromSpringPage(page.map(courseJPA -> {
            User author = new User();
            Course course = mapper.fromJPALight(courseJPA);
            String info = userRepository.findAuthorInfoByCourseId(courseJPA.getId());
            String[] splitInfo = info.split(",");
            author.setId(Long.parseLong(splitInfo[0]));
            author.setFullName(splitInfo[1]);
            author.setProfilePicture(splitInfo[2]);
            course.setAuthor(author);
            course.setLessonsCount(getLessonsCount(courseJPA.getId()));
            course.setEnrolledCount(getEnrolledCount(courseJPA.getId()));
            return course;
        }));
    }


    @Override
    public List<Course> courseOf(User user) {
        return repository.findByAuthorId(user.getId())
                .stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public int getEnrolledCount(long courseId) {
        return repository.getEnrolledCount(courseId);
    }

    @Override
    public int getLessonsCount(long courseId) {
        return repository.getLessonsCount(courseId);
    }

    @Override
    public long courseIdOfChapterId(long chapterId) {
        Optional<CourseJPA> course = repository.findByChapterId(chapterId);
        return course.map(CourseJPA::getId).orElse(0L);
    }

    @Override
    public String getTitle(long courseId) {
        return repository.getTitle(courseId);
    }
}
