/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <B>Class: Unit </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A concrete cluster config saveable in db <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class Unit implements Serializable {

    private static final long serialVersionUID = Double.doubleToLongBits(1.0);
    private static final String TAG = Unit.class.getSimpleName();
    private ArrayList<Command> commands;
    private ArrayList<Server> servers;
    private String name;
    private boolean online = false;
    private long id = 0;

    public Unit() {

    }

    public Unit(ArrayList<Command> commands, String name, boolean online) {
        super();
        this.commands = commands;
        this.name = name;
        this.online = online;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * @param online
     *            the online to set
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the commands
     */
    public ArrayList<Command> getCommands() {
        return commands;
    }

    /**
     * @param commands
     *            the commands to set
     */
    public void setCommands(ArrayList<Command> commands) {
        this.commands = commands;
    }

    /**
     * @return the servers
     */
    public ArrayList<Server> getServers() {
        return servers;
    }

    /**
     * @param servers
     *            the servers to set
     */
    public void setServers(ArrayList<Server> servers) {
        this.servers = servers;
    }

}
