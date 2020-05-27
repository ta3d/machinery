/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.model;

import org.openobjectives.androidlib.lang.StringUtils;
import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * <B>Class: Server </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A server abstraction <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
@Element
public class Server implements Serializable {

    private static final String TAG = Server.class.getSimpleName();
    private static final long serialVersionUID = Double.doubleToLongBits(1.0);
    @Element
    private String hostname;
    @Element
    private int port;
    @Element
    private String name;
    @Element(required = false, data = true)
    private String user;
    @Element(required = false, data = true)
    private String password;
    @Element(required = false, data = true)
    private String key;


    public Server() {
        super();
    }

    public Server(String hostname, int port, String name, String user,
                  String password, String key) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
        this.key = key;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerUrl(int length) {
        return StringUtils.cropStringToLength(hostname, length) + ":" + port;
    }


    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }


}
