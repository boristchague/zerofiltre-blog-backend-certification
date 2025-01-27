package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.k8sprovisioner.K8sSandboxProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
import tech.zerofiltre.blog.infra.providers.certificate.PDFCertificateEngine;
import tech.zerofiltre.blog.infra.providers.certificate.PDFCertificateProvider;
import tech.zerofiltre.blog.infra.providers.database.course.CertificateJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.purchase.DBPurchaseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBVerificationTokenProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.infra.security.config.DBUserDetailsService;
import tech.zerofiltre.blog.infra.security.config.LoginFirstAuthenticationEntryPoint;
import tech.zerofiltre.blog.infra.security.config.RoleRequiredAccessDeniedHandler;
import tech.zerofiltre.blog.infra.security.model.GithubAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.StackOverflowAuthenticationTokenProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EnrollmentController.class)
@Import({Jackson2ObjectMapperBuilder.class, K8sSandboxProvider.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
        APIClientConfiguration.class, DBPurchaseProvider.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class, DBChapterProvider.class, PDFCertificateProvider.class, PDFCertificateEngine.class})
class EnrollmentControllerIT {
    @MockBean
    SecurityContextManager securityContextManager;
    @MockBean
    ChapterProvider chapterProvider;
    @MockBean
    CourseProvider courseProvider;
    @MockBean
    UserProvider userProvider;
    @MockBean
    LessonProvider lessonProvider;
    @MockBean
    LoggerProvider loggerProvider;
    @MockBean
    EnrollmentProvider enrollmentProvider;
    @MockBean
    UserJPARepository userJPARepository;
    @MockBean
    StorageProvider storageProvider;
    @MockBean
    StackOverflowLoginProvider stackOverflowLoginProvider;
    @MockBean
    GithubLoginProvider githubLoginProvider;
    @MockBean
    DBVerificationTokenProvider verificationTokenProvider;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    CertificateJPARepository certificateJPARepository;

    @MockBean
    K8sSandboxProvider sandboxProvider;

    @MockBean
    DBPurchaseProvider dbPurchaseProvider;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;
    User author = ZerofiltreUtils.createMockUser(true);
    Course mockCourse = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    MetricsProvider metricsProvider;

    @BeforeEach
    void setUp() throws ZerofiltreException {
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(author));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));
        when(securityContextManager.getAuthenticatedUser()).thenReturn((author));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().courseId(mockCourse.getId()).build()));
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(Lesson.builder().build()));
        Enrollment anEnrollment = new Enrollment();
        anEnrollment.setUser(new User());
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(anEnrollment));
        doNothing().when(sandboxProvider).destroy(anyString(), anyString());
        when(enrollmentProvider.save(any())).thenAnswer(i -> {
            Enrollment enrollment = (Enrollment) i.getArguments()[0];
            enrollment.setCourse(mockCourse);
            return enrollment;
        });
        doNothing().when(loggerProvider).log(any());
    }


    @Test
    @WithMockUser
    void onEnroll_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/enrollment?courseId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onCompleted_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.patch("/enrollment/complete?courseId=45&lessonId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onUnCompleted_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.patch("/enrollment/uncomplete?courseId=45&lessonId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onUnenroll_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.delete("/enrollment?courseId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }
}
