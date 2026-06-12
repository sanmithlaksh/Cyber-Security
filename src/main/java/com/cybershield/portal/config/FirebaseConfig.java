package com.cybershield.portal.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private boolean active = false;
    private com.google.auth.oauth2.GoogleCredentials credentials;
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
            if (serviceAccount == null) {
                System.err.println("=============================================================");
                System.err.println("WARNING: serviceAccountKey.json NOT FOUND in src/main/resources/");
                System.err.println("Running CyberShield in LOCAL FALLBACK MOCK MODE.");
                System.err.println("Create a Firebase project, download credentials, and save them as");
                System.err.println("serviceAccountKey.json to link your live Firestore instance.");
                System.err.println("=============================================================");
                return;
            }

            com.google.auth.oauth2.GoogleCredentials creds = com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount);
            this.credentials = creds;
            if (creds instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
                this.projectId = ((com.google.auth.oauth2.ServiceAccountCredentials) creds).getProjectId();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(creds)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            this.active = true;
            System.out.println(">>> Firebase Admin SDK initialized successfully.");
        } catch (Exception e) {
            System.err.println(">>> Error initializing Firebase: " + e.getMessage());
        }
    }

    public boolean isFirebaseActive() {
        return active;
    }

    @Bean
    public Firestore getFirestore() {
        if (!active) {
            return null;
        }
        try {
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            System.err.println("Error initializing Firestore: " + e.getMessage());
            return null;
        }
    }
}
