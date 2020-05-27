package org.openobjectives.androidlib.rss;

import java.io.Serializable;
import java.util.Collection;

import org.openobjectives.androidlib.lang.StringUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * <B>Class: Server </B> <br>
 * <B>Class created: </B> 14.12.2014 <br>
 * **************************************************************************** <br>
 * Class description: The Container for the RSS Feed to deserialize <br>
 * DEPRECATED AS WE USE PLAINTEXT  <br>
 * ***************************************************************************** <br>
 *
 */
@Root(name="rss",strict=false)
public class XmlFeedContainer {
		@Element
	   	private Channel channel;

		/**
		 * @return the channel
		 */
		public Channel getChannel() {
			return channel;
		}
	
	
		/**
		 * @param channel the channel to set
		 */
		public void setChannel(Channel channel) {
			this.channel = channel;
		}


	@Root(name="channel",strict=false)
	public static class Channel {
	   @ElementList(inline=true)
	   private Collection<Item> list;
	   @Element(data=true)
	   public String title; 
	   @Element
	   public String link; 

	   /**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
	
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
	
		/**
		 * @return the link
		 */
		public String getLink() {
			return link;
		}
	
		/**
		 * @param link the link to set
		 */
		public void setLink(String link) {
			this.link = link;
		}

	   public void setList(Collection<Item> entry) {
	      if(entry.isEmpty()) {
	         throw new IllegalArgumentException("Empty collection");              
	      }
	      this.list = entry;          
	   }        

	   public Collection<Item> getList() {
	      return list;           
	   }
	   
	}
	
	   
	@Root(strict=false)
	public static class Item {

	   @Element
	   public String title; 
	   @Element
	   public String author; 	   
	   @Element
	   public String link; 
	   @Element(data=true)
	   public String description;
	   
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		/**
		 * @return the author
		 */
		public String getAuthor() {
			return author;
		}
		/**
		 * @param author the author to set
		 */
		public void setAuthor(String author) {
			this.author = author;
		}
		/**
		 * @return the link
		 */
		public String getLink() {
			return link;
		}
		/**
		 * @param link the link to set
		 */
		public void setLink(String link) {
			this.link = link;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}  


	}

}

