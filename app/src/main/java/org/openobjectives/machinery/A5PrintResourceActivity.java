/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jraska.console.Console;


import org.openobjectives.machinery.service.NetworkService;
/**
 * <B>Class: A5PrintResourceActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Prints the YAML for the concrete Resource <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A5PrintResourceActivity extends AppCompatActivity {

    private static final String TAG = A5PrintResourceActivity.class.getSimpleName();
    protected TextView mResourcenameView = null;
    protected NestedScrollView mNestedScrollView =null;
    protected NetworkService mService = null;
    protected boolean mServiceBound = false;
    protected String mResource = null;
    protected String mResourcename = null;
    protected String mConcreteResource = null;
    protected int mUnitId;
    protected String mNamespace =null;
    protected Handler mHandler = new Handler();
    protected Console mConsole = null;
    public boolean mDontscroll =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a5_print_resource);
        mUnitId = getIntent().getIntExtra("mUnitId", 0);
        mResource = getIntent().getStringExtra("mResource");
        mNamespace = getIntent().getStringExtra("mNamespace");
        mResourcename = getIntent().getStringExtra("mResourcename");
        mConcreteResource = getIntent().getStringExtra("mConcreteResource");
        initUi();
        initToolbar();
        Intent intent = new Intent(A5PrintResourceActivity.this, NetworkService.class);
        intent.putExtra("mUnitId", mUnitId);
        intent.putExtra("mNamespace", mNamespace);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        initSelectableConsole();
        Console.clear();
        Console.writeLine();


    }

    private void initSelectableConsole(){
        mConsole = (Console) findViewById(R.id.a5_console);
        TextView textView = mConsole.findViewById(R.id.console_text);
        //textView.setTextIsSelectable(true);
        textView.setTextIsSelectable(false);
        textView.measure(-1, -1);//you can specific other values.
        textView.setTextIsSelectable(true);
        Typeface tf = ResourcesCompat.getFont(this, R.font.jetbrainsmono_regular);
        textView.setTypeface(tf);


    }

    boolean hide = false;

    private void initUi() {
        mConsole = (Console) findViewById(R.id.a5_console);
        mNestedScrollView = ((NestedScrollView) findViewById(R.id.nested_scroll_view));
        ((NestedScrollView) findViewById(R.id.nested_scroll_view)).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY >= oldScrollY) { // down
                    if (hide) return;
                    mDontscroll =false;
                    hide = true;
                } else {
                    if (!hide) return;
                    mDontscroll =true;
                    hide = false;
                }
            }
        });
        mResourcenameView = (TextView) findViewById(R.id.resourcename);
        mResourcenameView.setText(mResourcename);

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
        newdrawable.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(newdrawable);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
            mService.describeResource(mNamespace, mResourcename, mNestedScrollView, mResource, mConcreteResource);


        }
    };

    @Override
    public void onDestroy() {

        if (mServiceBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}