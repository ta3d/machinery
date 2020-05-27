package org.openobjectives.androidlib.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

/**
 * <B>Class: FileUtils </B> <br>
 * <B>Class created: </B> 11.02.2010 <br>
 * ****************************************************************************
 * <br>
 * Class description: A Util class for static file transactions  <br>
 * TODO <br>
 * *****************************************************************************
 * <br>
 */

public class FileUtils {
	public static boolean StoreByteImage(Context mContext, byte[] imageData,
			int quality, String expName) {

        String sdImageMainDirectory = "/sdcard";
		FileOutputStream fileOutputStream = null;
		String nameFile = expName;
		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;
			
			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length,options);

			
			fileOutputStream = new FileOutputStream(
					sdImageMainDirectory.toString() +"/" + nameFile + ".jpg");
							
  
			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

}
