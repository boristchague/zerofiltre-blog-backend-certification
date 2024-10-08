package tech.zerofiltre.blog.domain.error;

public class CertificateVerificationFailedException extends ZerofiltreException {

    public static final String INVALID = "Certificate is not valid";
    public static final String ERROR = "Certificate not found";

    public CertificateVerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
