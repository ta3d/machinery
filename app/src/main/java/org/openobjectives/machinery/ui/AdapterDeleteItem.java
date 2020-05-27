package org.openobjectives.machinery.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.openobjectives.machinery.R;
import org.openobjectives.machinery.service.NetworkService;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;

public class AdapterDeleteItem extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Typeface tf;
    public String selectedNamespace = "any";
    boolean shortenPodnames = false;
    private List<HasMetadata> items = new ArrayList<>();
    private List<HasMetadata> items_swiped = new ArrayList<>();
    private List<Boolean> swiped = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private DbHelper mDb;
    private NetworkService mService;
    private String resource;
    private View parent_view;
    private Pod actualPod;
    private String replacement = "";

    public AdapterDeleteItem(Context context, List<HasMetadata> items, DbHelper db, NetworkService mService, String resource, View parent_view) {
        this.parent_view = parent_view;
        this.resource = resource;
        this.mService = mService;
        this.mDb = db;
        this.items = items;
        if (this.items == null) {
            this.items = new ArrayList<HasMetadata>(0);
        }
        swiped = items.stream()
                .filter(item -> item != null)
                .map(p -> new Boolean(false))
                .collect(Collectors.toList());

        mContext = context;
        tf = ResourcesCompat.getFont(mContext, R.font.jetbrainsmono_bold);
        shortenPodnames = db.getPrefBool("shortenPodnames");
        if (resource.equals(LocalDataStore.RES_POD) && shortenPodnames) {
            Set<String> results = GeneralHelper.longestCommonSubstrings(items.get(0).getMetadata().getName(), items.get(items.size() / 2).getMetadata().getName());
            replacement = results.iterator().next();
            if (replacement == null)
                replacement = "";
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._a3_entry_swipe, parent, false);
        vh = new ResourceViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ResourceViewHolder) {
            final ResourceViewHolder view = (ResourceViewHolder) holder;
            final HasMetadata p = items.get(position);
            if (resource.equals(LocalDataStore.RES_POD) && shortenPodnames)
                view.name.setText(
                        //with Replacement
                        p.getMetadata().getName().replace(replacement, "...")
                );
            else
                view.name.setText(
                        p.getMetadata().getName()
                );
            GeneralHelper.display(mContext, view.image, R.drawable.oo_kubernetes);
            if (resource.equals(LocalDataStore.RES_POD)) {
                actualPod = (Pod) items.get(position);
                GeneralHelper.display(mContext, view.image, R.drawable.oo_cube);
                if (!actualPod.getStatus().getPhase().equals("Running"))
                    GeneralHelper.display(mContext, view.image, R.drawable.oo_cube_scan);
            } else if (resource.equals(LocalDataStore.RES_SERVICE))
                GeneralHelper.display(mContext, view.image, R.drawable.oo_share_variant);
            else if (resource.equals(LocalDataStore.RES_DEPLOYMENT))
                GeneralHelper.display(mContext, view.image, R.drawable.oo_cube_unfolded);
            else if (resource.equals(LocalDataStore.RES_NAMESPACE))
                GeneralHelper.display(mContext, view.image, R.drawable.oo_label_outline);
            else if (resource.equals(LocalDataStore.RES_RESOURCES)) {
                HasMetadata x = items.get(position);
                if (x.getMetadata().getAdditionalProperties().containsKey("istio"))
                    GeneralHelper.display(mContext, view.image, R.drawable.oo_istio_bluelogo_whitebackground_framed);
                else if (x.getMetadata().getAdditionalProperties().containsKey("k8s"))
                    GeneralHelper.display(mContext, view.image, R.drawable.oo_kubernetes);
                else
                    GeneralHelper.display(mContext, view.image, R.drawable.oo_rhombus_outline);
            }
            view.base_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            view.clickrevert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //items.get(position).swiped = false;
                    swiped.set(position, false);
                    items_swiped.remove(items.get(position));
                    notifyItemChanged(position);
                }
            });
            //EDGE CASE on "namespace selected" delete swipe
            if (swiped.size() <= position) {
                view.base_parent.setVisibility(View.GONE);
                notifyItemChanged(position);
            } else {
                if (swiped.get(position)) {
                    view.base_parent.setVisibility(View.GONE);
                } else {
                    view.base_parent.setVisibility(View.VISIBLE);
                }
            }

            //highlight Namespace selected
            if (resource.equals(LocalDataStore.RES_NAMESPACE) && !selectedNamespace.equals("any") && selectedNamespace.equals(p.getMetadata().getName())) {
                view.base_parent.setBackgroundColor(Color.GRAY);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (HasMetadata s : items_swiped) {
                    int index_removed = items.indexOf(s);
                    if (index_removed != -1) {
                        items.remove(index_removed);
                        notifyItemRemoved(index_removed);
                    }
                }
                items_swiped.clear();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onItemDelete(int position) {
        if (swiped.get(position)) {
            swiped.remove(position);
            items_swiped.remove(items.get(position));
            //TODO mDb.deleteHasMetadata(items.get(position).getId());   -> CALL SERVICE AND WAIT
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean deleted = mService.deleteResource(resource, items.get(position).getMetadata().getName(), items.get(position).getMetadata().getNamespace());
                    if (deleted) {
                        items.remove(items.get(position));
                        notifyItemRemoved(position);
                    }
                    Snackbar.make(parent_view, "Deletion successful: " + deleted, Snackbar.LENGTH_SHORT).show();
                }
            }, 0);
            return;
        }
        swiped.set(position, true);
        items_swiped.add(items.get(position));
        notifyItemChanged(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, HasMetadata obj, int position);
    }

    public class ResourceViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public ImageButton clickdelete;
        public Button clickrevert;
        public View base_parent;

        public ResourceViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            name.setTypeface(tf);
            clickdelete = (ImageButton) v.findViewById(R.id.click_delete);
            if (!(resource.equals(LocalDataStore.RES_POD) || resource.equals(LocalDataStore.RES_SERVICE) || resource.equals(LocalDataStore.RES_DEPLOYMENT)))
                clickdelete.setVisibility(View.GONE);
            clickrevert = (Button) v.findViewById(R.id.click_revert);
            base_parent = (View) v.findViewById(R.id.base_parent);
            base_parent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.oo_grey_10));
        }

    }

}