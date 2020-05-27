package org.openobjectives.androidlib.lang;

import android.util.Log;

public class StringUtils {
	
	public static String cropStringToLength(String string, int length){
		if ((string.length()-3)>length){
			string = string.substring(0, length).concat("...");
		}
		return string;
		
	}

}
