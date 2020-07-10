package com.example.recview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<News> news;

        DataAdapter(Context context, List<News> news) {
        this.news = news;
        this.inflater = LayoutInflater.from(context);
        }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
        News news = this.news.get(position);
        holder.timeView.setText(news.getTime());
        holder.titleView.setText(news.getTitle());
        holder.bodyView.setText(news.getText());
        }

    @Override
    public int getItemCount() {
        return news.size();
        }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView titleView, bodyView, timeView;
        ViewHolder(View view){
            super(view);
            timeView = (TextView) view.findViewById(R.id.time);
            titleView = (TextView) view.findViewById(R.id.title);
            bodyView = (TextView) view.findViewById(R.id.body);
        }

    }
}
