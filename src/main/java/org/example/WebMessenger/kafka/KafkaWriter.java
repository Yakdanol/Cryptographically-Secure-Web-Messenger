package org.example.WebMessenger.kafka;

import org.springframework.stereotype.Service;

@Service
public interface KafkaWriter {
    public void processing(byte[] messageBytes, String outputTopic);

    public void close();
}

