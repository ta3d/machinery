/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.utils;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.openobjectives.androidlib.db.AbstractDb4oHelper;
import org.openobjectives.machinery.model.Server;
import org.openobjectives.machinery.model.Unit;
import org.openobjectives.machinery.model.Command;

import com.trilead.ssh2.crypto.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * <B>Class: DbHelper </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The DB Connection <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class DbHelper extends AbstractDb4oHelper {

	private static final String TAG = DbHelper.class.getSimpleName();
	private static final String APP_VERSION = "1.1";
	UnitContainer units;
	Context context;

	public DbHelper(Context ctx) {
		super(ctx);
		setContext(ctx);
		init();
	}

	@Override
	public void init() {
		File dir = context.getDir("objects", Context.MODE_PRIVATE);
		File ver = new File(dir, "version.txt");
		boolean installed = ver.exists();
		String v = findVersion(APP_VERSION);
		if (!installed) {
			setPrefBool("termsAccepted", false);
			setPrefBool("allowSelfsigned", true);
			setPrefBool("shortenPodnames", false);
            setPrefString("theme", LocalDataStore.THEME_STANDARD);
			setPrefString("hintInLogFor", "ERROR,Exception,Fatal");
			setPrefInt("consoleLength", 50000);
			setPrefInt("terminalLength", 10000);
			SharedPreferences settings = context.getSharedPreferences("secret", 0);
			SharedPreferences.Editor editor = settings.edit();
			KeyGenerator keygen;
			try {
				keygen = KeyGenerator.getInstance("AES");
				// as we encode result into base64 we have to gen more bits
				keygen.init(128);
				SecretKey genAesKey = keygen.generateKey();
				editor.putString("key", new String(Base64.encode(genAesKey
						.getEncoded())));
				keygen.init(16 * 6);
				SecretKey genAesIV = keygen.generateKey();
				editor.putString("iv", new String(Base64.encode(genAesIV
						.getEncoded())));
				editor.commit();
			} catch (NoSuchAlgorithmException e) {
				//Log.e("DbHelper:init", e.getMessage());
				throw new RuntimeException("KEY GENERATION FAILED");
			}
			createFileName(UnitContainer.class.getName(), getContext())
					.delete();
			// createFileName(Disposable.class.getName(), ctx).delete();

			// delete the old stuff...
			File db4oDir = getContext().getDir("db4o", Context.MODE_PRIVATE);
			new File(db4oDir, UnitContainer.class.getName() + ".yap").delete();
			db4oDir.delete();
			writeVersion(APP_VERSION);
		}
		
		// load required objects
		if (null == (units = loadObject(UnitContainer.class))) {
			saveObject(units = new UnitContainer());
		}
		
		if(!APP_VERSION.equals(v)){
			//when a new field is introduced db4o keeps track of it and just adds in old yap files 
			//nothing to do for migration - we just null the new key field 
			if (v.equals("1.0") && APP_VERSION.equals("1.1")){
				for (Unit unit : this.fetchAllUnits()) {
					for (Server s : unit.getServers()){
						s.setKey(null);
					}
				}
			}
//			if (v.equals("1.1") && APP_VERSION.equals("1.2")){
//				for (Unit unit : this.fetchAllUnits()) {
//					for (Server s : unit.getServers()){
//						if(s.getKey()==null)
//							s.setKey("");
//						if(s.getPassword()==null)
//							s.setPassword("");
//					}
//				}
//			}
			writeVersion(APP_VERSION); 
		}


	}

	public long createUnit(Unit unit) {
		units.saveUnit(unit);
		saveObject(units);
		return unit.getId();
	}

	public boolean deleteUnit(long unitId) {
		boolean result = units.removeUnit(units.findUnit(unitId));

		if (result) {
			saveObject(units);
		}

		return result;
	}

	public List<Unit> fetchAllUnits() {
		return units.getAllUnits();
	}

	public Unit findUnitById(long unitId) {
		return units.findUnit(unitId);
	}
	
	/**
	 * @param commandName
	 * @return
	 */
	public Command findCommandByName(String commandName, Unit unit) {
		for (Command command : unit.getCommands()) {
			if (command.getName().equals(commandName))
				return command;
		}
		return null;
	}

	public boolean updateUnit(Unit u) {
		units.saveUnit(u);
		saveObject(units);
		return true;
	}

	public void deleteAllUnits() {
		units.clear();
		saveObject(units);
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}
	public UnitContainer getUnitContainer() {
		return units;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	public Float getPrefFloat(String key) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		return prefs.getFloat(key,0);
	}

	public void setPrefFloat(String key, Float value) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor prefseditor = prefs.edit();
		prefseditor.putFloat(key, value);;
		prefseditor.commit();
	}

	public int getPrefInt(String key) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		return prefs.getInt(key, 0);
	}

	public void setPrefInt(String key, int value) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor prefseditor = prefs.edit();
		prefseditor.putInt(key, value);
		;
		prefseditor.commit();
	}
	public boolean getPrefBool(String key) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		return prefs.getBoolean(key,false);
	}

	public void setPrefBool(String key, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor prefseditor = prefs.edit();
		prefseditor.putBoolean(key, value);
		prefseditor.commit();
	}

	public String getPrefString(String key) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		return prefs.getString(key, "ERROR");
	}

	public void setPrefString(String key, String value) {
		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor prefseditor = prefs.edit();
		prefseditor.putString(key, value);
		prefseditor.commit();
	}

}
