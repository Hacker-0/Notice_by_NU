package com.alim.cse.noticebynu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.alim.cse.noticebynu.R;
import com.alim.cse.noticebynu.ViewerActivity;
import java.util.List;

public class Updates extends RecyclerView.Adapter<Updates.MyViewHolder>  {

    private TextView text;
    private boolean offline;
    private CardView cardView;
    private TextView text_date;
    private List<String> mDataset;
    private List<String> mDataDate;
    private List<String> mDataLink;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }
    }

    public Updates(List<String> Data, List<String> Date, List<String> Link, boolean offline) {
        this.mDataset = Data;
        this.mDataDate = Date;
        this.mDataLink = Link;
        this.offline = offline;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView;
        if (viewType==0)
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.updates_layout_start, parent, false);
        else if (viewType+1==mDataset.size())
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.updates_layout_end, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.updates_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final View rootView = holder.itemView;
        final Context context = holder.itemView.getContext();

        cardView = rootView.findViewById(R.id.card_view);
        text = rootView.findViewById(R.id.text);
        text_date = rootView.findViewById(R.id.text_date);
        text.setText(mDataset.get(position));
        text_date.setText(mDataDate.get(position));

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Link = mDataLink.get(position);
                String Extension = Link.substring(Link.length()-3);
                if (Extension.equals("pdf")) {
                    Intent intent = new Intent(context, ViewerActivity.class);
                    intent.putExtra("OFFLINE",offline);
                    intent.putExtra("FROM","OTHER");
                    intent.putExtra("PDF_NAME",mDataset.get(position));
                    intent.putExtra("PDF_LINK", Link);
                    context.startActivity(intent);
                } else {

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}