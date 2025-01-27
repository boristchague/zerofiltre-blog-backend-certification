package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.features.enrollment.CertificateService;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PDFCertificateEngineTest {

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private InfraProperties infraProperties;


    private PDFCertificateEngine certificateEngine;

    @BeforeEach
    void init() {
        certificateEngine = new PDFCertificateEngine(templateEngine, infraProperties);
    }

    @Test
    void mustCall_TemplateEngine() throws IOException, ZerofiltreException, WriterException, NoSuchAlgorithmException {
        //Arrange
        when(templateEngine.process(anyString(), any())).thenReturn("content");
        when(infraProperties.getEnv()).thenReturn("dev");


        //Act
        certificateEngine.process(Locale.FRANCE, "name", "title", "fileName", "");


        //Assert
        verify(templateEngine, times(1)).process(anyString(), any());
    }

}