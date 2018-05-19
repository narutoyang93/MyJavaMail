package com.example.naruto.myapplication.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.naruto.myapplication.R;
import com.example.naruto.myapplication.Tools.EmailTools;
import com.example.naruto.myapplication.Tools.MyTools;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int[] REQUEST_CODEs = {1025, 1026};
    private List<Integer> requestCodeList = new ArrayList<Integer>();
    private static final String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCodeList.contains(requestCode)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限申请成功
                afterRequest();
            } else {
                // Permission Denied
                MyTools.OperationInterface operationInterface = new MyTools.OperationInterface() {

                    @Override
                    public void done(Object o) {
                        // TODO Auto-generated method stub
                        finish();
                    }
                };
                MyTools.OperationInterface operationInterface2 = new MyTools.OperationInterface() {

                    @Override
                    public void done(Object o) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                    }
                };
                MyTools.showMyDialog(MainActivity.this, null, "申请权限被拒绝，无法使用本软件，即将自动退出！", "OK", "Go to setting", false,
                        operationInterface, operationInterface2);
            }
            return;
        }
    }

    /**
     * 申请权限
     */
    private void requestPermissions() {
        for (int i = 0; i < permission.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission[i]}, REQUEST_CODEs[i]);
                requestCodeList.add(REQUEST_CODEs[i]);
            }
        }
        if (requestCodeList.size() == 0) {
            afterRequest();
        }
    }

    //当有权限之后执行此方法
    private void afterRequest() {
        MyTools.writeSharedPreferences(MainActivity.this, MyTools.KEY_USERID, "naruto", MyTools.VALUETYPE_STRING);
    }

    /**
     * 发送邮件
     *
     * @param view
     */
    public void sendMail(View view) {
        /**
         * 发送邮件线程
         */
        Runnable sendMailRun = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    EmailTools.sendMail(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("--->异常");
                }
            }
        };
        new Thread(sendMailRun).start();
        Toast.makeText(this, "已发送", Toast.LENGTH_LONG).show();
    }

    public void showMails(View view) {
        Intent intent = new Intent(this, MailsActivity.class);
        startActivity(intent);
    }
}
