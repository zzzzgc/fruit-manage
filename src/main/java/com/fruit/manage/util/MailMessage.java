package com.fruit.manage.util;

/**
 * @author partner
 * @date 2018/5/24 19:54
 */
public class MailMessage {
    private String id;
    private String subject;
    private String content;
    private String[] receiveAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String[] receiveAddress) {
        this.receiveAddress = receiveAddress;
    }
}
