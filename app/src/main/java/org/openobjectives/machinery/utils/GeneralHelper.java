/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.joda.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.openobjectives.androidlib.lang.CloneUtil;
import org.openobjectives.machinery.R;
import org.openobjectives.machinery.model.Server;
import org.openobjectives.machinery.helper.StringCrypter;
import org.openobjectives.machinery.model.Unit;
import org.openobjectives.machinery.model.Command;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * <B>Class: GeneralHelper </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A helper class for static code needed in different places <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */

public class GeneralHelper {

	private static final String TAG = GeneralHelper.class.getSimpleName();
	/**
	 * To ensure linefeeds inbetween key tags (which trilead sshlibary needs)
	 * we have to reinsert them as base64encode removed them 
	 * @param sshkey
	 * @return
	 */
	public static String addLineFeedsToSshKey(String sshkey) {
		int actualPosition=0; 
		// we have 4 times the '-----' sequence in sshkey
		for(int i=1; i<4 && actualPosition<sshkey.length(); i++){
			actualPosition = sshkey.indexOf("-----", actualPosition);
			//we add a linefeed after key starttag and before endtag
			if (i==2)
				sshkey = new StringBuffer(sshkey).insert(actualPosition+5,"\n").toString();
			else if(i==3)
				sshkey = new StringBuffer(sshkey).insert(actualPosition,"\n").toString();
			actualPosition=actualPosition+5;
		}
		return sshkey;
	}

	public static Server setupServerProperties(SharedPreferences settings, String sKubecfgPath, Server existingServer, AssetManager as) throws Exception {

		String key = settings.getString("key", "");
		String iv = settings.getString("iv", "");
		String sKubeConfig = sKubecfgPath;
		//the case someone changes server auth from password to key
		if (existingServer != null && existingServer.getKey() == null) {
			File f = new File(sKubecfgPath);
			FileInputStream fileIS = new FileInputStream(f);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			StringBuilder keyBuilder = new StringBuilder();
			String line;
			while ((line = buf.readLine()) != null) {
				keyBuilder.append(line + "\r\n");
			}
			sKubeConfig = StringCrypter.encrypt(keyBuilder.toString(), key, iv);
		}
		//the case someone changes server with an already stored key
		//but doesn't update the key but something else
		else if (existingServer != null && !existingServer.getKey().equals("") && !sKubecfgPath.startsWith("/")) {
			sKubecfgPath = existingServer.getKey();
		} else {
			File f = new File(sKubecfgPath);
			FileInputStream fileIS = new FileInputStream(f);
			// for debugging from assets - now in emulator under /sdcard/Download/kubeconfig
			//InputStream fileIS = as.open("kubeconfig");
			BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			StringBuilder keyBuilder = new StringBuilder();
			String line;
			while ((line = buf.readLine()) != null) {
				keyBuilder.append(line + "\r\n");
			}
			sKubeConfig = StringCrypter.encrypt(keyBuilder.toString(), key, iv);
		}

		//Store in Key Kubecfg
		Server s = new Server("kubeconfig", 0, "kubeconfig", "dummy", sKubecfgPath, sKubeConfig);
		//Store Password
		return s;


	}

	/**
	 * Due to several add/edit server handling views we screw out server setup
	 * @param 
	 * @return
	 */
	public static Server setupServerProperties(String sStatusServerPort,
											   SharedPreferences settings,
											   Spinner spinner,
											   String sCredential,
											   String sStatusServerUrl,
											   String sUser,
											   Server existingServer) throws Exception
												
