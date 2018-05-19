package com.example.naruto.myapplication.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.naruto.myapplication.Model.MailInfo;
import com.example.naruto.myapplication.R;
import com.example.naruto.myapplication.Tools.EmailTools;
import com.example.naruto.myapplication.Tools.MyDBOpenHelper;
import com.example.naruto.myapplication.Tools.MyTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

public class DetailActivity extends AppCompatActivity {
    private TextView subjectTextView;
    private TextView senderTextView;
    private TextView senderAddressTextView;
    private TextView receiverTextView;
    private TextView receiverAddressTextView;
    private TextView dateTimeTextView;
    private TextView attachmentTextView;
    private ProgressDialog dialog;
    private WebView webView;
    private MailInfo mail;
    private EmailTools emailTools;
    private String externalStorageFilePath = Environment.getExternalStorageDirectory() + "/01MyFolder/temp/";
    private String cacheDirFolderPath;//图片缓存目录
    private MyDBOpenHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        emailTools = EmailTools.getInstance();
        cacheDirFolderPath = getCacheDir().getAbsolutePath() + "/picture/";
        // 获取/创建名为my.db的数据库，版本号为1
        myDb = new MyDBOpenHelper(this, "myMail.db", null, 1);
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在加载内容，请稍后……");
        mail = (MailInfo) intent.getSerializableExtra("mail");

        subjectTextView = (TextView) findViewById(R.id.subject);
        senderTextView = (TextView) findViewById(R.id.sender);
        senderAddressTextView = (TextView) findViewById(R.id.sender_address);
        receiverTextView = (TextView) findViewById(R.id.receiver);
        receiverAddressTextView = (TextView) findViewById(R.id.receiver_address);
        dateTimeTextView = (TextView) findViewById(R.id.date_time);
        attachmentTextView = (TextView) findViewById(R.id.attachment);
        webView = (WebView) findViewById(R.id.web_view);

        WebSettings webSettings = webView.getSettings();
        //允许webview对文件的操作
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);


        if (isPictureHaveCache()) {
            initView(false);
            mail.setAttachmentNameList(myDb.getAttachmentName(mail.getMessageID()));
            attachmentTextView.setText(String.valueOf(mail.getAttachmentNameList().size()));
        } else {
            new GetMailsTask().execute();
        }
    }

    /**
     * 初始化页面
     *
     * @return
     */
    private String initView(boolean isNeedCachePicture) {
        subjectTextView.setText(mail.getSubject());
        senderTextView.setText(mail.getSender());
        senderAddressTextView.setText(mail.getSenderAddress());
        receiverTextView.setText(mail.getReceiver());
        receiverAddressTextView.setText(mail.getReceiverAddress());
        dateTimeTextView.setText(MyTools.formateDate(mail.getSendDate(), "yyyy/MM/dd HH:mm:ss"));

        String contentData = mail.getContentHtml().toString();
        if (contentData.equals("")) {
            contentData = mail.getContentText().toString();
        }
        webView.loadDataWithBaseURL("file://" + cacheDirFolderPath, isNeedCachePicture ? hendleBodyText() : contentData, "text/html", "utf-8", null);
        return contentData;
    }

    /**
     *
     */
    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 处理正文
     *
     * @return
     */
    private String hendleBodyText() {
        String contentData = mail.getContentHtml().toString();
        if (contentData.equals("")) {
            contentData = mail.getContentText().toString();
        }
        if (contentData.equals("")) {
            return contentData;
        }
        Map<String, byte[]> imageMap = mail.getImageMap();
        int i = 0;
        for (Map.Entry<String, byte[]> entry : imageMap.entrySet()) {
            String fileName = mail.getMessageID() + i + ".png";
            String key = entry.getKey();
            byte[] byteValue = entry.getValue();
            contentData = contentData.replace("cid:" + key, fileName);
            System.out.println("--->contentId=" + "cid:" + key);
            /*
            try {
                //保存图片到外部存储
                File f = MyTools.byteToFile(byteValue, externalStorageFilePath + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            // 保存图片到内部存储
            MyTools.saveImg(BitmapFactory.decodeByteArray(byteValue, 0, byteValue.length), fileName, 80, new File(cacheDirFolderPath), this, true, false);
            i++;
        }
        mail.setContentHtml(new StringBuffer(contentData));
        myDb.updateMailContent(mail);
        getAttachments();
        return contentData;
    }

    /**
     * 获取附件
     */
    private void getAttachments() {
        Map<String, byte[]> attachmentMap = mail.getAttachmentMap();
        List<String> fileNameList = new ArrayList<String>();
        for (Map.Entry<String, byte[]> entry : attachmentMap.entrySet()) {
            try {
                File f = MyTools.byteToFile(entry.getValue(), externalStorageFilePath + entry.getKey());
                fileNameList.add(entry.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mail.setAttachmentNameList(fileNameList);
        attachmentTextView.setText(String.valueOf(fileNameList.size()));
        myDb.saveAttachment(mail);
    }

    /**
     * 图片是否都已缓存
     *
     * @return
     */
    private boolean isPictureHaveCache() {
        int pictureCount = mail.getPictureCount();
        for (int i = 0; i < pictureCount; i++) {
            File file = new File(cacheDirFolderPath, mail.getMessageID() + i + ".png");
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 转发
     *
     * @param view
     * @throws Exception
     */
    public void forward(View view) throws Exception {
        new ForwardMailsTask().execute();
    }

    /**
     * 获取邮件的异步任务
     */
    class GetMailsTask extends AsyncTask<Void, Void, MailInfo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected MailInfo doInBackground(Void... voids) {
            List<MailInfo> mailInfoList = new ArrayList<MailInfo>();
            SearchTerm st = new MessageIDTerm(mail.getMessageID());
            //SearchTerm st = new AndTerm(new SentDateTerm(ComparisonTerm.EQ, mail.getSendDate()), new MessageIDTerm(mail.getMessageID()));
            try {
                mailInfoList = emailTools.getMails(st, true);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("--->异常");
            }
            System.out.println("--->size=" + mailInfoList.size());
            if (mailInfoList.size() > 0) {
                mail = mailInfoList.get(0);
            }
            return mail;
        }

        @Override
        protected void onPostExecute(MailInfo mail) {
            super.onPostExecute(mail);
            initView(true);
            dismissDialog();
        }
    }

    /**
     * 转发邮件的异步任务
     */
    class ForwardMailsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                emailTools.forwardMail(mail);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("--->转发异常");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissDialog();
        }
    }

}
