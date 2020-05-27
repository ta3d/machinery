package org.openobjectives.machinery.ui;

public class MainMenuItem {

    public int image;
    public String name;
    public String url;
    public boolean direct = true;

    public MainMenuItem(String name) {
        this.name = name;
    }

    public MainMenuItem(String name, String url, int image, boolean direct) {
        this.direct = direct;
        this.image = image;
        this.name = name;
        this.url = url;
    }

}
