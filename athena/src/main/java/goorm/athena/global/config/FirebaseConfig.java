package goorm.athena.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {
    @Value("${firebase.projectId}")
    private String firebaseProjectId;
    @Value("${firebase.privateKeyId}")
    private String firebasePrivateKeyId;
    @Value("${firebase.privateKey}")
    private String firebasePrivateKey;
    @Value("${firebase.clientEmail}")
    private String firebaseClientEmail;
    @Value("${firebase.clientId}")
    private String firebaseClientId;
    @Value("${firebase.clientX509CertUrl}")
    private String firebaseClientX509CertUrl;

    @PostConstruct
    public void init() {
        try {
            String json = String.format(
                    "{\n" +
                            "  \"type\": \"service_account\",\n" +
                            "  \"project_id\": \"%s\",\n" +
                            "  \"private_key_id\": \"%s\",\n" +
                            "  \"private_key\": \"%s\",\n" +
                            "  \"client_email\": \"%s\",\n" +
                            "  \"client_id\": \"%s\",\n" +
                            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                            "  \"client_x509_cert_url\": \"%s\",\n" +
                            "  \"universe_domain\": \"googleapis.com\"\n" +
                            "}",
                    firebaseProjectId, firebasePrivateKeyId, firebasePrivateKey, firebaseClientEmail, firebaseClientId,
                    firebaseClientX509CertUrl);

            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }

}