package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.infra.providers.database.course.CertificateJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PDFCertificateProviderTest {


    @Mock
    private StorageProvider storageProvider;

    @Mock
    private CourseProvider courseProvider;

    @Mock
    private PDFCertificateEngine pdfCertificateEngine;

    @Mock
    private CertificateJPARepository certificateJPARepository;


    private PDFCertificateProvider certificateProvider;


    @BeforeEach
    void init() {
        certificateProvider = new PDFCertificateProvider(storageProvider, courseProvider, pdfCertificateEngine, certificateJPARepository);
    }

    @Test
    void mustNot_processCertificate_whenStoredAlready() throws ZerofiltreException, IOException, WriterException, NoSuchAlgorithmException {

        //given
        when(storageProvider.get(any())).thenReturn(Optional.of(new byte[]{1, 2}));
        when(courseProvider.getTitle(anyLong())).thenReturn("title");

        //when
        certificateProvider.generate(ZerofiltreUtils.createMockUser(false), 3);

        //then
        verify(pdfCertificateEngine, times(0)).process(any(), anyString(), anyString(), anyString(), anyString());

        verify(storageProvider, times(0)).store(any(), anyString());

    }

    @Test
    void must_processCertificate_whenNotStoredAlready() throws ZerofiltreException, IOException, WriterException, NoSuchAlgorithmException {

        //given
        when(storageProvider.get(any())).thenReturn(Optional.empty());
        when(courseProvider.getTitle(anyLong())).thenReturn("title");


        //when
        certificateProvider.generate(ZerofiltreUtils.createMockUser(false), 3);


        //then
        verify(pdfCertificateEngine, times(1)).process(any(), anyString(), anyString(), anyString(), anyString());
        verify(storageProvider, times(1)).store(any(), anyString());

    }

}