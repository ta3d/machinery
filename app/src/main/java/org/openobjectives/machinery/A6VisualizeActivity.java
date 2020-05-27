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
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jraska.console.Console;


import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;
import org.openobjectives.machinery.service.NetworkService;
import org.openobjectives.machinery.model.Unit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
/**
 * <B>Class: A6VisualizeActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A graph visualization of the cluster deployment structure <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A6VisualizeActivity extends AppCompatActivity {

    private static final String TAG = A6VisualizeActivity.class.getSimpleName();
    private FloatingActionButton mFab;
    private FloatingActionButton mFabOnlyErrors;
    protected TextView mToolbarmainView = null;
    protected TextView mToolbarsecondView = null;
    protected TextView mToolbarunder2View = null;
    protected NestedScrollView mNestedScrollView = null;
    protected NetworkService mService = null;
    protected boolean mServiceBound = false;
    protected String mResource = null;
    protected Unit mActualUnit = null;
    protected int mUnitId;
    protected String mNamespace = null;
    protected String mInNamespace = null;
    protected boolean mOnlyErrors = false;
    private static final int SELECTNAMESPACE = 0;
    private final static int LOADINGTIME = 1500;
    private HashMap<String, Integer> mNamespacemap = new HashMap<String, Integer>();
    private ArrayList<HasMetadata> mDeploymentList;
    protected WebView mWebview;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private DbHelper mDb;
    protected boolean mHide = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a6_visulize);
        mUnitId = getIntent().getIntExtra("mUnitId", 0);
        mResource = getIntent().getStringExtra("mResource");
        mNamespace = getIntent().getStringExtra("mNamespace");
        mInNamespace = getIntent().getStringExtra("mInNamespace");
        mOnlyErrors = getIntent().getBooleanExtra("mOnlyErrors", false);
        mDb = new DbHelper(getApplicationContext());
        mActualUnit = mDb.findUnitById(mUnitId);
        initUi();
        initToolbar();
        Intent intent = new Intent(A6VisualizeActivity.this, NetworkService.class);
        intent.putExtra("mUnitId", mUnitId);
        intent.putExtra("mNamespace", mNamespace);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Console.clear();
        initContent();
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


    }

    private void initUi() {
        mNestedScrollView = ((NestedScrollView) findViewById(R.id.nested_scroll_view));
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNestedScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = null;
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClassName(A6VisualizeActivity.this, A6VisualizeActivity.class
                                .getName());
                        intent.putExtra("mNamespace", mNamespace);
                        intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                        intent.putExtra("mUnitId", mUnitId);
                        intent.putExtra("mOnlyErrors", mOnlyErrors);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        });
        mFabOnlyErrors = (FloatingActionButton) findViewById(R.id.fabOnlyErrors);
        if (mOnlyErrors)
            mFabOnlyErrors.setImageResource(R.drawable.oo_link_variant_off);
        mFabOnlyErrors.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNestedScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = null;
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClassName(A6VisualizeActivity.this, A6VisualizeActivity.class
                                .getName());
                        intent.putExtra("mNamespace", mNamespace);
                        intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                        intent.putExtra("mUnitId", mUnitId);
                        intent.putExtra("mOnlyErrors", !mOnlyErrors);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        });
        mToolbarmainView = (TextView) findViewById(R.id.podname);
        mToolbarsecondView = (TextView) findViewById(R.id.podstatus);
        mToolbarunder2View = (TextView) findViewById(R.id.poduptime);
        mToolbarmainView.setText("Visualize");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
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
        final Drawable overflow = getResources().getDrawable(R.drawable.oo_focusfield_horizental);;
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



    private void initWebView() {
        mWebview = (WebView) findViewById(R.id.webview);
        final WebSettings ws = mWebview.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setPluginState(WebSettings.PluginState.ON);
        ws.setAllowFileAccess(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setBlockNetworkLoads(true);
        mWebview.setWebViewClient(new WebViewClient());
        mWebview.setWebChromeClient(new WebChromeClient());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //TODO check DP
        int width = size.x;
        int height = size.y;
        String basejs = readAssetFileAsString("echarts/index.html");
        //Log.i(TAG,"source=" + basejs);
        String finalhtml = generateGraph(basejs);
        //Log.i(TAG,"source=" + finalhtml);
        mWebview.loadDataWithBaseURL("file:///android_asset/echarts/", finalhtml, "text/html", "UTF-8", null);
    }

    private String generateGraph(String basejs) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ArrayNode namespaceNodes = mapper.createArrayNode();
        ArrayNode nodeNodes = mapper.createArrayNode();
        ArrayNode linkNodes = mapper.createArrayNode();
        int count = 0;
        for (HasMetadata hasMetadata : mDeploymentList) {
            count++;
            Deployment d = (Deployment) hasMetadata;
            JsonNode node = mapper.createObjectNode();
            int podcount = d.getStatus().getReplicas();
            int desiredcount = d.getSpec().getReplicas();
            Integer unavailableReplicaCount = d.getStatus().getUnavailableReplicas();
            String tmpname = "";
            if (unavailableReplicaCount != null && unavailableReplicaCount > 0)
                tmpname = d.getMetadata().getName() + " \\\n Replica mismatch";
            else
                tmpname = d.getMetadata().getName();
            if (mOnlyErrors && !tmpname.contains("Replica mismatch"))
                continue;
            ((ObjectNode) node).put("name", tmpname);
            ((ObjectNode) node).put("type", getNamespaceId(d));
            ((ObjectNode) node).put("replicas", podcount + "/" + desiredcount);
            nodeNodes.add(node);
            for (int i = 1; i <= podcount; i++) {
                node = mapper.createObjectNode();
                ((ObjectNode) node).put("name", d.getMetadata().getName() + "-" + i);
                ((ObjectNode) node).put("type", getNamespaceId(d));
                nodeNodes.add(node);
                JsonNode link = mapper.createObjectNode();
                ((ObjectNode) link).put("source", tmpname);
                ((ObjectNode) link).put("target", d.getMetadata().getName() + "-" + i);
//                if (podcount == desiredcount)
//                    ((ObjectNode) link).put("type", 1);
//                else
//                    ((ObjectNode) link).put("type", 6);
                linkNodes.add(link);
            }
        }
        for (String s : mNamespacemap.keySet()) {
            JsonNode node = mapper.createObjectNode();
            ((ObjectNode) node).put("name", s);
            namespaceNodes.add(node);
        }

        mToolbarmainView.setText(mDeploymentList.size() + " Deployments");
        mToolbarsecondView.setText(linkNodes.size() + " Pods");

        basejs = basejs.replace("#mynodesplaceholder", nodeNodes.toString());
        basejs = basejs.replace("#mylinksplaceholder", linkNodes.toString());
        basejs = basejs.replace("#mycategoriesplaceholder", namespaceNodes.toString());
        basejs = basejs.replace("#mynameplaceholder", "" + mActualUnit.getName());
        return basejs;
    }

    private int getNamespaceId(Deployment deployment) {
        String namesp = deployment.getMetadata().getNamespace();
        if (mNamespacemap.containsKey(namesp))
            return mNamespacemap.get(namesp);
        else {
            mNamespacemap.put(namesp, mNamespacemap.size());
            return mNamespacemap.get(namesp);
        }
    }

    private String readAssetFileAsString(String sourceHtmlLocation) {
        InputStream is;
        try {
            is = getApplicationContext().getAssets().open(sourceHtmlLocation);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String result = new String(buffer, "UTF-8");
            //Log.e(TAG,""+result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String readFileAsString(String filename) {
        //Log.i(TAG,"" + getApplicationContext().getDataDir().getPath());
        File f = new File(getApplicationContext().getDataDir() + "/" + filename);
        //Read text from file
        StringBuffer text = new StringBuffer();

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        String result = new String(text);
        //Log.i(TAG,"" + result);
        return result;

    }

    private boolean writeCacheFileAsString(String fcontent, String filename) {

        try {
            File file = new File(getApplicationContext().getDataDir().getAbsolutePath(), filename);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // TODO READY BUT DISABLED AS LONG AS CONSOLE BUFFER CAN'T BE Changed
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._a4_menu_single_log, menu);
        menu.add(0, SELECTNAMESPACE, 0, "Filter by namespace");
        return true;
    }


    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            mWebview.setScaleX(mScaleFactor);
            mWebview.setScaleY(mScaleFactor);
            return true;
        }
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
            //mService.tailPodLog(mInNamespace, mPodname, mNestedScrollView, mDontscroll, mSinceTime, mSelectedContainer);
        }
    };


    @Override
    public void onDestroy() {
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
        } else if (item.getItemId() == SELECTNAMESPACE) {
            finish();
            intent = null;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A6VisualizeActivity.this, A3MenuResourcesActivity.class
                    .getName());
            intent.putExtra("mNamespace", mNamespace);
            intent.putExtra("mResource", LocalDataStore.RES_NAMESPACE);
            intent.putExtra("mUnitId", mUnitId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else {
            finish();
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A6VisualizeActivity.this,
                    A6VisualizeActivity.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("mNamespace", mNamespace);
            intent.putExtra("mInNamespace", mInNamespace);
            intent.putExtra("mResource", LocalDataStore.RES_POD);
            intent.putExtra("mUnitId", mUnitId);
            startActivity(intent);
            return true;
        }
    }


    private void initContent() {
        LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view_progress);
        animationView.setVisibility(View.VISIBLE);
        animationView.loop(true);
        animationView.playAnimation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mService.serverOnline) {
                    mDeploymentList = mService.getResources(LocalDataStore.RES_DEPLOYMENT, null);
                    GeneralHelper.fadeOut(animationView);
                    if (mDeploymentList != null && mDeploymentList.size() < 1) {
                        Toast.makeText(getApplicationContext(), "No Resources found ", Toast.LENGTH_LONG).show();
                        Intent intent = null;
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClassName(A6VisualizeActivity.this, A3MenuResourcesActivity.class
                                .getName());
                        intent.putExtra("mNamespace", mNamespace);
                        intent.putExtra("mResource", LocalDataStore.RES_NAMESPACE);
                        intent.putExtra("mUnitId", mUnitId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (mDeploymentList == null) {
                        Toast.makeText(getApplicationContext(), "Connection Problem "+mService.actualErrorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        initWebView();
                    }
                } else {
                    //Log.e(TAG, "not connected");
                }
            }
        }, LOADINGTIME);

    }

}