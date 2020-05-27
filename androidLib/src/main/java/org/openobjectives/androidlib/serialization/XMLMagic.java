package org.openobjectives.androidlib.serialization;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Environment;

public class XMLMagic {

	public static File serializeToXml(Object object,String filePath){
		Serializer serializer = new Persister();
		File result = new File(filePath);
		try {
			serializer.write(object, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public static Object deserializeFromXml(File file, Class theclass){
		Serializer serializer = new Persister();
		Object object=null;
		try {
			object = serializer.read(theclass, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}
	
}
