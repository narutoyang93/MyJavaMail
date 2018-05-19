package com.example.naruto.myapplication.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.example.naruto.myapplication.Model.MailInfo;
import com.example.naruto.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate ${Date}
 * @Note
 */
public class MyDBOpenHelper extends SQLiteOpenHelper {
    private Context context;
    private SQLiteDatabase dbWrite;
    private SQLiteDatabase dbRead;
    private String userId;
    private final static String MAIL_TABLE_NAME = "mail";
    private final static String ATTACHMENT_TABLE_NAME = "attachment";
    private String dateFormat = "";

    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        if (dbRead == null) {
            // 用可读方法打开数据库
            dbRead = this.getReadableDatabase();
        }
        if (dbWrite == null) {
            // 用可写方法打开数据库
            dbWrite = this.getWritableDatabase();
        }
        userId = (String) MyTools.readSharedPreferences(context, MyTools.KEY_USERID, MyTools.VALUETYPE_STRING, "");
        dateFormat = context.getString(R.string.date_format);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user(user_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "nickname VARCHAR(50), address VARCHAR(50))");
        db.execSQL("CREATE TABLE " + MAIL_TABLE_NAME + "(mail_id VARCHAR(256) PRIMARY KEY, " + "user_id INTEGER, send_DateTime VARCHAR(20),from_nickname VARCHAR(50),from_address VARCHAR(50),to_nickname VARCHAR(4000),to_address VARCHAR(4000),cc_nickname VARCHAR(4000),cc_address VARCHAR(4000),subject TEXT,content_text TEXT,content_html TEXT,picture_count INTEGER)");
        db.execSQL("CREATE TABLE picture(mail_id VARCHAR(50),content_id VARCHAR(50),path VARCHAR(256), primary key (mail_id,content_id))");
        db.execSQL("CREATE TABLE " + ATTACHMENT_TABLE_NAME + "(mail_id VARCHAR(50),attachment_id INTEGER, file_name VARCHAR(50),path VARCHAR(256),type VARCHAR(10),size INTEGER,is_have_download INTEGER, primary key (mail_id,attachment_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * 保存邮件数据
     *
     * @param mailInfo
     */
    public void addMail(MailInfo mailInfo) {
        String messageID = mailInfo.getMessageID();
        if (messageID == null || messageID.equals("")) {
            return;
        }
        String sql = "select mail_id from " + MAIL_TABLE_NAME + " where user_id=? and mail_id=?";
        Cursor cursor = dbRead.rawQuery(sql, new String[]{userId, messageID});

        if (cursor.getCount() > 0) {
            return;
        }

        // 将获得的值加入到集合中
        ContentValues cv = new ContentValues();
        cv.put("mail_id", messageID);
        cv.put("user_id", userId);
        cv.put("send_DateTime", MyTools.formateDate(mailInfo.getSendDate(), dateFormat));
        cv.put("from_nickname", mailInfo.getSender());
        cv.put("from_address", mailInfo.getSenderAddress());
        cv.put("to_nickname", mailInfo.getReceiver());
        cv.put("to_address", mailInfo.getReceiverAddress());
        cv.put("cc_nickname", "");
        cv.put("cc_address", "");
        cv.put("subject", mailInfo.getSubject());
        cv.put("content_text", mailInfo.getContentText().toString());
        cv.put("content_html", mailInfo.getContentHtml().toString());
        cv.put("picture_count", mailInfo.getPictureCount());
        dbWrite.insert(MAIL_TABLE_NAME, null, cv);
    }

    /**
     * 获取邮件数据
     *
     * @return
     */
    public List<MailInfo> getMails() {
        String sql = "select mail_id,send_DateTime,from_nickname,from_address,to_nickname,to_address,cc_nickname,cc_address,subject,content_text,content_html,picture_count from " + MAIL_TABLE_NAME + " where user_id=? ";
        List<MailInfo> list = new ArrayList<MailInfo>();
        Cursor cursor = dbRead.rawQuery(sql, new String[]{userId});
        while (cursor.moveToNext()) {
            MailInfo mailInfo = new MailInfo();
            mailInfo.setMessageID(cursor.getString(0));
            mailInfo.setSendDate(MyTools.stringToDate(cursor.getString(1), dateFormat));
            mailInfo.setSender(cursor.getString(2));
            mailInfo.setSenderAddress(cursor.getString(3));
            mailInfo.setReceiver(cursor.getString(4));
            mailInfo.setReceiverAddress(cursor.getString(5));
            mailInfo.setSubject(cursor.getString(8));
            mailInfo.setContentText(new StringBuffer(cursor.getString(9)));
            mailInfo.setContentHtml(new StringBuffer(cursor.getString(10)));
            mailInfo.setPictureCount(cursor.getInt(11));
            list.add(mailInfo);
        }
        cursor.close();
        return list;
    }

    /**
     * 更新邮件正文内容
     *
     * @param mailInfo
     */
    public void updateMailContent(MailInfo mailInfo) {
        ContentValues cv = new ContentValues();
        cv.put("content_html", mailInfo.getContentHtml().toString());
        dbWrite.update(MAIL_TABLE_NAME, cv, "user_id=? and mail_id=?", new String[]{userId, mailInfo.getMessageID()});
    }

    /**
     * 保存附件信息
     *
     * @param mailInfo
     */
    public void saveAttachment(MailInfo mailInfo) {
        String messageID = mailInfo.getMessageID();
        List<String> fileNameList = mailInfo.getAttachmentNameList();
        if (messageID == null || messageID.equals("") || fileNameList == null || fileNameList.size() == 0) {
            return;
        }

        for (int i = 0; i < fileNameList.size(); i++) {
            String sql = "select mail_id from " + ATTACHMENT_TABLE_NAME + " where mail_id=? and attachment_id=?";
            Cursor cursor = dbRead.rawQuery(sql, new String[]{messageID, String.valueOf(i)});
            if (cursor.getCount() > 0) {
                continue;
            }

            // 将获得的值加入到集合中
            ContentValues cv = new ContentValues();
            cv.put("mail_id", messageID);
            cv.put("attachment_id", i);
            cv.put("file_name", fileNameList.get(i));
            dbWrite.insert(ATTACHMENT_TABLE_NAME, null, cv);
        }

    }

    /**
     * 获取附件名
     *
     * @param mailId
     * @return
     */
    public List<String> getAttachmentName(String mailId) {
        String sql = "select mail_id,attachment_id,file_name from " + ATTACHMENT_TABLE_NAME + " where mail_id=? ";
        List<String> list = new ArrayList<String>();
        Cursor cursor = dbRead.rawQuery(sql, new String[]{mailId});
        while (cursor.moveToNext()) {
            list.add(cursor.getString(2));
        }
        cursor.close();
        return list;
    }

}
