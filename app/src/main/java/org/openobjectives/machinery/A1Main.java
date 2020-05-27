/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.openobjectives.machinery.ui.AdapterListMainMenu;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.LocalDataStore;
import org.openobjectives.machinery.ui.MainMenuItem;
import org.openobjectives.machinery.service.NetworkService;
import org.openobjectives.machinery.model.Unit;

import java.util.ArrayList;
import java.util.List;
/**
 * <B>Class: A1Main </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The MainMenu for cluster selection <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A1Main extends AppCompatActivity {
    private static final String TAG = A1Main.class.getSimpleName();
    private View mParent;
    private AdapterListMainMenu mAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddButton;
    private A1Main mActual;
    private DbHelper mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a1_main);
        mParent = findViewById(android.R.id.content);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mActual = this;
		mDb = new DbHelper(this);
		if(!mDb.getPrefBool("termsAccepted")){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A1Main.this, A0ExplainWizard.class
                    .getName());
            startActivity(intent);
        }
        initToolbar();
        //if Service is still running destroy
        Intent intent = new Intent(A1Main.this, NetworkService.class);
        stopService(intent);
        initUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        //THEME
        String theme = mDb.getPrefString("theme");
        ImageView toolbarLogo = (ImageView) findViewById(R.id.image);
        if (theme.equals(LocalDataStore.THEME_STANDARD)) {
            toolbarLogo.setImageResource(R.drawable.oo_web_hi_res_512);
        } else if (theme.equals(LocalDataStore.THEME_COMPUTE_NOIR)) {
            toolbarLogo.setImageResource(R.drawable.unitlogo7);
        } else if (theme.equals(LocalDataStore.THEME_GRADIENT)) {
            toolbarLogo.setImageResource(R.drawable.gradient_oo4);
        }
        Intent intent = new Intent(A1Main.this, NetworkService.class);
        stopService(intent);
        if(!mDb.getPrefBool("termsAccepted")){
            Intent intentTerms = new Intent(Intent.ACTION_VIEW);
            intentTerms.setClassName(A1Main.this, A0ExplainWizard.class
                    .getName());
            startActivity(intentTerms);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        final Drawable overflow = getResources().getDrawable(R.drawable.oo_focusfield_horizental);
        toolbar.setOverflowIcon(overflow);

    }

    private void initUi() {
        //THEME
        String theme = mDb.getPrefString("theme");
        ImageView toolbarLogo = (ImageView) findViewById(R.id.image);
        ImageView ooLogo = (ImageView) findViewById(R.id.oologo);
        if (theme.equals(LocalDataStore.THEME_STANDARD)) {
            toolbarLogo.setImageResource(R.drawable.oo_web_hi_res_512);
            ooLogo.setImageResource(android.R.color.transparent);
        } else if (theme.equals(LocalDataStore.THEME_COMPUTE_NOIR)) {
            toolbarLogo.setImageResource(R.drawable.unitlogo7);
        } else if (theme.equals(LocalDataStore.THEME_GRADIENT)) {
            toolbarLogo.setImageResource(R.drawable.gradient_oo4);
        }
        TextView appnametxt = (TextView) findViewById(R.id.appnametxt);
        Typeface tf = ResourcesCompat.getFont(this, R.font.montserrat_bold);
        appnametxt.setTypeface(tf);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        //TODO Refactor with real Model using "real" mUnitId NOT position
        List<MainMenuItem> items = new ArrayList<>();
        items.add(new MainMenuItem(""));  // add empty one
        for (Unit u: mDb.fetchAllUnits()) {
            items.add(new MainMenuItem(u.getName() == null ? "Cluster " + u.getId() : u.getName(), u.getServers().get(0).getHostname(), R.drawable.oo_cluster, true));
        }
        //adjust arcbackground smaller if many clusters defined
        if (items.size() > 2) {
            float dpCalculation = getResources().getDisplayMetrics().density;
            toolbarLogo.getLayoutParams().height = (int) (300 * dpCalculation);
        }
        mAdapter = new AdapterListMainMenu(this, items);
        mRecyclerView.setAdapter(mAdapter);
        mAddButton = (FloatingActionButton) findViewById(R.id.a1_add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(A1Main.this, A2AddUnitFormActivity.class
                            .getName());
                    startActivity(intent);
            }
        });
        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListMainMenu.OnItemClickListener() {
            @Override
            public void onItemClick(View view, MainMenuItem obj, int position) {
                Snackbar.make(mParent, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
                Intent intent=null;
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent
                            .setClassName(A1Main.this, A3MenuResourcesActivity.class
                                    .getName());
                    // minus 1 cause we have manually added the first "Sections" + one
                    int unitId = (int) mDb.fetchAllUnits().get((int) position - 1).getId();
                    //TODO fix --- +1
                    intent.putExtra("mUnitId", unitId);
                    intent.putExtra("mResource", LocalDataStore.RES_POD);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, LocalDataStore.A0_HELP, 0, getResources().getString(R.string.HELP)).setIcon(R.drawable.shape_roundedbutton_accent);
        menu.add(0, LocalDataStore.A0_SETTINGS, 0, getResources().getString(R.string.Settings)).setIcon(R.drawable.shape_roundedbutton_accent);
        menu.add(0, LocalDataStore.A0_IMEXPORT, 0, getResources().getString(R.string.IMEXPORT)).setIcon(R.drawable.shape_roundedbutton_accent);
        if (mDb.fetchAllUnits().size() > 0) {
            menu.add(0, LocalDataStore.A0_DELETE_UNIT, 0, getResources().getString(
                    R.string.DELETE_UNIT)).setIcon(R.drawable.shape_roundedbutton_accent);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case LocalDataStore.A0_HELP:
                AlertDialog.Builder dialog = new AlertDialog.Builder(mActual);
                dialog.setTitle(getResources().getString(R.string.help));
                dialog.setMessage(getResources().getString(R.string.helptxt));
                dialog.setNegativeButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                            }
                        });
                dialog.show();
                return true;
            case LocalDataStore.A0_IMEXPORT:
                Intent intentIMEX = new Intent(Intent.ACTION_VIEW);
                intentIMEX.setClassName(A1Main.this, A2ImExportActivity.class
                        .getName());
                intentIMEX.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentIMEX);
                return true;
            case LocalDataStore.A0_SETTINGS:
                Intent intentSettings = new Intent(Intent.ACTION_VIEW);
                intentSettings.setClassName(A1Main.this, A7SettingsActivity.class
                        .getName());
                intentSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSettings);
                return true;
            case LocalDataStore.A0_DELETE_UNIT:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A1Main.this,
                        A2DeleteUnitFormActivity.class.getName());
                startActivity(intent);
                return true;
        }
        return false;
    }
}
