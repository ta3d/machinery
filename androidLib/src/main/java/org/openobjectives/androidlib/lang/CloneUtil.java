package org.openobjectives.androidlib.lang;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public class CloneUtil {
	
	//performance aweful please use only with small objects
	public static Serializable clone(Serializable object){
		return SerializationUtils.clone(object);
	}
}
