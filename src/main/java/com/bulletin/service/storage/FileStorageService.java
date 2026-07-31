package com.bulletin.service.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service de stockage des fichiers (PDF de bulletins).
 *
 * - Si S3_BUCKET est configuré : stockage sur S3 (compatible AWS S3, Cloudflare R2, MinIO).
 *   Recommandé sur Railway où le filesystem est éphémère.
 * - Sinon : fallback sur le filesystem local (UPLOAD_DIR). Les fichiers sont perdus
 *   au redéploiement sur Railway, mais le PDF est régénéré à la demande.
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:/tmp/uploads}")
    private String uploadDir;

    @Value("${app.storage.s3.bucket:}")
    private String bucket;

    @Value("${app.storage.s3.region:us-east-1}")
    private String region;

    @Value("${app.storage.s3.endpoint:}")
    private String endpoint;

    @Value("${app.storage.s3.access-key:}")
    private String accessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String secretKey;

    private S3Client s3Client;
    private boolean s3Enabled;

    @PostConstruct
    public void init() {
        s3Enabled = bucket != null && !bucket.isBlank()
                && accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();

        if (!s3Enabled) {
            log.warn("S3 non configuré (S3_BUCKET/S3_ACCESS_KEY/S3_SECRET_KEY). "
                    + "Les PDF seront stockés localement dans '{}' et PERDUS au redéploiement sur Railway. "
                    + "Ils seront régénérés à la demande.", uploadDir);
            return;
        }

        try {
            var builder = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)));

            if (endpoint != null && !endpoint.isBlank()) {
                builder.endpointOverride(URI.create(endpoint))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build());
            }

            this.s3Client = builder.build();
            log.info("Stockage S3 activé : bucket='{}', region='{}'{}", bucket, region,
                    endpoint != null && !endpoint.isBlank() ? ", endpoint='" + endpoint + "'" : "");
        } catch (Exception e) {
            log.error("Impossible d'initialiser le client S3. Fallback sur le stockage local.", e);
            this.s3Enabled = false;
            this.s3Client = null;
        }
    }

    /** Clé de stockage d'un PDF de bulletin. */
    private String key(Long reportCardId) {
        return "bulletins/bulletin-" + reportCardId + ".pdf";
    }

    private Path localPath(Long reportCardId) {
        return Path.of(uploadDir, "bulletins", "bulletin-" + reportCardId + ".pdf");
    }

    /** Indique si le PDF du bulletin existe dans le stockage. */
    public boolean exists(Long reportCardId) {
        if (s3Enabled) {
            try {
                s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key(reportCardId))
                        .build());
                return true;
            } catch (NoSuchKeyException e) {
                return false;
            } catch (Exception e) {
                log.warn("Erreur headObject S3 pour le bulletin {}: {}", reportCardId, e.getMessage());
                return false;
            }
        }
        return Files.exists(localPath(reportCardId));
    }

    /** Sauvegarde le PDF et retourne une référence (clé S3 ou chemin relatif). */
    public String save(Long reportCardId, byte[] content) throws IOException {
        if (s3Enabled) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key(reportCardId))
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromBytes(content));
            log.info("PDF du bulletin {} stocké sur S3 (bucket={})", reportCardId, bucket);
            return "s3://" + bucket + "/" + key(reportCardId);
        }

        Path file = localPath(reportCardId);
        Files.createDirectories(file.getParent());
        Files.write(file, content);
        log.info("PDF du bulletin {} stocké localement: {}", reportCardId, file.toAbsolutePath());
        return "uploads/bulletins/bulletin-" + reportCardId + ".pdf";
    }

    /** Charge le contenu du PDF (régénéré à l'appel si absent). */
    public byte[] load(Long reportCardId) throws IOException {
        if (s3Enabled) {
            try {
                ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key(reportCardId))
                        .build());
                return bytes.asByteArray();
            } catch (NoSuchKeyException e) {
                throw new IOException("PDF introuvable sur S3 pour le bulletin " + reportCardId, e);
            }
        }
        return Files.readAllBytes(localPath(reportCardId));
    }
}
