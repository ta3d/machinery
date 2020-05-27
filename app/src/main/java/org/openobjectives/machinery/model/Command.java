/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.model;

import java.io.Serializable;

/**
 * <B>Class: Command </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Abstract a command  <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class Command implements Serializable {

    private static final String TAG = Command.class.getSimpleName();
    private static final long serialVersionUID = Double.doubleToLongBits(1.0);
    private String command;
    private Server server;
    private String name;

    public Command() {
        super();
    }

    public Command(String name, String command, Server server) {
        this.name = name;
        this.command = command;
        this.server = server;
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
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command
     *            the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param server
     *            the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

}
