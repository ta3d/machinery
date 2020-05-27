/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.LocalDataStore;
import org.openobjectives.machinery.service.NetworkService;
/**
 * <B>Class: A3MenuActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The Base Activity for all Resources <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A3MenuActivity extends AppCompatActivity {

    private static final String TAG = A3MenuActivity.class.getSimpleName();
    protected ActionBar mActionBar;
    protected Toolbar mToolbar;
    protected NetworkService mService = null;
    protected boolean mServiceBound = false;
    protected TextView mConnectedView = null;
    protected TextView mNamespaceView = null;
    protected String mResource = null;
    protected String mResourceIdent = null;
    protected String mConcreteResource = null;
    protected int mUnitId;
    protected String mNamespace =null;
    protected DbHelper mDb;
    protected Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnitId = getIntent().getIntExtra("mUnitId", 0);
        mResource = getIntent().getStringExtra("mResource");
        mNamespace = getIntent().getStringExtra("mNamespace");
        mResourceIdent = getIntent().getStringExtra("mResourceIdent");
        mConcreteResource = getIntent().getStringExtra("mConcreteResource");
        if(mNamespace ==null)
            mNamespace ="any";
        Intent intent = new Intent(A3MenuActivity.this, NetworkService.class);
        intent.putExtra("mUnitId", mUnitId);
        intent.putExtra("mNamespace", mNamespace);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mHandler.post(mUpdateTimeTask);
        mDb = new DbHelper(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    protected void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbarColl);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        //set resourceident for 2nd level
        if(mResourceIdent ==null)
            mActionBar.setTitle(mResource);
        else
            mActionBar.setTitle(mResourceIdent);
    }


    protected void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.drawerOpen, R.string.drawerClose) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_ootoolbar);
        final Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_bold);
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);
        //Special Burger button activate
        mToolbar = (Toolbar) findViewById(R.id.toolbarColl);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        LottieAnimationView toolbarAni = (LottieAnimationView) findViewById(R.id.animation_toolbar);
        if (mResource.equals(LocalDataStore.RES_DEPLOYMENT))
            toolbarAni.setAnimation("ani_deployment.json");
        else if (mResource.equals(LocalDataStore.RES_SERVICE))
            toolbarAni.setAnimation("ani_service.json");
        else if (mResource.equals(LocalDataStore.RES_NAMESPACE))
            toolbarAni.setAnimation("ani_namespace.json");
        else if (mResource.equals(LocalDataStore.RES_POD))
            toolbarAni.setAnimation("ani_pod.json");
        else if (mResource.equals(LocalDataStore.RES_RESOURCES))
            toolbarAni.setAnimation("ani_resources.json");
        else
            toolbarAni.setAnimation("ani_resources.json");
        //THEME
        String theme = mDb.getPrefString("theme");
        ImageView toolbarLogo = (ImageView) findViewById(R.id.image);
        if (theme.equals(LocalDataStore.THEME_STANDARD)) {
                toolbarLogo.setImageResource(android.R.color.transparent);
        } else if (theme.equals(LocalDataStore.THEME_COMPUTE_NOIR)) {
            toolbarAni.setVisibility(View.GONE);
            if (mResource.equals(LocalDataStore.RES_DEPLOYMENT))
                toolbarLogo.setImageResource(R.drawable.unitlogo3);
            else if (mResource.equals(LocalDataStore.RES_SERVICE))
                toolbarLogo.setImageResource(R.drawable.unitlogo1);
            else if (mResource.equals(LocalDataStore.RES_NAMESPACE))
                toolbarLogo.setImageResource(R.drawable.unitlogo6);
            else if (mResource.equals(LocalDataStore.RES_POD))
                toolbarLogo.setImageResource(R.drawable.unitlogo8);
            else if (mResource.equals(LocalDataStore.RES_RESOURCES))
                toolbarLogo.setImageResource(R.drawable.unitlogo9);
            else
                toolbarLogo.setImageResource(R.drawable.unitlogo8);
        } else if (theme.equals(LocalDataStore.THEME_GRADIENT)) {
            toolbarLogo.setImageResource(R.drawable.gradient_oo4);
        }

        //OK rotate image
        ImageView navlogo = (ImageView) mToolbar.findViewById(R.id.logo);
        //NOK Lottie
        //LottieAnimationView navlogo = (LottieAnimationView) mToolbar.findViewById(R.id.animation_context);
        navlogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotateAnimation r = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                r.setDuration((long) 500);
                r.setRepeatCount(0);
                navlogo.startAnimation(r);
                //NOK Lottie
                //navlogo.setVisibility(View.VISIBLE);
                //navlogo.loop(true);
                //navlogo.playAnimation();
                drawer.openDrawer(nav_view);
            }
        });

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                if(item.getItemId()==R.id.nav_pods){
                    Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A3MenuResourcesActivity.class
                                    .getName());
                    // minus 1 cause we have manually added the first "Sections" + one
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_POD);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else if(item.getItemId()==R.id.nav_deployments){
                    Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A3MenuResourcesActivity.class
                                    .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_DEPLOYMENT);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if(item.getItemId()==R.id.nav_services){
                    Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A3MenuResourcesActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_SERVICE);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if(item.getItemId()==R.id.nav_namespaces){
                    Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A3MenuResourcesActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_NAMESPACE);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if(item.getItemId()==R.id.nav_other_resources){
                    Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A3MenuResourcesActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_visualize) {
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A6VisualizeActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.putExtra("mOnlyErrors", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_settings) {
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A7SettingsActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.putExtra("mOnlyErrors", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_help) {
                    Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A3MenuActivity.this, A8HelpActivity.class
                            .getName());
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.putExtra("mOnlyErrors", false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                mActionBar.setTitle(item.getTitle());
                drawer.closeDrawers();
                return true;
            }
        });
        View header = nav_view.getHeaderView(0);
        mConnectedView = (TextView) header.findViewById(R.id.connectedTextView);
        mNamespaceView = (TextView) header.findViewById(R.id.selectedNamespace);
        mNamespaceView.setText("Namespace: " + mNamespace);
        if (mNamespace.equals("any"))
            mNamespaceView.setVisibility(View.GONE);
    }

    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mServiceBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkBinder b = (NetworkService.NetworkBinder) service;
            mService = b.getService();
            mServiceBound = true;
        }
    };

    /**
     * Runnable
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            updateData();
            // Running this thread after 10 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private void updateData() {
        if(mService!=null) {
            if (mConnectedView != null) {
                //disabled due to eyecandy
                //mConnectedView.setText(mService.serverOnline?"connected":"no connection");
                mConnectedView.setText(mService.serverOnline ? "" : "Connection Error "+mService.actualErrorMessage);
            }
            if(mService.serverOnline){

            }else{

            }

        }else{
            if(mConnectedView !=null)
                mConnectedView.setText("NetworkService not bound");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (mServiceBound) {
            unbindService(mConnection);
        }
    }

}
