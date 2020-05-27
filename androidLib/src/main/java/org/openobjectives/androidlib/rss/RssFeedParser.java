package org.openobjectives.androidlib.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class RssFeedParser {

	public static XmlFeedContainer parseFeed(String urlStr){
		Serializer serializer = new Persister();
		XmlFeedContainer feedContainer =null;
		try {
			URL url = new URL(urlStr);
			url.openConnection();
			feedContainer = (XmlFeedContainer)serializer.read(XmlFeedContainer.class, (InputStream)url.getContent());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feedContainer;
	}
	
}
