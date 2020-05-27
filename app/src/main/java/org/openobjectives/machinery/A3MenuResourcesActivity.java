/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.openobjectives.machinery.ui.AdapterDeleteItem;
import org.openobjectives.machinery.ui.DeleteHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
/**
 * <B>Class: A3MenuResourcesActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The concrete Resource list menu <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A3MenuResourcesActivity extends A3MenuActivity {

    private static final String TAG = A3MenuResourcesActivity.class.getSimpleName();
    private ActionBar mActionBar;
    private Toolbar mToolbar;
    private View mParent;
    private RecyclerView mRecyclerView;
    private AdapterDeleteItem mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ArrayList<HasMetadata> mList;
    private View mPreviousView = null;
    private final static int LOADINGTIME = 1500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a3_menu_resources);
        mParent = findViewById(android.R.id.content);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        initToolbar();
        initNavigationMenu();
        initContent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initContent() {
        LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view_progress);
        animationView.setVisibility(View.VISIBLE);
        animationView.loop(true);
        animationView.playAnimation();
            mRecyclerView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mService != null && mService.serverOnline) {
                        mList = mService.getResources(mResource, mResourceIdent);
                        GeneralHelper.fadeOut(animationView);
                        if(mList !=null && mList.size()<1){
                            Toast.makeText(getApplicationContext(), "No Resources for " + mResourceIdent, Toast.LENGTH_LONG).show();
                            Intent intent = null;
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setClassName(A3MenuResourcesActivity.this, A3MenuResourcesActivity.class
                                    .getName());
                            intent.putExtra("mNamespace", mNamespace);
                            intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                            intent.putExtra("mUnitId", mUnitId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else if(mList ==null){
                            Toast.makeText(getApplicationContext(), "Connection Problem "+mService.actualErrorMessage, Toast.LENGTH_LONG).show();
                        }else {
                            initUi(mList);
                        }
                    }else {
                        //Log.e(TAG, "not connected" );
                    }
                }
            }, LOADINGTIME);
    }

    private void initUi(ArrayList<HasMetadata> list) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        //set data and mList adapter
        mAdapter = new AdapterDeleteItem(this, list, mDb, mService, mResource, mParent);
        mAdapter.selectedNamespace= mNamespace;
        mRecyclerView.setAdapter(mAdapter);

        //dont't delete namespaces or resources
        if(!mResource.equals(LocalDataStore.RES_NAMESPACE) && !mResource.equals(LocalDataStore.RES_RESOURCES)) {
            ItemTouchHelper.Callback callback = new DeleteHelper(mAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }

        // on item mList clicked
        mAdapter.setOnItemClickListener(new AdapterDeleteItem.OnItemClickListener() {
            @Override
            public void onItemClick(View view, HasMetadata obj, int position) {
                if (mResource.equals(LocalDataStore.RES_NAMESPACE)) {
                    if (mPreviousView != null && mPreviousView.equals(view)) {
                        view.setBackgroundColor(Color.WHITE);
                        mNamespace = "any";
                        mPreviousView = null;
                    } else if (mNamespace.equals(obj.getMetadata().getName())) {
                        view.setBackgroundColor(Color.WHITE);
                        mNamespace = "any";
                        mPreviousView = null;
                    } else if (mPreviousView != null && !mPreviousView.equals(view)) {
                        mPreviousView.setBackgroundColor(Color.WHITE);
                        view.setBackgroundColor(Color.GRAY);
                        mPreviousView = view;
                        Snackbar.make(mParent, "Namespace " + obj.getMetadata().getName() + " selected", Snackbar.LENGTH_SHORT).show();
                        mNamespace = obj.getMetadata().getName();
                    } else {
                        view.setBackgroundColor(Color.GRAY);
                        mPreviousView = view;
                        Snackbar.make(mParent, "Namespace " + obj.getMetadata().getName() + " selected", Snackbar.LENGTH_SHORT).show();
                        mNamespace = obj.getMetadata().getName();
                    }
                    mNamespaceView.setText("Namespace: " + mNamespace);
                    mAdapter.selectedNamespace = mNamespace;
                    mRecyclerView.setAdapter(null);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.getAdapter().notifyDataSetChanged();


                } else if (mResource.equals(LocalDataStore.RES_POD)) {
                    Pod actual = (Pod) obj;
                    List<String> containers=null;
                    if (actual.getSpec().getContainers().size()>=1){
                        containers = actual.getSpec().getContainers().stream()
                        .filter(item -> item!=null)
                        .map(p -> new String(p.getName()))
                        .collect(Collectors.toList());
                    }
                    //TODO FIX to UTC
                    //Log.e(TAG, actual.getStatus().getStartTime());
                    //Log.e(TAG, new LocalDateTime().toDateTime(DateTimeZone.UTC).toString());
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuResourcesActivity.this, A4SingleLogActivity.class
                            .getName());
                    intent.putExtra("mPodname", obj.getMetadata().getName());
                    intent.putExtra("mPoduptime", GeneralHelper.getTimeDistanceShortISO8601(actual.getStatus().getStartTime(), new LocalDateTime().toDateTime(DateTimeZone.UTC)));
                    intent.putExtra("mPodstatus", actual.getStatus().getPhase());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mInNamespace", obj.getMetadata().getNamespace());
                    intent.putExtra("mResource", LocalDataStore.RES_POD);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.putExtra("mSinceTime", 0);
                    intent.putExtra("mSelectedContainer", containers.get(0));
                    intent.putStringArrayListExtra("mContainers", (ArrayList<String>)containers);
                    startActivity(intent);
                } else if (mResource.equals(LocalDataStore.RES_SERVICE)) {
                    Service actual = (Service) obj;
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuResourcesActivity.this, A5PrintResourceActivity.class
                            .getName());
                    intent.putExtra("mResourcename", obj.getMetadata().getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_SERVICE);
                    intent.putExtra("mUnitId", mUnitId);
                    startActivity(intent);
                } else if (mResource.equals(LocalDataStore.RES_DEPLOYMENT)) {
                    Deployment actual = (Deployment) obj;
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuResourcesActivity.this, A5PrintResourceActivity.class
                            .getName());
                    intent.putExtra("mResourcename", obj.getMetadata().getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_DEPLOYMENT);
                    intent.putExtra("mUnitId", mUnitId);
                    startActivity(intent);
                } else if (mResource.equals(LocalDataStore.RES_RESOURCES)) {
                    HasMetadata actual = obj;
                    Intent intent = null;
                    //check 1st or 2nd level
                    if(mResourceIdent ==null) {
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClassName(A3MenuResourcesActivity.this, A3MenuResourcesActivity.class
                                .getName());
                        intent.putExtra("mNamespace", mNamespace);
                        intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                        intent.putExtra("mUnitId", mUnitId);
                        intent.putExtra("mResourceIdent", obj.getMetadata().getName());
                        intent.putExtra("mConcreteResource", mConcreteResource);
                        startActivity(intent);
                    }else{
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClassName(A3MenuResourcesActivity.this, A5PrintResourceActivity.class
                                .getName());
                        intent.putExtra("mResourcename", obj.getMetadata().getName());
                        intent.putExtra("mNamespace", mNamespace);
                        intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                        intent.putExtra("mConcreteResource", obj.getMetadata().getName());
                        intent.putExtra("mUnitId", mUnitId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                }
            }
        }
        );

    }

}
