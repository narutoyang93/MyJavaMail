package com.example.naruto.myapplication.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.naruto.myapplication.Adapter.MailsAdapter;
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

import javax.mail.Message;
import javax.mail.MessagingException;

public class MailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressDialog dialog;
    private MailsAdapter adapter;
    private MyDBOpenHelper myDb;
    private EmailTools emailTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mails);
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在加载内容，请稍后……");
        // 获取/创建名为my.db的数据库，版本号为1
        myDb = new MyDBOpenHelper(this, "myMail.db", null, 1);
        emailTools = EmailTools.getInstance();
        emailTools.setMyDb(myDb);
        emailTools.setContext(this);
        initData();
    }

    private void initData() {
        new GetMailsTask().execute();
    }

    private void initView() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // 设置布局管理器
        recyclerView.setLayoutManager(mLayoutManager);
        // 设置adapter
        recyclerView.setAdapter(adapter);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 获取邮件的异步任务
     */
    class GetMailsTask extends AsyncTask<Void, Void, List<MailInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<MailInfo> doInBackground(Void... voids) {
            List<MailInfo> mailInfoList = myDb.getMails();
            if (mailInfoList.size() == 0) {
                try {
                    mailInfoList = emailTools.getMails(null, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("--->异常");
                }
            }
            return mailInfoList;
        }

        @Override
        protected void onPostExecute(List<MailInfo> messages) {
            super.onPostExecute(messages);
            System.out.println("--->size=" + messages.size());
            MailsAdapter.MailItemClickListener listener = new MailsAdapter.MailItemClickListener() {
                @Override
                public void onItemClick(View itemView, int position) {
                    Intent intent = new Intent(MailsActivity.this, DetailActivity.class);
                    MailInfo mailInfo = adapter.getMailInfoList().get(position);
                    intent.putExtra("mail", mailInfo);
                    startActivity(intent);
                }
            };
            adapter = new MailsAdapter(messages, listener);
            initView();
            dismissDialog();
        }
    }

}
