/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.openobjectives.machinery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.openobjectives.androidlib.crypto.MD5Crypter;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.model.Unit;

/**
 * <B>Class: A2ImExportActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Import or Export Cluster Configs <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */

public class A2ImExportActivity extends AppCompatActivity {

    private static final String TAG = A2ImExportActivity.class.getSimpleName();
    private Unit mActualUnit;
    private Activity mActual;
    private Spinner mSpinner;
    private int mActualAuthSpinnerPosition;
    private DbHelper mDb;

    private String sFilePath;
    private String s0Password1;
    private String s0Password2;
    private String sPassword1;
    private String sPassword2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActual = this;
        setContentView(R.layout._a2_importexport);
        initToolbar();
        mDb = new DbHelper(this);
        EditText editFilePath = (EditText) findViewById(R.id.editFilePath);
        editFilePath.setTransformationMethod(null);
        editFilePath.setText(Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/");
        (findViewById(R.id.fab_export)).setOnClickListener(bExport);
        ((FloatingActionButton) findViewById(R.id.fab_export)).setImageBitmap(textAsBitmap("EX", 40, Color.WHITE));
        (findViewById(R.id.fab_import)).setOnClickListener(bImport);
        ((FloatingActionButton) findViewById(R.id.fab_import)).setImageBitmap(textAsBitmap("IM", 40, Color.WHITE));
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Import/Export Cluster");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public final Button.OnClickListener bImport = new Button.OnClickListener() {
        public void onClick(View v) {
            collectInputs();
            if (sFilePath.length() > 0 && s0Password1.length() > 7 && s0Password2.length() > 7) {
                try {
                    GeneralHelper.importSettingsByFile(mActual, mDb.getUnitContainer(), sFilePath, sPassword1, sPassword2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(),
                            getResources().getText(R.string.Error) + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(),
                        getResources().getText(R.string.ImportSuccessful),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A2ImExportActivity.this,
                        A1Main.class.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {
                Toast.makeText(getBaseContext(),
                        getResources().getText(R.string.pass6digits),
                        Toast.LENGTH_LONG).show();
                return;
            }

        }
    };


    public final Button.OnClickListener bExport = new Button.OnClickListener() {
        public void onClick(View v) {
            collectInputs();
            if (sFilePath.length() > 0 && s0Password1.length() > 7 && s0Password2.length() > 7) {
                try {
                    GeneralHelper.exportSettingsByFile(mActual, mDb.getUnitContainer(), sFilePath, sPassword1, sPassword2);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(),
                            getResources().getText(R.string.Error) + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                Toast.makeText(getBaseContext(),
                        getResources().getText(R.string.pass6digits),
                        Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getBaseContext(),
                    getResources().getText(R.string.ExportSuccessful),
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A2ImExportActivity.this,
                    A1Main.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    public void collectInputs() {
        EditText editFilePath = (EditText) findViewById(R.id.editFilePath);
        EditText editPassword1 = (EditText) findViewById(R.id.editPassword1);
        EditText editPassword2 = (EditText) findViewById(R.id.editPassword2);
        sFilePath = editFilePath.getText().toString().trim();
        s0Password1 = editPassword1.getText().toString().trim();
        s0Password2 = editPassword2.getText().toString().trim();
        sPassword1 = MD5Crypter.getMd5Hash(editPassword1.getText().toString().trim());
        sPassword2 = MD5Crypter.getMd5Hash(editPassword2.getText().toString().trim());
        sPassword2 = new String(sPassword2.toCharArray(), 0, 16);
    }

    //method to convert your text to image
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

}