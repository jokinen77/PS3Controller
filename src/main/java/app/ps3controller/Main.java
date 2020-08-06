/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.ps3controller;

import java.awt.AWTException;
import java.io.IOException;
import java.util.List;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakko
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws UsbException, AWTException, IOException, InterruptedException {
        UsbUtils.dumpUsbDevices();
        List<UsbDevice> devices = PS3Controller.findPS3Controllers();
        if (!devices.isEmpty()) {
            PS3Controller controller = new PS3Controller(devices.get(0));
            controller.initialize();
            controller.listen();
        } else {
            LOG.error("No PS3 controllers found!");
        }
    }
    
}
