package qf.huesos;


import qf.huesos.boot.Bootloader;

public class Main {
    public static void main(String[] args) throws Exception {
        Bootloader boot = new Bootloader();
        boot.init();
    }
}
