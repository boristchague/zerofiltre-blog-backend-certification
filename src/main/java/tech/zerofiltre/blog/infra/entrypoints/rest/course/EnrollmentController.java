package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.features.enrollment.*;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.CertificateVerificationFailedException;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.CertificateVerificationResponseVM;
import tech.zerofiltre.blog.infra.providers.certificate.CertificateDigitalSignature;
import tech.zerofiltre.blog.infra.providers.database.CertificateRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    private final SecurityContextManager securityContextManager;
    private final Enroll enroll;
    private final Suspend suspend;
    private final CompleteLesson completeLesson;
    private final FindEnrollment findEnrollment;

    private final CertificateService certificateService;
    private final MessageSource messageSource;
    private final CertificateRepository certificateRepository;
    private final CertificateDigitalSignature certificateDigitalSignature;


    public EnrollmentController(
            EnrollmentProvider enrollmentProvider,
            CourseProvider courseProvider,
            UserProvider userProvider,
            SecurityContextManager securityContextManager,
            LessonProvider lessonProvider,
            ChapterProvider chapterProvider,
            SandboxProvider sandboxProvider,
            PurchaseProvider purchaseProvider,
            CertificateProvider certificateProvider, MessageSource messageSource, CertificateRepository certificateRepository, CertificateDigitalSignature certificateDigitalSignature) {
        this.securityContextManager = securityContextManager;
        this.messageSource = messageSource;
        this.certificateRepository = certificateRepository;
        this.certificateDigitalSignature = certificateDigitalSignature;
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider);
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);
        findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        certificateService = new CertificateService(enrollmentProvider, certificateProvider);
    }


    @PostMapping
    public Enrollment enroll(@RequestParam long courseId) throws ZerofiltreException {
        return enroll.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);

    }

    @DeleteMapping
    public void unEnroll(@RequestParam long courseId) throws ZerofiltreException {
        suspend.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);
    }

    @PatchMapping("/complete")
    public Enrollment completeLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), true);
    }

    @PatchMapping("/uncomplete")
    public Enrollment unCompleteLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), false);
    }


    @GetMapping
    public Enrollment getEnrollment(@RequestParam long courseId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User executor = securityContextManager.getAuthenticatedUser();
        return findEnrollment.of(courseId, userId, executor);
    }

    @GetMapping("/user")
    Page<Course> coursesOfEnrollment(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag) throws UserNotFoundException {
        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(securityContextManager.getAuthenticatedUser());
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        return findEnrollment.of(request);
    }

    @GetMapping("/certificate")
    public ResponseEntity<InputStreamResource> giveCertificateByCourseId(@RequestParam long courseId) throws ZerofiltreException, NoSuchAlgorithmException {
        User user = securityContextManager.getAuthenticatedUser();
        Certificate certificate = certificateService.get(user, courseId);

        byte[] content = certificate.getContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + certificate.getPath());

        // Générer un UUID unique pour le certificat
        certificateDigitalSignature.generateUuid(certificate);

        // Combiner les données du certificat pour générer le hachage
        certificateDigitalSignature.generateHash(certificate);

        // Sauvegarder le certificat dans la base de données
        certificateRepository.save(certificate);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(content.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(byteArrayInputStream));
    }

    @GetMapping("/certificate/verification")
    public CertificateVerificationResponseVM verifyCertificate(
            @RequestParam String uuid,
            @RequestParam String fullname,
            @RequestParam String courseTitle,
            HttpServletRequest request) {

        CertificateVerificationResponseVM response = new CertificateVerificationResponseVM();

        try {
            List<String> certificateData = certificateService.verify(uuid, fullname, courseTitle);
            response.setResponse(messageSource.getMessage("message.certificate.verification.response.valid", new Object[]{}, request.getLocale()));
            response.setDescription(messageSource.getMessage("message.certificate.verification.description.valid", new Object[]{}, request.getLocale()));
            response.setCourseTitle(certificateData.get(0));
            response.setOwnerFullName(certificateData.get(1));
            return response;

        } catch (CertificateVerificationFailedException e) {
            if (CertificateVerificationFailedException.INVALID.equals(e.getMessage())) {
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.invalid", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.invalid", new Object[]{}, request.getLocale()));
                return response;
            }
            response.setResponse(messageSource.getMessage("message.certificate.verification.response.error", new Object[]{}, request.getLocale()));
            response.setDescription(messageSource.getMessage("message.certificate.verification.description.error", new Object[]{}, request.getLocale()));
            return response;
        }

    }

    @GetMapping("/certificate/verification")
    public CertificateVerificationResponseVM verifyCertificate1(){

        /*
        Exposer l’endpoint du QRCODE permettant de vérifier le certificat

        stocker les données du certificat (fullname,courseTitle) en paramètre de l’url du QRCODE du certificat

        stocker le uuid en paramètre  de l’url du QRCODE du certificat
                /certificate/verification?fullname=philippe simo&courseTitle=DDD&uuid=xxxxxxxxx

        à l’appel de l’endpoint, récupérer les données du certificat, les hasher, récupérer l’uuid du QRcode,
        aller rechercher le hash en bd, comparer les deux hashs, si ok, renvoyer les infos du certificat avec OK,
        sinon renvoyer KO


         */
        return null;
    }

}
