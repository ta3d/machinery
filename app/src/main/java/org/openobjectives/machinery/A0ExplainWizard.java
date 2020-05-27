/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.openobjectives.machinery.ui.LinePagerIndicatorDecoration;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <B>Class: A0ExplainWizard </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The Intro Wizard <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */

public class A0ExplainWizard extends AppCompatActivity {

    private static final String TAG = A0ExplainWizard.class.getSimpleName();
    private static final int READ_EXTERNAL_STORAGE = 0;
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    public A0ExplainWizard mActual;
    protected RecyclerView mRecyclerView;
    private DbHelper mDb;
    private ExplainAdapter mExplainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a0_explain_wizard);
        mDb = new DbHelper(this);
        mActual = this;
        if (ContextCompat.checkSelfPermission(mActual,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActual,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE);
        }
        mExplainAdapter = new ExplainAdapter(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = getCurrentItem();
                    onPageChanged(position);
                }
            }
        });
        onCreateRecyclerView(savedInstanceState);
    }

    private void onPageChanged(int position) {
        if (position == 6 )
            if(mDb.getPrefBool("termsAccepted") == false)
                showTermServicesDialog();
        if (position == 7 && mDb.getPrefBool("termsAccepted") == true) {
            ((ImageView) findViewById(R.id.ivImage)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A0ExplainWizard.this, A1Main.class
                            .getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
            setCurrentItem(7,false);
        }
    }

    public void onCreateRecyclerView(Bundle savedInstanceState) {
        Context context = this;
        mRecyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mRecyclerView.setAdapter(mExplainAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        // add pager behavior
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        // pager indicator
        mRecyclerView.addItemDecoration(new LinePagerIndicatorDecoration());

    }


    private void showTermServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActual);
        dialog.setTitle(getResources().getString(R.string.terms));
        dialog.setMessage(GeneralHelper.readAssetFileAsString("privacypolicy.txt",getApplicationContext())+
                            GeneralHelper.readAssetFileAsString("eula.txt",getApplicationContext()));
        dialog.setPositiveButton(getResources().getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface,
                                        int i) {
                        mDb.setPrefBool("termsAccepted", true);
                        dialogInterface.dismiss();
                    }
                });
        dialog.setNegativeButton(getResources().getString(R.string.decline),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface,
                                        int i) {
                        mRecyclerView.scrollToPosition(5);
                        mDb.setPrefBool("termsAccepted", false);
                        dialogInterface.cancel();
                    }
                });
        dialog.show();
    }



    public class ExplainAdapter extends RecyclerView.Adapter<ExplainAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<String> headerList = new ArrayList<>(Arrays.asList(LocalDataStore.EXPLAINHEADERS));
        private ArrayList<String> infoList = new ArrayList<>(Arrays.asList(LocalDataStore.EXPLAININFOS));
        private ArrayList<Integer> imagesList = new ArrayList<>(Arrays.asList(LocalDataStore.EXPLAINIMAGES));

        public ExplainAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout._a0_item_explain_wizard, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.tvHeader.setText(headerList.get(position));
            holder.tvInfo.setText(infoList.get(position));
            holder.ivImage.setImageResource(imagesList.get(position));
        }

        @Override
        public int getItemCount() {
            return headerList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            AppCompatTextView tvHeader;
            TextView tvInfo;
            ImageView ivImage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvHeader = itemView.findViewById(R.id.tvHeader);
                tvInfo = itemView.findViewById(R.id.tvInfo);
                ivImage = itemView.findViewById(R.id.ivImage);
            }
        }
    }

    public boolean hasPreview() {
        return getCurrentItem() > 0;
    }

    public boolean hasNext() {
        return mRecyclerView.getAdapter() != null &&
                getCurrentItem() < (mRecyclerView.getAdapter().getItemCount()- 1);
    }

    public void preview() {
        int position = getCurrentItem();
        if (position > 0)
            setCurrentItem(position -1, true);
    }

    public void next() {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null)
            return;

        int position = getCurrentItem();
        int count = adapter.getItemCount();
        if (position < (count -1))
            setCurrentItem(position + 1, true);
    }

    private int getCurrentItem(){
        return ((LinearLayoutManager)mRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
    }

    private void setCurrentItem(int position, boolean smooth){
        if (smooth)
            mRecyclerView.smoothScrollToPosition(position);
        else
            mRecyclerView.scrollToPosition(position);
    }

}
