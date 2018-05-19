package com.example.naruto.myapplication.Tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.naruto.myapplication.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate ${Date}
 * @Note
 */
public class MyTools {
    public static final int VALUETYPE_INT = 0;
    public static final int VALUETYPE_STRING = 1;
    public static final String FILE_NAME = "myJavamailAppData";

    public static final String KEY_USERID = "userId";

    public interface OperationInterface {
        void done(Object o);
    }

    /**
     * 读取本地配置数据
     *
     * @param context
     * @param key
     * @param valueType
     * @param defaultvalue
     * @return
     */
    public static Object readSharedPreferences(Context context, String key, int valueType, Object defaultvalue) {
        Object result = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        switch (valueType) {
            case VALUETYPE_INT:
                result = sharedPreferences.getInt(key, (int) defaultvalue);
                break;
            case VALUETYPE_STRING:
                result = sharedPreferences.getString(key, (String) defaultvalue);
                break;
        }
        return result;
    }

    /**
     * 写入本地配置数据
     *
     * @param context
     * @param key
     * @param value
     * @param valueType
     */
    public static void writeSharedPreferences(Context context, String key, Object value, int valueType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();// 获取编辑器
        switch (valueType) {
            case VALUETYPE_INT:
                editor.putInt(key, (int) value);
                break;
            case VALUETYPE_STRING:
                editor.putString(key, (String) value);
                break;
        }
        editor.commit();// 提交修改
    }

    /**
     * 消息弹窗
     *
     * @param context
     * @param title
     * @param message
     * @param okButtonText
     * @param cancelButonText
     * @param isCancelable
     * @param okOperation
     * @param cancelOperation
     */
    public static void showMyDialog(Context context, String title, String message,
                                    String okButtonText, String cancelButonText, boolean isCancelable,
                                    final MyTools.OperationInterface okOperation,
                                    final MyTools.OperationInterface cancelOperation) {
        LayoutInflater inflaterDl = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflaterDl.inflate(R.layout.dialog, null);
        final Dialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(isCancelable);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        LinearLayout titleLayout = (LinearLayout) layout.findViewById(R.id.title);
        TextView messageTextView = (TextView) layout.findViewById(R.id.message);
        Button okButton = (Button) layout.findViewById(R.id.okButton);
        Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
        View line = layout.findViewById(R.id.line2);

        if (title == null || title.equals("")) {// 没有title
            titleLayout.setVisibility(View.GONE);
        } else {
            TextView titleTextView = (TextView) titleLayout.getChildAt(0);
            titleTextView.setText(title);
        }

        messageTextView.setText(message);// 设置弹窗消息内容

        // 没有按钮
        if ((cancelButonText == null || cancelButonText.equals(""))
                && (okButtonText == null || okButtonText.equals(""))) {
            LinearLayout buttonLayout = (LinearLayout) layout.findViewById(R.id.button);
            buttonLayout.setVisibility(View.GONE);
        } else {
            // 没有取消按钮
            if (cancelButonText == null || cancelButonText.equals("")) {
                line.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
            } else {
                cancelButton.setText(cancelButonText);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        if (cancelOperation != null) {
                            cancelOperation.done(null);
                        }

                    }
                });
            }

            okButton.setText(okButtonText);
            okButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if (okOperation != null) {
                        okOperation.done(null);
                    }
                }
            });
        }

    }

    /**
     * 格式化日期
     *
     * @param date
     * @param format
     * @return
     */
    public static String formateDate(Date date, String format) {
        if (date == null) {
            date = new Date();
        }
        String s = "";
        SimpleDateFormat df = new SimpleDateFormat(format);// 设置日期格式
        s = df.format(date);
        return s;
    }

    /**
     * 格式化日期
     *
     * @param dateString
     * @param formateFrom
     * @param formateTo
     * @return
     */
    public static String formateDate(String dateString, String formateFrom, String formateTo) {
        if ((dateString == null || dateString.equals(""))
                || (formateFrom == null || formateFrom.equals(""))
                || (formateTo == null || formateTo.equals(""))) {
            return "";
        }
        String s = "";
        SimpleDateFormat sdfF = new SimpleDateFormat(formateFrom, Locale.ENGLISH);
        SimpleDateFormat sdfT = new SimpleDateFormat(formateTo, Locale.ENGLISH);
        Date date;
        try {
            date = sdfF.parse(dateString);
            s = sdfT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 字符串转Date
     *
     * @param dateString
     * @param format
     * @return
     */
    public static Date stringToDate(String dateString, String format) {
        if ((dateString == null || dateString.equals(""))
                || (format == null || format.equals(""))) {
            return null;
        }
        SimpleDateFormat sdfF = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = null;
        try {
            date = sdfF.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * InputStream转Byte[]
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static byte[] inputStreamToByte(InputStream is) throws IOException {
        System.out.println("--->inputStreamToByte开始");
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] buff = new byte[500]; //buff用于存放循环读取的临时数据
        int ch;
        while ((ch = is.read(buff,0,500)) != -1) {
            bytestream.write(buff,0,ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        System.out.println("--->inputStreamToByte结束");
        return imgdata;
    }

    /**
     * byte[]转File
     *
     * @param b
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File byteToFile(byte[] b, String fileName) throws IOException {
        File file = new File(fileName);
        // 如果文件已存在，则覆盖（删除）
        if (file.exists()) {
            file.delete();
        }
        OutputStream output = new FileOutputStream(file);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        bufferedOutput.write(b);
        return file;
    }

    /**
     * 保存图片到本地
     *
     * @param bitmap
     * @param fileName
     * @param quality
     * @param folderPath
     * @param context
     * @param overwrite
     * @param isNeedRefreshGallery 是否需要刷新图库
     */
    public static void saveImg(Bitmap bitmap, String fileName, int quality, File folderPath, Context context, boolean overwrite, boolean isNeedRefreshGallery) {
        // 保存图片
        if (!folderPath.exists()) {// 如果目标文件夹不存在，就自动创建
            folderPath.mkdir();
        }
        File file = new File(folderPath, fileName);
        if (file.exists()) {// 如果文件存在
            if (overwrite) {
                // 删除
                file.delete();
            } else {
                return;
            }
        }

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isNeedRefreshGallery) {
            // 通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }

}
