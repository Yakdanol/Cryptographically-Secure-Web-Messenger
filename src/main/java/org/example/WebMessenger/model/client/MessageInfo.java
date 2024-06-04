package org.example.WebMessenger.model.client;

import lombok.Builder;
import org.springframework.data.redis.core.RedisHash;
import org.example.WebMessenger.model.messages.Message;

import java.io.Serializable;

@Builder
@RedisHash("MessageInfo")
public class MessageInfo implements Serializable {
    private String id;

    private long from;

    private long to;

    private Message message;
}
