package org.openobjectives.androidlib.net.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

/**
 * <B>Class: HttpPostExecute </B> <br>
 * <B>Class created: </B> 13.02.2010 <br>
 * ****************************************************************************
 * <br>
 * Class description: executes post commands - set just string parameter or use fileupload  <br>
 * TODO <br>
 * *****************************************************************************
 * <br>
 */

public class HttpPostExecute {

    
    static final int BUFF_SIZE = 1024;
    static final byte[] buffer = new byte[BUFF_SIZE];
    
	private BasicHttpParams params;
	private HashMap<String, String> paramsMap = new HashMap<String, String>();
	
	public HttpPostExecute() {
		super();
		params  = new BasicHttpParams(); 
		paramsMap = new HashMap<String, String>();
	}

	
	public void setHttpParam(String name, String value){
		params.setParameter(name, value); 
		paramsMap.put(name, value);
	}
	
	public String postSimpleFile(String url, File file, String mimetype) {
		HttpClient client = new DefaultHttpClient();  
		HttpPost post = new HttpPost(url);  
		HttpResponse response = null;
		try {
			post.setEntity(new FileEntity(file, mimetype));
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			return "MALFORMED POST";
		} catch (IOException e) {
			return "NONET";
		}  

		HttpEntity resEntity = response.getEntity();  
		String responseString = null; 
		if (resEntity != null) {    
			try {
				resEntity.consumeContent();
				responseString=resEntity.getContent().toString();
			} catch (IOException e) {
				return "NONET";
			} 
		}
		return responseString.trim();
	}
	
	public boolean postSimpleParams(String url) throws UnsupportedEncodingException{
		HttpClient client = new DefaultHttpClient();  
		HttpPost post = new HttpPost(url);  
		post.setParams(params);
		HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		}  

		HttpEntity resEntity = response.getEntity();  
		if (resEntity != null) {    
			try {
				resEntity.consumeContent();
			} catch (IOException e) {
				return false;
			} 
		}
		return true;
	}
	
	public String postParamsAndFile(String url, File file) {
		HttpClient client = new DefaultHttpClient();   
		HttpPost post = new HttpPost(url);
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("file", new FileBody(file));
		post.setEntity(entity);
		HttpResponse response = null;
		try {
			post.setParams(params);
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			return "MALFORMED POST";
		} catch (IOException e) {
			return "NONET";
		}  

		HttpEntity resEntity = response.getEntity();  
		String responseString = null; 
		if (resEntity != null) {    
			try {
				resEntity.consumeContent();
				responseString=resEntity.getContent().toString();
			} catch (IOException e) {
				return "NONET";
			} 
		}
		return responseString.trim();
	}
	
//    	/**
//	    * postMultipartParamsAndFile does Multipart by hand
//	    */
//	    public String postMultipartParamsAndFile (String url,File file) {
//	        try {
//	            URL servlet = new URL(url);  
//	            URLConnection conn = servlet.openConnection();
//	            conn.setDoOutput(true);
//	            conn.setDoInput(true);
//	            conn.setUseCaches(false);
//	            InputStream stream = conn.getInputStream();
//	            String boundary = "---------------------------7d226f700d0";
//	            conn.setRequestProperty("Content-type","multipart/form-data; boundary=" + boundary);
//	            conn.setRequestProperty("Referer", "http://127.0.0.1/index.jsp");
//	            conn.setRequestProperty("Cache-Control", "no-cache");
//	            conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
//	            conn.setRequestProperty("Accept","[star]/[star]");
//
//
//	             
//	            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
//	            out.writeBytes("--" + boundary + "\r\n");
//	            for (String param : paramsMap.keySet()) {
//	            	if(paramsMap.get(param)!=null)
//	            		writeParam(param, paramsMap.get(param), out, boundary);
//				}
//	            writeFile("file", file.getAbsolutePath(), out, boundary);
//	            out.flush();
//	            out.close();
//	            
//	            BufferedReader br = new BufferedReader(new InputStreamReader(stream));     
//	            String line;
//	            String result=null;
//	            while ((line = br.readLine()) != null){
//	                result = result+line;           
//	            } 
//	            br.close();
//	        	stream.close();
//	            return result;
//	        } catch (Exception e) {  
//	            Log.e(TAG,e.toString());
//	            return null;
//	        }
//	    }
//	
//    private static void writeParam(String name, String value, DataOutputStream out, String boundary) {
//        try {
//            out.writeBytes("content-disposition: form-data; name=\"" + name + "\"\r\n\r\n");
//            out.writeBytes(value);
//            out.writeBytes("\r\n" + "--" + boundary + "\r\n");
//        } catch (Exception e) {  Log.e(TAG,e.toString());      }
//    }
//    
//    private static void writeFile(String name, String filePath, DataOutputStream out, String boundary) {
//        try {
//        out.writeBytes("content-disposition: form-data; name=\"" + name + "\"; filename=\""
//                            + filePath + "\"\r\n");
//            out.writeBytes("content-type: application/octet-stream" + "\r\n\r\n");
//            FileInputStream fis = new FileInputStream(filePath);
//            while (true) {
//                synchronized (buffer) {
//                    int amountRead = fis.read(buffer);
//                        if (amountRead == -1) {
//                            break;
//                        }
//                    out.write(buffer, 0, amountRead); 
//                    }
//            }
//            fis.close();
//            out.writeBytes("\r\n" + "--" + boundary + "\r\n");
//             } catch (Exception e) {  Log.e(TAG,e.toString());      }
//    }
//
//    





}
