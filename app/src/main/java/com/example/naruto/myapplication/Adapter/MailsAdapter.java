package com.example.naruto.myapplication.Adapter;

import android.app.job.JobInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.naruto.myapplication.Model.MailInfo;
import com.example.naruto.myapplication.R;
import com.example.naruto.myapplication.Tools.MyTools;

import java.security.cert.TrustAnchor;
import java.util.List;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate ${Date}
 * @Note
 */
public class MailsAdapter extends RecyclerView.Adapter<MailsAdapter.ViewHolder> {

    private List<MailInfo> mailInfoList;
    private MailItemClickListener itemClickListener;

    public MailsAdapter(List<MailInfo> mailInfoList, MailItemClickListener itemClickListener) {
        this.mailInfoList = mailInfoList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public MailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_item, parent, false);
        // 实例化viewholder
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MailsAdapter.ViewHolder holder, int position) {
        MailInfo mail = mailInfoList.get(position);
        try {
            String contentData = mail.getContentText().toString();
            if (contentData.equals("")) {
                contentData = mail.getContentHtml().toString();
            }
            holder.senderTextView.setText(mail.getSender());
            holder.dateTextView.setText(MyTools.formateDate(mail.getSendDate(), "yyyy/MM/dd"));
            holder.subjectTextView.setText(mail.getSubject());
            holder.contentTextView.setText(contentData.length() > 100 ? contentData.substring(0, 100) : contentData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //点击事件

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mailInfoList == null ? 0 : mailInfoList.size();
    }

    public List<MailInfo> getMailInfoList() {
        return mailInfoList;
    }

    /**
     * ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView dateTextView;
        TextView subjectTextView;
        TextView contentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            senderTextView = (TextView) itemView.findViewById(R.id.sender);
            dateTextView = (TextView) itemView.findViewById(R.id.date);
            subjectTextView = (TextView) itemView.findViewById(R.id.subject);
            contentTextView = (TextView) itemView.findViewById(R.id.content);
        }
    }

    public interface MailItemClickListener {
        void onItemClick(View itemView, int position);
    }
}
