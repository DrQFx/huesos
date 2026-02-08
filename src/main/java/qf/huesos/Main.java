package qf.huesos;


import qf.huesos.boot.Bootloader;

public class Main {
    static void main(String[] args) {
        Bootloader boot = new Bootloader();
        boot.init();
    }
}
