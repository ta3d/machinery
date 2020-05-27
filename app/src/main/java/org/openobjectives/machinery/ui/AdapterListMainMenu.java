package org.openobjectives.machinery.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.openobjectives.machinery.R;

import java.util.ArrayList;
import java.util.List;

;

public class AdapterListMainMenu extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private List<MainMenuItem> items = new ArrayList<>();
    private int lastPosition = -1;
    private boolean on_attach = true;

    public AdapterListMainMenu(Context context, List<MainMenuItem> items) {
        this.items = items;
        ctx = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._a1_item_main_menu, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MainMenuItem p = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            view.name.setText(p.name);
            view.url.setText(p.url);
            view.image.setImageResource(p.image);
            if (position != 0) {
                view.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(view, items.get(position), position);
                        }
                    }
                });
            }
        } else {
            ListEntryViewHolder view = (ListEntryViewHolder) holder;
            view.listentry.setText(p.name);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, MainMenuItem obj, int position);
    }

    public static class ListEntryViewHolder extends RecyclerView.ViewHolder {
        public TextView listentry;

        public ListEntryViewHolder(View v) {
            super(v);
            listentry = (TextView) v.findViewById(R.id.listentry);
        }
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView url;
        public View parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            url = (TextView) v.findViewById(R.id.url);
            parent = (View) v.findViewById(R.id.parent);
        }
    }


}