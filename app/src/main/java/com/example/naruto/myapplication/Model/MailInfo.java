package com.example.naruto.myapplication.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate ${Date}
 * @Note
 */
public class MailInfo implements Serializable {
    private String messageID;
    private String sender;
    private String senderAddress;
    private String receiver;
    private String receiverAddress;
    private Date sendDate;
    private String subject;
    private StringBuffer contentHtml;
    private StringBuffer contentText;
    private Map<String, byte[]> imageMap;
    private Map<String, byte[]> attachmentMap;
    private int pictureCount = 0;
    private List<String> attachmentNameList;

    public MailInfo() {
        contentHtml = new StringBuffer();
        contentText = new StringBuffer();
        imageMap = new HashMap<String, byte[]>();
        attachmentMap = new HashMap<String, byte[]>();
        attachmentNameList = new ArrayList<String>();
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Map<String, byte[]> getImageMap() {
        return imageMap;
    }

    public void setImageMap(Map<String, byte[]> imageMap) {
        this.imageMap = imageMap;
    }

    public StringBuffer getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(StringBuffer contentHtml) {
        this.contentHtml = contentHtml;
    }

    public void appendContentHtml(String content) {
        this.contentHtml.append(content);
    }

    public StringBuffer getContentText() {
        return contentText;
    }

    public void setContentText(StringBuffer contentText) {
        this.contentText = contentText;
    }

    public void appendContentText(String content) {
        this.contentText.append(content);
    }

    public Map<String, byte[]> getAttachmentMap() {
        return attachmentMap;
    }

    public void setAttachmentMap(Map<String, byte[]> attachmentMap) {
        this.attachmentMap = attachmentMap;
    }

    public int getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(int pictureCount) {
        this.pictureCount = pictureCount;
    }

    public List<String> getAttachmentNameList() {
        return attachmentNameList;
    }

    public void setAttachmentNameList(List<String> attachmentNameList) {
        this.attachmentNameList = attachmentNameList;
    }

    public void clearAttachment() {
        for (Map.Entry<String, byte[]> entry : attachmentMap.entrySet()) {
            entry.setValue(new byte[0]);
        }
    }
}
