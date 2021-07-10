package com.jesseoberstein.config;

import com.google.api.client.util.IOUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleFirebaseConfig {

    @Value("${SERVICE_ACCOUNT_JSON}")
    String serviceAccountJson;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        var inputStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));

        FirebaseOptions fbOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(inputStream))
            .build();

        return FirebaseApp.initializeApp(fbOptions);
    }

    @Bean
    @DependsOn("firebaseApp")
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
