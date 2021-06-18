package com.jesseoberstein.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Log4j2
@AllArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;

    private static final int BATCH_SIZE = 500;

    public void send(List<Message> messages) {
        List<List<Message>> batches = batchMessages(messages);
        batches.forEach(this::sendMessages);
    }

    public void subscribe(String topicName, String token) {
        log.info("Subscribing client to topic: {}", topicName);
        firebaseMessaging.subscribeToTopicAsync(List.of(token), topicName);
    }

    public void unsubscribe(String topicName, String token) {
        log.info("Unsubscribing client from topic: {}", topicName);
        firebaseMessaging.unsubscribeFromTopicAsync(List.of(token), topicName);
    }

    private List<List<Message>> batchMessages(List<Message> messages) {
        return IntStream.range(0, messages.size())
            .boxed()
            .collect(Collectors.groupingBy(index -> index / BATCH_SIZE))
            .values()
            .stream()
            .map(indices -> indices
                .stream()
                .map(messages::get)
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

    private void sendMessages(List<Message> messages) {
        if (messages.isEmpty()) {
            log.info("No messages to send in this batch.");
            return;
        }

        firebaseMessaging.sendAllAsync(messages);
        log.info("Sent out batch containing {} push notification", messages.size());
    }
}
