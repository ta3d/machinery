/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.LocalDataStore;

import java.util.ArrayList;
/**
 * <B>Class: A7SettingsActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Edits the general settings <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A7SettingsActivity extends AppCompatActivity {

    private static final String TAG = A7SettingsActivity.class.getSimpleName();
    protected String mResource = null;
    protected String mResourceIdent = null;
    protected String mConcreteResource = null;
    protected int mUnitId;
    protected String mNamespace = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnitId = getIntent().getIntExtra("mUnitId", 0);
        mResource = getIntent().getStringExtra("mResource");
        mNamespace = getIntent().getStringExtra("mNamespace");
        mResourceIdent = getIntent().getStringExtra("mResourceIdent");
        mConcreteResource = getIntent().getStringExtra("mConcreteResource");
        if (mNamespace == null)
            mNamespace = "any";
        setContentView(R.layout._a7_settings);
        initToolbar();
        initUi();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initUi() {
        CheckBox allowSelfsigned = (CheckBox) findViewById(R.id.allowSelfsigned);
        CheckBox shortenPodnames = (CheckBox) findViewById(R.id.shortenPodnames);
        DbHelper db = new DbHelper(getApplicationContext());
        db.getPrefBool("test");
        boolean bAllowSelfsigned = db.getPrefBool("allowSelfsigned");
        boolean bshortenPodnames = db.getPrefBool("shortenPodnames");
        String theme = db.getPrefString("theme");
        int consoleLength = db.getPrefInt("consoleLength");
        int terminalLength = db.getPrefInt("terminalLength");
        String hintInLogFor = db.getPrefString("hintInLogFor");
        ArrayList<String> slist = new ArrayList<String>();
        slist.add(LocalDataStore.THEME_STANDARD);
        slist.add(LocalDataStore.THEME_COMPUTE_NOIR);
        slist.add(LocalDataStore.THEME_GRADIENT);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, slist);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(theme));
        //Disabled for performance
        EditText editconsoleLength = (EditText) findViewById(R.id.editconsoleLength);
        editconsoleLength.setFocusable(false);
        editconsoleLength.setEnabled(false);
        EditText editTerminalLength = (EditText) findViewById(R.id.editTerminalLength);
        editTerminalLength.setFocusable(false);
        editTerminalLength.setEnabled(false);
        EditText edithintInLogFor = (EditText) findViewById(R.id.edithintInLogFor);
        allowSelfsigned.setChecked(bAllowSelfsigned);
        shortenPodnames.setChecked(bshortenPodnames);
//        editconsoleLength.setText("" + consoleLength);
//        editTerminalLength.setText("" + terminalLength);
        editconsoleLength.setText("default");
        editTerminalLength.setText("default");
        edithintInLogFor.setText(hintInLogFor);
        (findViewById(R.id.fab_done)).setOnClickListener(bSave);

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
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    public final Button.OnClickListener bSave = new Button.OnClickListener() {
        public void onClick(View v) {
            CheckBox allowSelfsigned = (CheckBox) findViewById(R.id.allowSelfsigned);
            CheckBox shortenPodnames = (CheckBox) findViewById(R.id.shortenPodnames);
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            EditText editconsoleLength = (EditText) findViewById(R.id.editconsoleLength);
            EditText editTerminalLength = (EditText) findViewById(R.id.editTerminalLength);
            EditText edithintInLogFor = (EditText) findViewById(R.id.edithintInLogFor);
            String sHintInLogFor = edithintInLogFor.getText().toString().trim();
            //DISABLED
            //Integer iConsoleLength = Integer.parseInt(editconsoleLength.getText().toString().trim());
            //Integer iTerminalLength = Integer.parseInt(editTerminalLength.getText().toString().trim());
            //if (iConsoleLength != 0) {
                //TODO move to unit
                DbHelper db = new DbHelper(getApplicationContext());
                db.setPrefBool("allowSelfsigned", allowSelfsigned.isChecked());
                db.setPrefBool("shortenPodnames", shortenPodnames.isChecked());
                db.setPrefString("theme", spinner.getSelectedItem().toString());
                //mDb.setPrefInt("consoleLength", iConsoleLength);
                //mDb.setPrefInt("terminalLength", iTerminalLength);
                db.setPrefString("hintInLogFor", sHintInLogFor);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A7SettingsActivity.this, A1Main.class
                        .getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            //}
        }
    };


}