	{
		int port;
		try {
			port = Integer.parseInt(sStatusServerPort);
		} catch (NumberFormatException e) {
			throw e;
		}
		try {
			String key = settings.getString("key", "");
			String iv = settings.getString("iv", "");
			if(spinner.getSelectedItemPosition() == 0)
				sCredential = StringCrypter.encrypt(sCredential, key, iv);
			else{
				//the case someone changes server auth from password to key 
				if(existingServer!=null && existingServer.getKey()==null){
					File f = new File(sCredential);
			        FileInputStream fileIS = new FileInputStream(f);
			        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			        StringBuilder keyBuilder = new StringBuilder();
			        String line;
			        while((line = buf.readLine())!= null){
			           keyBuilder.append(line);
			        }
			        sCredential = StringCrypter.encrypt( keyBuilder.toString(), key, iv);
				}
				//the case someone changes server with an already stored key 
				//but doesn't update the key but something else
				else if(existingServer!=null && !existingServer.getKey().equals("") && !sCredential.startsWith("/")){
			        sCredential = existingServer.getKey();
				}else{
					File f = new File(sCredential);
			        FileInputStream fileIS = new FileInputStream(f);
			        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			        StringBuilder keyBuilder = new StringBuilder();
			        String line;
			        while((line = buf.readLine())!= null){
			           keyBuilder.append(line);
			        }
			        sCredential = StringCrypter.encrypt( keyBuilder.toString(), key, iv);
				}
		        
			}
		} catch (Exception e) {
			//Log.e(TAG,"Encrypt of Key went wrong");
			throw e;
		}
		Server s;
		
		if (existingServer==null){
			//Store Key
			if(spinner.getSelectedItemPosition() == 1)
				s = new Server(sStatusServerUrl, port, sStatusServerUrl
					+ ":" + port, sUser, null, sCredential);
			//Store Password
			else {
				s = new Server(sStatusServerUrl, port, sStatusServerUrl
					+ ":" + port, sUser, sCredential, null);
			}
			return s;
		}else{
			if(spinner.getSelectedItemPosition() == 1){
				existingServer.setHostname(sStatusServerUrl);
				existingServer.setPort(port);
				existingServer.setName(sStatusServerUrl	+ ":" + port);
				existingServer.setUser(sUser);
				existingServer.setPassword("");
				existingServer.setKey(sCredential);
			}
			else{
				existingServer.setHostname(sStatusServerUrl);
				existingServer.setPort(port);
				existingServer.setName(sStatusServerUrl	+ ":" + port);
				existingServer.setUser(sUser);
				existingServer.setPassword(sCredential);
				existingServer.setKey("");
			}
			return existingServer;
		}

	}

	
	public static int getIconDrawableIdByUnitId(Unit u) {
		int iconId= ((int)u.getId()%12) + 1;
		Class res = R.drawable.class;
        try {
			Field field = res.getField("unitlogo"+Integer.toString(iconId));
			return field.getInt(null);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return 1;
	}

	/**
	 *
	 * @throws Exception 
	 */
	public static void importSettingsByFile(Activity actual, UnitContainer savedContainer, String filePath, String filepassword1, String filepassword2) throws Exception {
		StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        in = new BufferedReader(new FileReader(new File(filePath)));
        while ((line = in.readLine()) != null) stringBuilder.append(line);
        in.close();
        SharedPreferences settings = actual.getSharedPreferences("secret", 0);
		String key = settings.getString("key", "");
		String iv = settings.getString("iv", "");
		String decryptedFile = StringCrypter.decrypt(stringBuilder.toString(), filepassword1, filepassword2);
		//Log.e(TAG,decryptedFile);
		Serializer serializer = new Persister();
		UnitContainer newUnits=null;
		newUnits = (UnitContainer)serializer.read(UnitContainer.class, decryptedFile);		
		for (Unit unit : newUnits.getAllUnits()) {
			for (Server server : unit.getServers()) {
					if (server.getKey()==null || server.getKey().equals("")){
						String cryptedPassword = StringCrypter.encrypt(server.getPassword(), key, iv);
						server.setPassword(cryptedPassword);
					}else{
						String cryptedKeyStr = StringCrypter.encrypt(server.getKey(), key, iv);
						server.setKey(cryptedKeyStr);
					}	
			}
			for (Command command : unit.getCommands() ) {
				Server server = command.getServer();
				if (server.getKey()==null || server.getKey().equals("")){
					String cryptedPassword = StringCrypter.encrypt(server.getPassword(), key, iv);
					server.setPassword(cryptedPassword);
				}else{
					String cryptedKeyStr = StringCrypter.encrypt(server.getKey(), key, iv);
					server.setKey(cryptedKeyStr);
				}
			}
			//first set null that we get from seq
			unit.setId(0);
			DbHelper db = new DbHelper(actual);
			db.createUnit(unit);
		}
		
	}
	/**
	 *
	 */
	public static void exportSettingsByFile(Activity actual,UnitContainer origContainer, String filePath, String filepassword1, String filepassword2) throws Exception{
		SharedPreferences settings = actual.getSharedPreferences("secret", 0);
		String key = settings.getString("key", "");
		String iv = settings.getString("iv", "");
		UnitContainer container = (UnitContainer) CloneUtil.clone(origContainer);
		ArrayList<Integer> objectHashCodes = new ArrayList<Integer>();
		for (Unit unit : container.getAllUnits()) {
			for (Server server : unit.getServers()) {
				if (server.getKey()==null || server.getKey().equals("")){
					//Log.e(TAG,server.getPassword());
					String password = StringCrypter.decrypt(server.getPassword(), key, iv);
					server.setPassword(password);
				}else{
					//Log.e(TAG,server.getKey());
					String keyStr = StringCrypter.decrypt(server.getKey(), key, iv);
					server.setKey(keyStr);
				}
				objectHashCodes.add(System.identityHashCode(server));
			}
			// the server object at the command ist the same as at the serverlist of unit - no need to change 
			for (Command command : unit.getCommands() ) {
				Server server = command.getServer();
				if (objectHashCodes.contains(System.identityHashCode(server))){
					//Log.i(TAG, "Found already transformed  SERVER object !!! Continuing");
					continue;
				}
				if (server.getKey()==null || server.getKey().equals("")){
					//Log.e(TAG,server.getPassword());
					String password = StringCrypter.decrypt(server.getPassword(), key, iv);
					server.setPassword(password);
				}else{
					//Log.e(TAG,server.getKey());
					String keyStr = StringCrypter.decrypt(server.getKey(), key, iv);
					server.setKey(keyStr);				
				}
			}
		}
		StringWriter sw = new StringWriter();
		Serializer serializer = new Persister();
		PrintWriter out;
		serializer.write(container, sw);
		//Log.e(TAG,sw.toString());
		String finalEncrypted = StringCrypter.encrypt(sw.toString(), filepassword1, filepassword2);
		out = null;
		out = new PrintWriter(filePath);
		out.println(finalEncrypted);
		out.close();
		
	}

	public static String getDateFormatted(Date date,String variant){
		DateFormat df = new SimpleDateFormat(variant);
		String dateStr = df.format(date);
		return dateStr;

	}

	public static String getEventType(int type){
		String result = "";
		switch (type) {
			case 0:
				result = " Abnoe ";
				break;
			case 1:
				result = " Pause ";
				break;
			case 2:
				result = " Notification ";
				break;
		}
		return result;

	}


	public static String getTimeDistance(Date startDate, Date endDate) {
		int days = Days.daysBetween(new LocalDateTime(startDate), new LocalDateTime(endDate)).getDays();
		if(days<1){
			int hours = Hours.hoursBetween(new LocalDateTime(startDate), new LocalDateTime(endDate)).getHours();
			if(hours<1) {
				int minutes = Minutes.minutesBetween(new LocalDateTime(startDate), new LocalDateTime(endDate)).getMinutes();
				if (minutes<1) {
					int seconds = Seconds.secondsBetween(new LocalDateTime(startDate), new LocalDateTime(endDate)).getSeconds();
					return seconds + " seconds";
				} else return minutes + " minutes";
			}else return hours+" hours";
		}else return days+" days";
	}

    public static void display(Context ctx, ImageView img, @DrawableRes int drawable) {
        try {
            Glide.with(ctx).load(drawable).into(img);
        } catch (Exception e) {
        }
    }

	// https://stackoverflow.com/questions/8405087/what-is-this-date-format-2011-08-12t201746-384z
	public static String getTimeDistanceShortISO8601(String startDate, DateTime endDate) {
		DateTimeFormatter UTC = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();
		DateTime start = UTC.parseDateTime(startDate);
		int days = Days.daysBetween(new LocalDateTime(start), new LocalDateTime(endDate)).getDays();
		if(days<1){
			int hours = Hours.hoursBetween(new LocalDateTime(start), new LocalDateTime(endDate)).getHours();
			if(hours<1) {
				int minutes = Minutes.minutesBetween(new LocalDateTime(start), new LocalDateTime(endDate)).getMinutes();
				if (minutes<1) {
					int seconds = Seconds.secondsBetween(new LocalDateTime(start), new LocalDateTime(endDate)).getSeconds();
					return seconds + "s";
				} else return minutes + "m";
			}else return hours+"h";
		}else return days+"d";
	}

	public static String santinzeYaml(String yaml){
		//TODO CHECK IF THIS ALSO NEEDED IN OTHER
		yaml = yaml.replaceAll("---","");
		//\n
		yaml = yaml.replaceAll("\\\\n","");
		//\
		yaml = yaml.replaceAll("\\\\","");
		return yaml;
	}

    public static void hideButton(View fab) {
        int y = fab.getHeight()*2;
        fab.animate()
                .translationY(y)
                .setDuration(270)
                .start();
    }

    public static void showButton(View fab) {
        fab.animate()
                .translationY(0)
                .setDuration(270)
                .start();
    }

    public static void fadeOut(final View v) {
        v.setAlpha(1.0f);
        v.animate()
                .setDuration(600)
                .alpha(0.0f);
    }

	public static Set<String> longestCommonSubstrings(String s, String t) {
		int[][] table = new int[s.length()][t.length()];
		int longest = 0;
		Set<String> result = new HashSet<>();

		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.charAt(i) != t.charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1
						: 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result;
	}

	public static String readAssetFileAsString(String sourceLocation, Context ctx) {
        InputStream is;
        try {
            is = ctx.getAssets().open(sourceLocation);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String result = new String(buffer, "UTF-8");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

}