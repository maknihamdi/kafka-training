package com.kafka.training.scalableconsumer.model;

import java.time.LocalDateTime;

public class Message {
    private String id;
    private String content;
    private String sender;
    private LocalDateTime timestamp;

    public Message() {
    }

    public Message(String id, String content, String sender, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
