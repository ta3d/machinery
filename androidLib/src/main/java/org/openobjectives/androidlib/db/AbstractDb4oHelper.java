package org.openobjectives.androidlib.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public abstract class AbstractDb4oHelper {
	private static final String APP_VERSION = "todefine";
	public abstract void init();
	private Context mCtx;

	
	public AbstractDb4oHelper(Context ctx) {
		this.mCtx = ctx; 
		
	}
	
	protected String writeVersion(String appversion){
		File dir = mCtx.getDir("objects", Context.MODE_PRIVATE);
		File ver = new File(dir, "version.txt");
		ver.delete();
		ver = new File(dir, "version.txt");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(ver));
			out.println(appversion);
			out.close();
			return appversion;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	protected String findVersion(String appversion) {
		File dir = mCtx.getDir("objects", Context.MODE_PRIVATE);
		File ver = new File(dir, "version.txt");
		if (!ver.exists()) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileOutputStream(ver));
				out.println(appversion);
				out.close();
				return appversion;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
//		if (!ver.canRead()) {
//			//ATTENTION HERE  could be after disi upgrade
//			ver.delete();
//			ver = new File(dir, "version.txt");
//			boolean b = ver.canRead();
//			PrintWriter out = null;
//			try {
//				out = new PrintWriter(new FileOutputStream(ver));
//				out.println(appversion);
//				out.close();
//				return appversion;
//			} catch (IOException ex) {
//				throw new RuntimeException(ex);
//			} finally {
//				if (out != null) {
//					out.close();
//				}
//			}
//		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(ver));
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					//Log.e(getClass().getName(), e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T loadObject(Class<T> cls) {
		Context ctx = mCtx;
		File f = createFileName(cls.getName(), ctx);

		if (f.exists()) {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new FileInputStream(f));
				return (T) in.readObject();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (IOException e) {
						//Log.e(getClass().getName(), e.getMessage());
					}
				}
			}
		}

		return null;
	}

	protected File createFileName(String name, Context ctx) {
		File f = new File(ctx.getDir("objects", Context.MODE_PRIVATE), name
				+ ".obj");
		return f;
	}

	protected void saveObject(Object o) {
		Context ctx = mCtx;
		File f = createFileName(o.getClass().getName(), ctx);

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
					//Log.e(getClass().getName(), e.getMessage());
				}
			}
		}
	}
	
	public void backup(File toFile, ArrayList objects) throws IOException {

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(toFile));
			out.writeObject(APP_VERSION);
			for (Object object : objects) {
				out.writeObject(object);
			}
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					//Log.e(getClass().getName() + "#backup", e.getMessage());
				}
			}
		}

	}

	public ArrayList restore(File fromFile) throws Exception {

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				fromFile));
		ArrayList objects = new ArrayList();
		try {
			String ver = (String) in.readObject();
			if (APP_VERSION.equals("ver")) {
				throw new Exception("Incompatible backup version " + ver);
			}
			while(in.available()!=0){
				Object o = in.readObject();
				objects.add(o);
				saveObject(o);
			}

		} catch (IOException ex) {
			throw new Exception(
					"Failed to read backup file, it could be corrupt");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					//Log.e(getClass().getName() + "#restore", e.getMessage());
				}
			}
		}
		return objects;

	}

	/**
	 * @return the mCtx
	 */
	public Context getContext() {
		return mCtx;
	}


	
}
