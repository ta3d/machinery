/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.openobjectives.machinery.model.Command;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.LocalDataStore;
import org.openobjectives.machinery.model.Server;
import org.openobjectives.machinery.model.Unit;

import java.util.ArrayList;
/**
 * <B>Class: A2AddUnitFormActivity </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Adds a new cluster to MainMenu <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class A2AddUnitFormActivity extends AppCompatActivity {

    private static final String TAG = A2AddUnitFormActivity.class.getSimpleName();
    private Spinner mSpinner;
    private CheckBox mChkBoxDirectConView;
    private CheckBox mChkBoxKubeconfigView;
	private int mActualAuthSpinnerPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._a2_addunit);
        initToolbar();
        initUi();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initUi() {
        //fill the auth mode Spinner
		ArrayList<String> slist = new ArrayList<String>();
		slist.add("  "+getResources().getString(R.string.Password));
		slist.add("  "+getResources().getString(R.string.Key));
		ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, slist);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner = (Spinner) findViewById(R.id.spinner);
		mSpinner.setOnItemSelectedListener(sSpinner);
		mActualAuthSpinnerPosition = mSpinner.getSelectedItemPosition();
		mSpinner.setAdapter(adapter);
        (findViewById(R.id.fab_done)).setOnClickListener(bSave);
        //(findViewById(R.id.home)).setOnClickListener(bCancel);
        mChkBoxDirectConView = (CheckBox) findViewById(R.id.directConnection);
        mChkBoxDirectConView.setOnCheckedChangeListener(bDirectConChecked);
        mChkBoxKubeconfigView = (CheckBox) findViewById(R.id.useKubeconfig);
        mChkBoxKubeconfigView.setOnCheckedChangeListener(bKubeconfigChecked);

        EditText editKubeconfig = (EditText) findViewById(R.id.editKubeconfig);
        editKubeconfig.setText(Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/");
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

    public final Spinner.OnItemSelectedListener sSpinner = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			if (position != mActualAuthSpinnerPosition) {
				EditText editCredential = (EditText) findViewById(R.id.editCredential);
				if (position == 0){
					editCredential.setText("");
					editCredential.setTransformationMethod(new PasswordTransformationMethod());
					mActualAuthSpinnerPosition = position;

				}
				else{
					editCredential.setTransformationMethod(null);
					editCredential.setText(Environment.getExternalStorageDirectory()+ "/"+Environment.DIRECTORY_DOWNLOADS+ "/");
					mActualAuthSpinnerPosition = position;
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};


    public final Button.OnClickListener bSave = new Button.OnClickListener() {
        public void onClick(View v) {
            EditText editServerName = (EditText) findViewById(R.id.editServerName);
            EditText editStatusServerUrl = (EditText) findViewById(R.id.editStatusServerUrl);
            EditText editStatusServerPort = (EditText) findViewById(R.id.editStatusServerPort);
            EditText editUser = (EditText) findViewById(R.id.editUser);
            EditText editCredential = (EditText) findViewById(R.id.editCredential);
            EditText editKubeconfig = (EditText) findViewById(R.id.editKubeconfig);
            String sServerName = editServerName.getText().toString().trim();
            String sStatusServerUrl = editStatusServerUrl.getText().toString().trim();
            String sStatusServerPort = editStatusServerPort.getText().toString().trim();
            String sUser = editUser.getText().toString().trim();
            String sCredential = editCredential.getText().toString();
            // the API Server
            //EditText aeditServerName = (EditText) findViewById(R.id.editApiServerName);
            EditText aeditStatusServerUrl = (EditText) findViewById(R.id.editApiServerUrl);
            EditText aeditStatusServerPort = (EditText) findViewById(R.id.editApiServerPort);
            EditText aeditUser = (EditText) findViewById(R.id.editApiUser);
            EditText aeditCredential = (EditText) findViewById(R.id.editApiCredential);
            //String aServerName = aeditServerName.getText().toString().trim();
            String aStatusServerUrl = aeditStatusServerUrl.getText().toString().trim();
            String aStatusServerPort = aeditStatusServerPort.getText().toString().trim();
            String aUser = aeditUser.getText().toString().trim();
            String aCredential = aeditCredential.getText().toString();
            String aKubeconfig = editKubeconfig.getText().toString();
            Unit u = new Unit();
            ArrayList<Command> commands = new ArrayList<Command>();
            ArrayList<Server> servers = new ArrayList<Server>();

            if (!mChkBoxDirectConView.isChecked() && sServerName.length() > 0 && sStatusServerUrl.length() > 0
                    && sStatusServerPort.length() > 0 && sUser.length() > 0
                    && sCredential.length() > 0 &&
                    ((aStatusServerUrl.length() > 0
                            && aStatusServerPort.length() > 0 && aUser.length() > 0 && aStatusServerUrl.startsWith("http"))
                            || (aKubeconfig.length() > 0)
                    )
                    ) {
                    //TODO && aCredential.length() > 0) {
                Server s;
                try {
                    s = GeneralHelper.setupServerProperties(
                            sStatusServerPort,
                            getSharedPreferences("secret", 0),
                            mSpinner,
                            sCredential,
                            sStatusServerUrl,
                            sUser,
                            null);
                } catch (NumberFormatException e) {
                    Toast.makeText(getBaseContext(),
                            getResources().getText(R.string.correctPort),
                            Toast.LENGTH_LONG).show();
                    return;
                } catch (Exception e) {
                    //Log.e(TAG, "Encrypt of Key went wrong");
                    Toast.makeText(getBaseContext(),
                            getResources().getText(R.string.filelocation),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Command c = new Command(getResources().getText(R.string.status)
                        .toString(), LocalDataStore.STATUSCOMMAND, s);

                s.setName(LocalDataStore.SSHSERVER);
                servers.add(s);
                commands.add(c);
                u.setServers(servers);
                u.setCommands(commands);
                u.setName(sServerName);

//moved up
//                if (aServerName.length() > 0 && aStatusServerUrl.length() > 0
//                        && aStatusServerPort.length() > 0 && aUser.length() > 0
//                        && aCredential.length() > 0) {
                Server s2 = new Server();
                if (!mChkBoxKubeconfigView.isChecked()) {
                    try {
                        s2 = GeneralHelper.setupServerProperties(
                                aStatusServerPort,
                                getSharedPreferences("secret", 0),
                                //a hack to reuse exsitng logic set mSpinner default password
                                new AppCompatSpinner(getApplicationContext(), 0) {{
                                    setSelection(0);
                                }},
                                aCredential,
                                aStatusServerUrl,
                                aUser,
                                null);
                        s2.setName(LocalDataStore.APISERVER);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getBaseContext(),
                                getResources().getText(R.string.correctPort),
                                Toast.LENGTH_LONG).show();
                        return;
                    } catch (Exception e) {
                        //Log.e(TAG, "Encrypt went wrong");
                        Toast.makeText(getBaseContext(),
                                e.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    try {
                        s2 = GeneralHelper.setupServerProperties(
                                getSharedPreferences("secret", 0),
                                aKubeconfig,
                                null,
                                getApplicationContext().getAssets());
                        s2.setName(LocalDataStore.KUBECONFIG);
                    } catch (Exception e) {
                        //Log.e(TAG, "Reading kubeconfig failed. please give correct path.");
                        Toast.makeText(getBaseContext(),
                                e.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Command c2 = new Command(getResources().getText(R.string.status)
                        .toString(), LocalDataStore.STATUSCOMMAND, s2);
                servers.add(s2);
                commands.add(c2);
                u.setServers(servers);
                u.setCommands(commands);

                DbHelper db = new DbHelper(getApplicationContext());
                db.createUnit(u);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(A2AddUnitFormActivity.this,
                        A1Main.class.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (mChkBoxDirectConView.isChecked() && sServerName.length() > 0 &&
                    ((aStatusServerUrl.length() > 0
                            && aStatusServerPort.length() > 0 && aUser.length() > 0 && aStatusServerUrl.startsWith("http"))
                            || (aKubeconfig.length() > 0))
                    ) {
                        //TODO && aCredential.length() > 0) {
                Server s2 = new Server();
                if (!mChkBoxKubeconfigView.isChecked()) {
                    try {
                        s2 = GeneralHelper.setupServerProperties(
                                aStatusServerPort,
                                getSharedPreferences("secret", 0),
                                //to reuse exsitng logic set mSpinner default password
                                new AppCompatSpinner(getApplicationContext(), 0) {{
                                    setSelection(0);
                                }},
                                aCredential,
                                aStatusServerUrl,
                                aUser,
                                null);
                        s2.setName(LocalDataStore.APISERVER);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getBaseContext(),
                                getResources().getText(R.string.correctPort),
                                Toast.LENGTH_LONG).show();
                        return;
                    } catch (Exception e) {
                        //Log.e("ADDAPISERVER", "Encrypt went wrong");
                        Toast.makeText(getBaseContext(),
                                e.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    try {
                        s2 = GeneralHelper.setupServerProperties(
                                getSharedPreferences("secret", 0),
                                aKubeconfig,
                                null,
                                getApplicationContext().getAssets());
                        s2.setName(LocalDataStore.KUBECONFIG);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getBaseContext(),
                                getResources().getText(R.string.correctPort),
                                Toast.LENGTH_LONG).show();
                        return;
                    } catch (Exception e) {
                        //Log.e("ADDKUBECFG", "Reading kubeconfig failed. please give correct path.");
                        Toast.makeText(getBaseContext(),
                                e.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
            }
            Command c2 = new Command(getResources().getText(R.string.status)
                    .toString(), LocalDataStore.STATUSCOMMAND, s2);
            servers.add(s2);
            commands.add(c2);
            u.setServers(servers);
            u.setCommands(commands);
                u.setName(sServerName);

            DbHelper db = new DbHelper(getApplicationContext());
            db.createUnit(u);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A2AddUnitFormActivity.this,
                    A1Main.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            }
            else {
                Toast.makeText(getBaseContext(),
                        getResources().getText(R.string.fillall),
                        Toast.LENGTH_LONG).show();
            }

        }
    };
    public final Button.OnClickListener bCancel = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(A2AddUnitFormActivity.this, A1Main.class
                    .getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    public final CompoundButton.OnCheckedChangeListener bDirectConChecked = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             LinearLayout jmpText = (LinearLayout) findViewById(R.id.jumpserverText);
             CardView jmpInput = (CardView) findViewById(R.id.jumpserverInput);
            if (buttonView.isChecked()) {
                jmpText.setVisibility(View.GONE);
                jmpInput.setVisibility(View.GONE);
            }
            else
            {
                jmpText.setVisibility(View.VISIBLE);
                jmpInput.setVisibility(View.VISIBLE);
            }
        }

    };

    public final CompoundButton.OnCheckedChangeListener bKubeconfigChecked = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            LinearLayout kubecfgText = (LinearLayout) findViewById(R.id.usekubeconfigText);
            CardView kubecfgInput = (CardView) findViewById(R.id.unitKubeconfig);
            LinearLayout apiserverText = (LinearLayout) findViewById(R.id.apiserverText);
            CardView apiserverInput = (CardView) findViewById(R.id.unitApiServer);
            if (buttonView.isChecked()) {
                kubecfgText.setVisibility(View.VISIBLE);
                kubecfgInput.setVisibility(View.VISIBLE);
                apiserverText.setVisibility(View.GONE);
                apiserverInput.setVisibility(View.GONE);
            } else {
                kubecfgText.setVisibility(View.GONE);
                kubecfgInput.setVisibility(View.GONE);
                apiserverText.setVisibility(View.VISIBLE);
                apiserverInput.setVisibility(View.VISIBLE);
            }
        }

    };

}