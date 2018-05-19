package com.example.naruto.myapplication.Tools;

import android.content.Context;
import android.os.Environment;

import com.example.naruto.myapplication.Model.MailInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchTerm;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate ${Date}
 * @Note
 */
public class EmailTools {
    //发件人地址
    private static String senderAddress = "naruto_yang93@163.com";
    //收件人地址
    private static String recipientAddress = "276875584@qq.com";
    //发件人账户授权码
    private static String senderPassword = "naruto931025";
    //收件人账户授权码
    private static String recipientPassword = "entvztgitbszbjhh";
    private static final int MAIL_PAGE_SIZE = 10;
    private static String receiveHost;
    private static String receiveProtocol;
    private static final String[] PROTOCOLS = {"pop3", "imap"};
    private static final String[] PORTS = {"995", "993"};
    private static final int PROTOCOL_POSITION = 1;
    private static String externalStorageFilePath = Environment.getExternalStorageDirectory() + "/01MyFolder/temp/";
    private static String cacheDirFolderPath;//图片缓存目录

    public static Session receiveSession;
    public static Store receiveStore;
    public static Folder receiveFolder;

    public static MyDBOpenHelper myDb;
    public static Context context;

    //将构造函数私有化
    private EmailTools() {
        //1、连接邮件服务器的参数配置
        Properties props = new Properties();
        receiveProtocol = PROTOCOLS[PROTOCOL_POSITION];
        String port = PORTS[PROTOCOL_POSITION];
        if (PROTOCOL_POSITION == 0) {
            receiveHost = "pop.qq.com";
            props.setProperty("mail." + receiveProtocol + ".socketFactory.fallback", "true");
        } else {
            receiveHost = "imap.qq.com";
            props.setProperty("mail." + receiveProtocol + ".auth.login.disable", "true");
        }

        //设置传输协议
        props.setProperty("mail.store.protocol", receiveProtocol);
        props.setProperty("mail.transport.protocol", "smtp");
        //设置收件人的服务器
        props.setProperty("mail." + receiveProtocol + ".host", receiveHost);
        //以下为QQ邮箱所需的设置，其他邮箱可能不需要
        props.setProperty("mail." + receiveProtocol + ".port", port);
        // SSL安全连接参数
        props.setProperty("mail." + receiveProtocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail." + receiveProtocol + ".socketFactory.port", port);

//2、创建定义整个应用程序所需的环境信息的 Session 对象
        receiveSession = Session.getInstance(props);
//设置调试信息在控制台打印出来
        // session.setDebug(true);
    }

    public static EmailTools getInstance() {
        return EmailToolsHolder.INSTANCE;
    }

    /**
     * 初始化
     *
     * @throws MessagingException
     */
    public void init() throws MessagingException {
        receiveStore = receiveSession.getStore(receiveProtocol);
//连接收件人POP3服务器
        receiveStore.connect(receiveHost, recipientAddress, recipientPassword);
//获得用户的邮件账户，注意通过pop3协议获取某个邮件夹的名称只能为inbox
        receiveFolder = receiveStore.getFolder("inbox");
    }

    public static class EmailToolsHolder {
        static final EmailTools INSTANCE = new EmailTools();//单例对象实例
    }

    public static MyDBOpenHelper getMyDb() {
        return myDb;
    }

    public static void setMyDb(MyDBOpenHelper myDb) {
        EmailTools.myDb = myDb;
    }

    public static void setContext(Context context) {
        EmailTools.context = context;
        EmailTools.cacheDirFolderPath = context.getCacheDir().getAbsolutePath() + "/picture/";
    }

    /**
     * MimeMessage比较器
     */
    class MimeMessageComparator implements Comparator<MimeMessage> {

        @Override
        public int compare(MimeMessage m1, MimeMessage m2) {
            try {
                return m1.getMessageID().equals(m2.getMessageID()) ? 0 : -1;
            } catch (MessagingException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /**
     * 创建邮件对象
     *
     * @param session
     * @return
     * @throws Exception
     */
    public static MimeMessage createMail(Session session, MailInfo mailInfo) throws Exception {
        //创建一封邮件的实例对象
        MimeMessage mail = new MimeMessage(session);
        String fromAddress = senderAddress;
        String toAddress = recipientAddress;
        String subject = "邮件主题";

        MimeBodyPart mbp = new MimeBodyPart();
        MimeMultipart mm_all = new MimeMultipart();
        if (mailInfo != null) {
            fromAddress = recipientAddress;
            toAddress = "yangxiaocheng@eims.com.cn";
            subject = "转发：" + mailInfo.getSubject();
            int pictureCount = mailInfo.getPictureCount();
            List<String> attachmentNameList = mailInfo.getAttachmentNameList();
            String content = mailInfo.getContentHtml().toString();
            System.out.println("--->pictureCount=" + pictureCount);
            //处理正文图片
            if (pictureCount > 0) {
                MimeMultipart mm_text_image = new MimeMultipart();
                List<MimeBodyPart> mimeBodyPartList = new ArrayList<MimeBodyPart>();
                //mm_text_image.addBodyPart(new MimeBodyPart());
                System.out.println("--->179pictureCount=" + pictureCount);
                for (int i = 0; i < pictureCount; i++) {
                    String fileName = mailInfo.getMessageID() + i + ".png";
                    String contentId = "naruto" + i;
                    content = content.replace(fileName, "cid:" + contentId);
                    MimeBodyPart imageMbp = new MimeBodyPart();
                    imageMbp.setDataHandler(new DataHandler(new FileDataSource(new File(cacheDirFolderPath + fileName))));                   // 将图片数据添加到“节点”
                    imageMbp.setContentID(contentId);
                    //mm_text_image.addBodyPart(imageMbp);
                    mimeBodyPartList.add(imageMbp);
                }
                //创建文本“节点”
                MimeBodyPart text = new MimeBodyPart();
                System.out.println("--->188content=" + content);
                text.setContent(content, "text/html;charset=UTF-8");
                // （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
                mm_text_image.addBodyPart(text);
                for (int i = 0; i < mimeBodyPartList.size(); i++) {
                    mm_text_image.addBodyPart(mimeBodyPartList.get(i));
                }
                mm_text_image.setSubType("related");    // 关联关系
                mbp.setContent(mm_text_image);
            } else {
                if (content.equals("")) {
                    content = mailInfo.getContentText().toString();
                }
                mbp.setContent(content, "text/html;charset=UTF-8");
            }

            mm_all.addBodyPart(mbp);

            //处理附件
            if (attachmentNameList.size() > 0) {
                System.out.println("--->attachmentNameList.size()=" + attachmentNameList.size());
                for (int i = 0; i < attachmentNameList.size(); i++) {
                    // 9. 创建附件“节点”
                    String fileName = attachmentNameList.get(i);
                    MimeBodyPart attachment = new MimeBodyPart();
                    attachment.setDataHandler(new DataHandler(new FileDataSource(new File(externalStorageFilePath + fileName))));                                             // 将附件数据添加到“节点”
                    attachment.setFileName(MimeUtility.encodeText(fileName));              // 设置附件的文件名（需要编码）

                    // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
                    mm_all.addBodyPart(mbp);
                    mm_all.addBodyPart(attachment);
                    mm_all.setSubType("mixed");         // 混合关系
                }
            }

        } else {
            // 创建图片“节点”
            String imageId = "image";
            MimeBodyPart image = new MimeBodyPart();
            File imageFile = new File(Environment.getExternalStorageDirectory(), "01MyFolder/picture/123.png");
            image.setDataHandler(new DataHandler(new FileDataSource(imageFile)));                   // 将图片数据添加到“节点”
            image.setContentID(imageId);

            // 6. 创建文本“节点”
            MimeBodyPart text = new MimeBodyPart();
            text.setContent("这是一张图片<br/><img src='cid:" + imageId + "'/>", "text/html;charset=UTF-8");

            // 7. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
            MimeMultipart mm_text_image = new MimeMultipart();
            mm_text_image.addBodyPart(text);
            mm_text_image.addBodyPart(image);
            mm_text_image.setSubType("related");    // 关联关系

            // 8. 将 文本+图片 的混合“节点”封装成一个普通“节点”
            //    最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
            //    上面的 mm_text_image 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
            mbp.setContent(mm_text_image);

            // 9. 创建附件“节点”
            MimeBodyPart attachment = new MimeBodyPart();
            File attachmentFile = new File(Environment.getExternalStorageDirectory(), "01MyFolder/file/naruto.zip");
            DataHandler dh2 = new DataHandler(new FileDataSource(attachmentFile));  // 读取本地文件
            attachment.setDataHandler(dh2);                                             // 将附件数据添加到“节点”
            attachment.setFileName(MimeUtility.encodeText(dh2.getName()));              // 设置附件的文件名（需要编码）

            // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
            mm_all.addBodyPart(mbp);
            mm_all.addBodyPart(attachment);
            mm_all.setSubType("mixed");         // 混合关系
        }

        //设置发件人地址
        mail.setFrom(new InternetAddress(fromAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        mail.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toAddress));
        //设置邮件主题
        mail.setSubject(subject, "UTF-8");

        // 11. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        mail.setContent(mm_all);

        // 12. 设置发件时间
        mail.setSentDate(new Date());

        // 13. 保存上面的所有设置
        mail.saveChanges();

        return mail;
    }

    /**
     * 发送邮件
     *
     * @throws Exception
     */
    public static void sendMail(MailInfo mailInfo) throws Exception {
        String host = "smtp.163.com";
//1、连接邮件服务器的参数配置
        Properties props = new Properties();
//设置用户的认证方式
        props.setProperty("mail.smtp.auth", "true");
//设置传输协议
        props.setProperty("mail.transport.protocol", "smtp");

//设置发件人的SMTP服务器地址
        props.setProperty("mail.smtp.host", host);
//2、创建定义整个应用程序所需的环境信息的 Session 对象
        Session session = Session.getInstance(props);
//设置调试信息在控制台打印出来
        session.setDebug(true);
//3、创建邮件的实例对象
        Message msg = createMail(session, mailInfo);
//4、根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
//5、设置发件人的账户名和密码
        transport.connect(senderAddress, senderPassword);
//6、发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(msg, msg.getAllRecipients());

//7、关闭邮件连接
        transport.close();
    }


    /**
     * 转发
     *
     * @param mailInfo
     * @throws Exception
     */
    public void forwardMail(MailInfo mailInfo) throws Exception {
        Properties props = new Properties();
        String port = "465";
        // 开启debug调试
        props.setProperty("mail.debug", "true");
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 设置邮件服务器主机名
        props.setProperty("mail.host", "smtp.qq.com");
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp");

        //以下为QQ邮箱所需的设置，其他邮箱可能不需要
        props.setProperty("mail.smtp.port", port);
        // SSL安全连接参数
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.port", port);


/*        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", "smtp.qq.com");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");*/


        Session session = Session.getInstance(props);

//3、创建邮件的实例对象
        Message msg = createMail(session, mailInfo);
//4、根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
//5、设置发件人的账户名和密码
        transport.connect("smtp.qq.com", recipientAddress, recipientPassword);
//6、发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(msg, msg.getAllRecipients());

//7、关闭邮件连接
        transport.close();
    }


    /**
     * 根据条件获取邮件
     *
     * @param st
     * @return
     * @throws Exception
     */
    public List<MailInfo> getMails(SearchTerm st, boolean isNeedDeepParser) throws Exception {
        init();
//设置对邮件账户的访问权限
        receiveFolder.open(Folder.READ_WRITE);
        List<MailInfo> mailInfoList = new ArrayList<MailInfo>();
        //mailInfoList.add(parserMail(messages[messages.length - 1]));
        Message[] messages = new Message[1];
        if (st == null) {
            int messageCount = receiveFolder.getMessageCount();
            System.out.println("--->messagesCount=" + messageCount);
            messages = receiveFolder.getMessages(messageCount - 1 - MAIL_PAGE_SIZE, messageCount - 1);
            //messages = receiveFolder.getMessages(messageCount - 1 - 4, messageCount - 1 - 4);
        } else {
            System.out.println("--->开始搜索");
            //messages = receiveFolder.search(st);
            Message[] messagesArray = receiveFolder.getMessages();
            for (int i = messagesArray.length - 1; i >= 0; i--) {
                Message m = messagesArray[i];
                if (st.match(m)) {
                    messages[0] = m;
                    break;
                }
            }
            System.out.println("--->结束搜索");
            System.out.println("--->搜索结果：" + (messages.length > 0 ? "找到了！" : "没找到！"));
        }
/*        for (int i = messages.length - 1; i > messages.length - 1 - MAIL_PAGE_SIZE; i--) {
            mailInfoList.add(parserMail(messages[i], isNeedDeepParser));
        }*/
        for (int i = messages.length - 1; i >= 0; i--) {
            if (messages[i] != null) {
                MailInfo mailInfo = parserMail(messages[i], isNeedDeepParser);
                mailInfoList.add(mailInfo);
                myDb.addMail(mailInfo);
            }
        }

//关闭邮件夹对象
        receiveFolder.close(true);
//关闭连接对象
        receiveStore.close();
        return mailInfoList;
    }


    /**
     * 解析邮件
     *
     * @param m
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public static MailInfo parserMail(Message m, boolean isNeedDeepParser) throws IOException, MessagingException {
        if (m == null) {
            return null;
        }
        MimeMessage mm = (MimeMessage) m;
        MailInfo mailInfo = new MailInfo();
        if (m.getContentType().toLowerCase().contains("text")) {
            mailInfo.appendContentText((String) m.getContent());
        } else {
            parseMultipart((Multipart) m.getContent(), mailInfo, isNeedDeepParser);
        }
        setAddressAndNickname(mailInfo, mm, "from");
        setAddressAndNickname(mailInfo, mm, "to");
        mailInfo.setMessageID(((MimeMessage) m).getMessageID());
        mailInfo.setSendDate(m.getSentDate());
        mailInfo.setSubject(m.getSubject());
        return mailInfo;
    }

    /**
     * 邮箱地址数组转字符串（添加";"分隔符）
     *
     * @param addresseArray
     * @return
     */
    public static String addressArrayToString(Address[] addresseArray) {
        if (addresseArray == null || addresseArray.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer("");
        for (Address address : addresseArray) {
            sb.append((((InternetAddress) address).getAddress() + ";"));
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * 昵称集合转字符串（添加";"分隔符）
     *
     * @param nicnameList
     * @return
     */
    public static String nicknameListToString(List<String> nicnameList) {
        if (nicnameList == null || nicnameList.size() == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer("");
        for (String s : nicnameList) {
            sb.append((s + ";"));
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * 设置昵称和邮箱
     *
     * @param mailInfo
     * @param mm
     * @param type
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static void setAddressAndNickname(MailInfo mailInfo, MimeMessage mm, String type) throws MessagingException, UnsupportedEncodingException {
        Address[] addressArray = getAddress(mm, type);
        String nickname = "";
        String address = "";
        if (addressArray != null || addressArray.length >= 0) {
            nickname = nicknameListToString(getNicknameByAddress(addressArray));
            address = addressArrayToString(addressArray);
        }
        switch (type) {
            case "from":
                mailInfo.setSender(nickname);
                mailInfo.setSenderAddress(address);
                break;
            case "to":
                mailInfo.setReceiver(nickname);
                mailInfo.setReceiverAddress(address);
                break;
            case "cc":
                break;
            case "bc":
                break;
        }
    }

    /**
     * 获取邮件地址
     *
     * @param msg
     * @param type
     * @return
     * @throws MessagingException
     */
    public static Address[] getAddress(MimeMessage msg, String type) throws MessagingException {
        switch (type) {
            case "from":
                return msg.getFrom();
            case "to":
                return msg.getRecipients(Message.RecipientType.TO);
            case "cc":
                return msg.getRecipients(Message.RecipientType.CC);
            case "bc":
                return msg.getRecipients(Message.RecipientType.BCC);
        }
        return null;
    }

    /**
     * 获取昵称
     *
     * @param msg
     * @param type
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static List<String> getNickname(MimeMessage msg, String type) throws MessagingException, UnsupportedEncodingException {
        List<String> nameList = new ArrayList<String>();
        List<Address> addressList = new ArrayList<Address>();
        Address[] addresses = getAddress(msg, type);
        if (addresses != null && addresses.length > 0) {
            addressList = Arrays.asList(addresses);
            for (Address address : addressList) {
                nameList.add(getNicknameByAddress(address));
            }
        }
        return nameList;
    }

    /**
     * 获取昵称
     *
     * @param addresses
     * @return
     * @throws UnsupportedEncodingException
     */
    public static List<String> getNicknameByAddress(Address[] addresses) throws UnsupportedEncodingException {
        List<String> nameList = new ArrayList<String>();
        List<Address> addressList = new ArrayList<Address>();
        if (addresses != null && addresses.length > 0) {
            addressList = Arrays.asList(addresses);
            for (Address address : addressList) {
                nameList.add(getNicknameByAddress(address));
            }
        }
        return nameList;
    }

    /**
     * 根据邮箱获取用户昵称
     *
     * @param address
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getNicknameByAddress(Address address) throws UnsupportedEncodingException {
        String userName = "";
        userName = ((InternetAddress) address).getPersonal();
        if (userName == null) {
            String[] a = address.toString().split("@");
            userName = a.length > 0 ? a[0] : "";
        }
        userName = MimeUtility.decodeText(userName) + " ";
        return userName;
    }


    /**
     * 邮件解析
     *
     * @param multipart
     * @param mailInfo
     * @param isNeedDeepParser 是否解析图片（仅用于邮件列表简单数据的显示）
     * @throws MessagingException
     * @throws IOException
     */
    public static void parseMultipart(Multipart multipart, MailInfo mailInfo, boolean isNeedDeepParser) throws MessagingException, IOException {
        int count = multipart.getCount();
        System.out.println("--->multipartCount=" + count);
        for (int idx = 0; idx < count; idx++) {
            BodyPart bodyPart = multipart.getBodyPart(idx);
            String contentType = bodyPart.getContentType().toLowerCase().split("/")[0];
            switch (contentType) {
/*                case "multipart/related":
                case "multipart/mixed":
                case "multipart/alternative":*/
                case "multipart":
                    parseMultipart((Multipart) bodyPart.getContent(), mailInfo, isNeedDeepParser);
                    break;
                case "text":
                    parseText(bodyPart, mailInfo);
                    break;
                case "image":
                    if (isNeedDeepParser) {
                        System.out.println("--->开始解析图片");
                        parseImage(bodyPart, mailInfo.getImageMap());
                        System.out.println("--->结束解析图片");
                    }
                    mailInfo.setPictureCount(mailInfo.getPictureCount() + 1);
                    break;
                case "application"://附件
                    if (isNeedDeepParser) {
                        System.out.println("--->开始解析附件");
                        parseAttachment(bodyPart, mailInfo.getAttachmentMap());
                        System.out.println("--->结束解析附件");
                    }
                    break;
                default:
                    System.out.println("--->contentType=" + contentType);
                    break;
            }
        }
    }

    /**
     * 解析Text
     *
     * @param bodyPart
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public static void parseText(BodyPart bodyPart, MailInfo mailInfo) throws IOException, MessagingException {
        System.out.println("--->TextContentType" + bodyPart.getContentType().split(";")[0]);
        if (bodyPart.isMimeType("text/html")) {
            mailInfo.appendContentHtml((String) bodyPart.getContent());
        } else {
            mailInfo.appendContentText((String) bodyPart.getContent());
        }
    }

    /**
     * 解析图片
     *
     * @param bodyPart
     * @param imgMap
     * @throws IOException
     * @throws MessagingException
     */
    public static void parseImage(BodyPart bodyPart, Map<String, byte[]> imgMap) throws IOException, MessagingException {
        Object content = bodyPart.getContent();
        String contentID = ((String[]) bodyPart.getHeader("Content-ID"))[0];
        if (contentID.charAt(0) == '<') {
            contentID = contentID.substring(1);
        }
        if (contentID.charAt(contentID.length() - 1) == '>') {
            contentID = contentID.substring(0, contentID.length() - 1);
        }
        InputStream is = (InputStream) content;
        imgMap.put(contentID, MyTools.inputStreamToByte(is));
    }

    /**
     * 解析附件
     *
     * @param bodyPart
     * @param attachmentMap
     * @throws MessagingException
     * @throws IOException
     */
    public static void parseAttachment(BodyPart bodyPart, Map<String, byte[]> attachmentMap) throws MessagingException, IOException {
        String disposition = bodyPart.getDisposition();
        System.out.println("--->disposition=" + disposition);
        if (disposition.equalsIgnoreCase(BodyPart.ATTACHMENT)) {
            String fileName = bodyPart.getFileName();
            if (fileName.startsWith("=?")) {
                // 把文件名编码成符合RFC822规范
                fileName = MimeUtility.decodeText(fileName);
            }
            InputStream is = bodyPart.getInputStream();
            attachmentMap.put(fileName, MyTools.inputStreamToByte(is));
        }
    }
}
