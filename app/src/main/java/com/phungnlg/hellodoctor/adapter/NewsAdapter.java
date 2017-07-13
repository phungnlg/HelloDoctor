package com.phungnlg.hellodoctor.adapter;

import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.phungnlg.hellodoctor.NewsItem;
import com.phungnlg.hellodoctor.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Phil on 7/12/2017.
 */

public class NewsAdapter extends  RecyclerView.Adapter<NewsAdapter.viewHolder> {

    private LinkedHashMap<String, NewsItem> listNews;
    private ArrayList<String> key;
    private ArrayList<NewsItem> value;

    public NewsAdapter(LinkedHashMap<String, NewsItem> listNews) {
        this.listNews = listNews;
        key = new ArrayList<>(listNews.keySet());
        value = new ArrayList<>(listNews.values());
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        holder.setTitle(value.get(position).getTitle());
        holder.setPhoto(value.get(position).getPhotoUrl());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), key.get(position), Toast.LENGTH_SHORT).show();
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(47219);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(v.getContext(), Uri.parse(value.get(position).getContentUrl()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listNews.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView iv;
        private TextView body;

        public viewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            iv = (ImageView) mView.findViewById(R.id.item_news_photo);
            body = (TextView) mView.findViewById(R.id.item_news_tv_title);
        }

        public void setPhoto(String photoUrl) {
            Picasso.with(mView.getContext())
                   .load(photoUrl)
                   .resize(200, 305)
                   .centerCrop()
                   .into(iv);
        }

        public void setTitle(String _title) {
            body.setText(_title);
        }
    }
}
