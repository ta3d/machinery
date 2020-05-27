/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jraska.console.Console;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;
import org.openobjectives.machinery.service.NetworkService;



import java.util.ArrayList;
/**
 * <B>Class: A4SingleLogActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The logging activity <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A4SingleLogActivity extends AppCompatActivity {

    private static final String TAG = A4SingleLogActivity.class.getSimpleName();
    private FloatingActionButton mFab;
    protected TextView mPodnameView = null;
    protected TextView mPoduptimeView = null;
    protected TextView mPodstatusView = null;
    protected NestedScrollView mNestedScrollView = null;
    protected NetworkService mService = null;
    protected boolean mServiceBound = false;
    protected String mResource = null;
    protected String mPodname = null;
    protected String mSelectedContainer = null;
    protected ArrayList<String> mContainers;
    protected String mPodstatus = null;
    protected String mPoduptime = null;
    protected int mUnitId;
    protected int mSinceTime;
    protected String mNamespace = null;
    protected String mInNamespace = null;
    protected Handler mHandler = new Handler();
    protected Console mConsole = null;
    public MutableBoolean mDontscroll = new MutableBoolean(false);
    private LinearLayout mConsoleLayout =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a4_single_log);
        mUnitId = getIntent().getIntExtra("mUnitId", 0);
        mResource = getIntent().getStringExtra("mResource");
        mNamespace = getIntent().getStringExtra("mNamespace");
        mInNamespace = getIntent().getStringExtra("mInNamespace");
        mPodname = getIntent().getStringExtra("mPodname");
        mPoduptime = getIntent().getStringExtra("mPoduptime");
        mPodstatus = getIntent().getStringExtra("mPodstatus");
        mSelectedContainer = getIntent().getStringExtra("mSelectedContainer");
        mContainers = getIntent().getStringArrayListExtra("mContainers");
        mSinceTime = getIntent().getIntExtra("mSinceTime", 0);
        initSelectableConsole();
        initUi();
        initToolbar();
        Intent intent = new Intent(A4SingleLogActivity.this, NetworkService.class);
        intent.putExtra("mUnitId", mUnitId);
        intent.putExtra("mNamespace", mNamespace);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Console.clear();
        mHandler.post(mUpdateTimeTask);

    }

    private void initSelectableConsole(){
        mConsole = (Console) findViewById(R.id.a4_single_logconsole);
        TextView textView = mConsole.findViewById(R.id.console_text);
        //textView.setTextIsSelectable(true);
        textView.setTextIsSelectable(false);
        textView.measure(-1, -1);//you can specific other values.
        textView.setTextIsSelectable(true);
        Typeface tf = ResourcesCompat.getFont(this, R.font.jetbrainsmono_regular);
        textView.setTypeface(tf);
        //TODO make Log copyable
        //mConsoleLayout=(LinearLayout)findViewById(R.id.a4_console_layout);
        //mConsoleLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

    }

    boolean hide = false;

    private void initUi() {
        mConsole = (Console) findViewById(R.id.a4_single_logconsole);
        mNestedScrollView = ((NestedScrollView) findViewById(R.id.nested_scroll_view));
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNestedScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mNestedScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        ((NestedScrollView) findViewById(R.id.nested_scroll_view)).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY >= oldScrollY) { // down
                    if (hide) return;
                    GeneralHelper.hideButton(mFab);
                    mDontscroll.setValue(false);
                    //TODO make Log copyable
                    //mConsoleLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    hide = true;
                } else {
                    if (!hide) return;
                    GeneralHelper.showButton(mFab);
                    mDontscroll.setValue(true);
                    //TODO make Log copyable
                    //mConsoleLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                    hide = false;
                }
            }
        });
        mPodnameView = (TextView) findViewById(R.id.podname);
        mPoduptimeView = (TextView) findViewById(R.id.podstatus);
        mPodstatusView = (TextView) findViewById(R.id.poduptime);
        mPodnameView.setText(mPodname);
        if(mContainers !=null || mContainers.size()>1){
            mPodnameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSidecarDialog();
            }
        });
        }
        mPoduptimeView.setText(mPoduptime);
        mPodstatusView.setText(mPodstatus);

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.oo_porthole_toolbar);
        Bitmap bitmap = ((BitmapDrawable) upArrow).getBitmap();
        int actionbarsize = getActionBarHeight();
        Drawable newdrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            newdrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 56, 56, true));
         else
             newdrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, actionbarsize, actionbarsize, true));
        //Primary Color
        newdrawable.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(newdrawable);
        final Drawable overflow = getResources().getDrawable(R.drawable.oo_focusfield_horizental);
        //overflow.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_ATOP);
        toolbar.setOverflowIcon(overflow);
    }

    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
                    true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }


// TODO READY BUT DISABLED AS LONG AS CONSOLE BUFFER CAN'T BE Changed
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._a4_menu_single_log, menu);
        menu.add(0, LocalDataStore.INFO, 0, "info");
        menu.add(0, LocalDataStore.TAIL, 0, "tail");
        menu.add(0, LocalDataStore.M1, 0, "1m");
        menu.add(0, LocalDataStore.M5, 0, "5m");
        menu.add(0, LocalDataStore.M30, 0, "30m");
        menu.add(0, LocalDataStore.M60, 0, "60m");
        return true;
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
            mService.tailPodLog(mInNamespace, mPodname, mNestedScrollView, mDontscroll, mSinceTime, mSelectedContainer);
        }
    };

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            updateData();
            mHandler.postDelayed(this, 100);
        }
    };

    private void updateData() {
        if (mService != null) {
            if (mService.serverOnline) {
                //NOK
                //fabConnect.setImageResource(R.drawable.oo_link_variant);
            } else {
                //NOK;
                //fabConnect.setImageResource(R.drawable.oo_link_variant_off);
            }

        } else {
//            if (fabConnect != null)
//                fabConnect.getDrawable().mutate().setTint(ContextCompat.getColor(this, R.color.colorAccentLight));
//            fabConnect.setImageResource(R.drawable.oo_link_variant_off);
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (mServiceBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }else if (item.getItemId() == LocalDataStore.INFO) {
                finish();
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A4SingleLogActivity.this, A5PrintResourceActivity.class
                        .getName());
                intent.putExtra("mResourcename", mPodname);
                intent.putExtra("mNamespace", mInNamespace);
                intent.putExtra("mResource", mResource);
                intent.putExtra("mUnitId", mUnitId);
                startActivity(intent);
                return true;
        }else {
            finish();
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A4SingleLogActivity.this,
                    A4SingleLogActivity.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("mPodname", mPodname);
            intent.putExtra("mPoduptime", mPoduptime);
            intent.putExtra("mPodstatus", mPodstatus);
            intent.putExtra("mNamespace", mNamespace);
            intent.putExtra("mInNamespace", mInNamespace);
            intent.putExtra("mResource", LocalDataStore.RES_POD);
            intent.putExtra("mUnitId", mUnitId);
            intent.putExtra("mSinceTime", item.getItemId());
            intent.putExtra("mSelectedContainer", mSelectedContainer);
            intent.putStringArrayListExtra("mContainers", mContainers);
            startActivity(intent);
            return true;
        }
    }



    private void showSidecarDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout._a4_dialog_container_select);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LinearLayout ll = dialog.findViewById(R.id.mac_action);
        for (String cname: mContainers) {
            AppCompatButton b = new AppCompatButton(getApplicationContext());
            b.setLayoutParams(lp);
            b.setText(cname);
            b.setBackgroundResource(R.drawable.shape_whitebutton_outline);
            b.setGravity(Gravity.CENTER);
            b.setTextColor(Color.WHITE);
            b.setTextAppearance(getApplicationContext(), R.style.Widget_AppCompat_Button_Borderless);
            b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A4SingleLogActivity.this, A4SingleLogActivity.class
                            .getName());
                    intent.putExtra("mPodname", mPodname);
                    intent.putExtra("mPoduptime", mPoduptime);
                    intent.putExtra("mPodstatus", mPodstatus);
                    intent.putExtra("mNamespace", mNamespace);
                    intent.putExtra("mInNamespace", mInNamespace);
                    intent.putExtra("mResource", LocalDataStore.RES_POD);
                    intent.putExtra("mUnitId", mUnitId);
                    intent.putExtra("mSinceTime", 0);
                    intent.putExtra("mSelectedContainer", cname);
                    intent.putStringArrayListExtra("mContainers", mContainers);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
            }
        });
        ll.addView(b,lp);
        }

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


}