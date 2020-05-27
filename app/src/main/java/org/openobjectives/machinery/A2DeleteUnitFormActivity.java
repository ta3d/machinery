/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.model.Unit;

import java.util.List;

/**
 * <B>Class: A2DeleteUnitFormActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Delete a Cluster from MainMenu <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A2DeleteUnitFormActivity extends AppCompatActivity {

	private static final String TAG = A2DeleteUnitFormActivity.class.getSimpleName();
	private List<Unit> mAllUnits;
	Activity mActual;
	RadioGroup mRgroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActual = this;
		setContentView(R.layout._a2_deleteunit);
		initToolbar();
		//int mUnitId = getIntent().getIntExtra("mUnitId", 0);
		DbHelper db = new DbHelper(this);
		mAllUnits = db.fetchAllUnits();
		mRgroup = (RadioGroup) findViewById(R.id.radiobuttons);
		for (int i = 0; i < mAllUnits.size(); i++) {
			Unit u = mAllUnits.get(i);
			RadioButton newRadioButton = new RadioButton(this);
			newRadioButton.setText(u.getName() == null ? "Cluster " + u.getId() : u.getName());
			newRadioButton.setId(i);
			LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
					RadioGroup.LayoutParams.WRAP_CONTENT,
					RadioGroup.LayoutParams.WRAP_CONTENT);
			mRgroup.addView(newRadioButton, i, layoutParams);
		}
		(findViewById(R.id.fab_done)).setOnClickListener(bDelete);

	}

	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Delete Unit");
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

	public final Button.OnClickListener bDelete = new Button.OnClickListener() {
		public void onClick(View v) {
				int id = mRgroup.getCheckedRadioButtonId();
				Unit selUnit;
				try {
					selUnit = mAllUnits.get(id);
				} catch (Exception e) {
					Toast.makeText(getBaseContext(),
							getResources().getText(R.string.selectOne),
							Toast.LENGTH_LONG).show();
					return;
				}
				Unit u;
				DbHelper db = new DbHelper(mActual);
				db.deleteUnit(selUnit.getId());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(A2DeleteUnitFormActivity.this,
						A1Main.class.getName());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
		}
	};
	public final Button.OnClickListener bCancel = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(A2DeleteUnitFormActivity.this,
					A1Main.class.getName());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	};
}