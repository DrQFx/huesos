package qf.huesos.boot;

import qf.huesos.kernel.Kernel;
import qf.huesos.kernel.SysCall;
import qf.huesos.kernel.provider.ImageProvider;

public class Bootloader {
    Kernel kernel;

    public Bootloader() throws Exception {
        kernel = new Kernel();
    }
}
