package com.patsofts.chatapp.Models;

public class MessageModel {
    private String senderId, receiverId, message, timeStamp;
    private boolean isseen;

    public MessageModel(String senderId, String receiverId, String message, boolean isseen, String timeStamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.isseen = isseen;
        this.timeStamp = timeStamp;
    }

    public MessageModel() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
