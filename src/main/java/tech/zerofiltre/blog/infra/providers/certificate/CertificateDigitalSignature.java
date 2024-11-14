package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.infra.providers.database.CertificateRepository;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import com.google.zxing.BarcodeFormat;

public class CertificateDigitalSignature {


    public CertificateRepository certificateRepository;

    public CertificateDigitalSignature(CertificateRepository certificateRepository){
        this.certificateRepository = certificateRepository;
    }

    public String generateHash(String data) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        // Convertit le hachage en une chaîne hexadécimale
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Méthode pour créer et sauvegarder un certificat
    public void saveCertificate(Certificate certificate) throws NoSuchAlgorithmException {
        // Sauvegarder le certificat dans la base de données
        certificateRepository.save(certificate);
    }

    public void generateHash(Certificate certificate) throws NoSuchAlgorithmException {
        // Combiner les données du certificat pour générer le hachage
        String dataToHash = certificate.getOwnerFullName() + ":" + certificate.getCourseTitle();
        String hash = generateHash(dataToHash);
        certificate.setHash(hash);
    }

    public void generateUuid(Certificate certificate) {
        // Générer un UUID unique pour le certificat
        UUID uuid = UUID.randomUUID();
        certificate.setUuid(uuid);
    }

    public BufferedImage generateQrCode(Certificate certificate) throws WriterException {

        UUID uuid = certificate.getUuid();
        String fullName = certificate.getOwnerFullName();
        String courseTitle = certificate.getCourseTitle();

        String contents = "/certificate/verification?fullname="+fullName+"&courseTitle="+courseTitle+"&uuid="+uuid;
        BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

        int width = 300;
        int height = 300;

        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BitMatrix matrix = barcodeWriter.encode(contents, barcodeFormat, width, height);
        BufferedImage qrCodeImg = MatrixToImageWriter.toBufferedImage(matrix);

        return qrCodeImg;
    }


    public static String generateCertificateUUID(Certificate certificate) {

        String uniqueData = certificate.getCourseTitle() + ":" + certificate.getOwnerFullName();

        UUID certificateUUID = UUID.nameUUIDFromBytes(uniqueData.getBytes());

        return certificateUUID.toString();
    }




}