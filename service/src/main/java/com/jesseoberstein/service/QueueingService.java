package com.jesseoberstein.service;

import com.google.firebase.messaging.Message;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@EnableScheduling
public class QueueingService {

    private final NotificationService notificationService;
    private final Queue<Message> queue;

    public QueueingService(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.queue = new LinkedList<>();
    }

    public void add(Message message) {
        queue.add(message);
    }

    // Send all of the messages in the queue every 15 seconds.
    @Async
    @Scheduled(fixedRate = 15 * 1_000)
    public void flush() {
        int queueSize = queue.size();
        List<Message> messages = new ArrayList<>(queueSize);

        while (queueSize > 0) {
            messages.add(queue.remove());
            queueSize--;
        }

        if (!messages.isEmpty()) {
            notificationService.send(messages);
        }
    }
}
