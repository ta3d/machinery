/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.openobjectives.machinery.utils.GeneralHelper;

/**
 * <B>Class: A8HelpActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The Help Screens <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */

public class A8HelpActivity extends AppCompatActivity {

    private static final String TAG = A8HelpActivity.class.getSimpleName();
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
        setContentView(R.layout._a8_help);
        initToolbar();
        initUi();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Help");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initUi() {
        ((Button) findViewById(R.id.bt_show)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermServicesDialog();
            }
        });
        ((Button) findViewById(R.id.bt_rerun)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A8HelpActivity.this, A0ExplainWizard.class
                        .getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
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

    private void showTermServicesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(A8HelpActivity.this);
        dialog.setTitle(getResources().getString(R.string.terms));
        dialog.setMessage(GeneralHelper.readAssetFileAsString("privacypolicy.txt",getApplicationContext())+
                    GeneralHelper.readAssetFileAsString("eula.txt",getApplicationContext()));
        dialog.setNegativeButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface,
                                        int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.show();
    }
}