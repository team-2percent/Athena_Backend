package goorm.athena.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.certification}")
    private String firebaseConfigFilePath;

    @PostConstruct
    public void init(){
        try{
            ClassPathResource resource = new ClassPathResource(firebaseConfigFilePath);
            FileInputStream serviceAccount = new FileInputStream(resource.getFile());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e){
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }

}