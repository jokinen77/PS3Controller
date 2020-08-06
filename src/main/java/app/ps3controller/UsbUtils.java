/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.ps3controller;

import java.util.ArrayList;
import java.util.List;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakko
 */
public class UsbUtils {
    private static final Logger LOG = LoggerFactory.getLogger(UsbUtils.class);
    
    public static void dumpUsbDevices() throws UsbException {
        StringBuilder sb = new StringBuilder("Usb devices:");
        UsbServices services = UsbHostManager.getUsbServices();
        dump(services.getRootUsbHub(), 0, sb);
        LOG.info(sb.toString());
    }
    
    private static void dump(UsbDevice device, int level, StringBuilder sb) throws UsbException {
        sb.append("\n");
        
        for (int i = 0; i < level; i += 1) {
            sb.append("\t");
        }
        
        sb.append(device);

        if (device.isUsbHub()) {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                dump(child, level + 1, sb);
            }
        }
    }
    
    public static List<UsbDevice> findDevices(short vendorId, short productId) throws UsbException {
        List<UsbDevice> devices = new ArrayList<>();
        UsbServices services = UsbHostManager.getUsbServices();
        findDevices(services.getRootUsbHub(), vendorId, productId, devices);
        return devices;
    }

    private static void findDevices(UsbHub hub, short vendorId, short productId, List<UsbDevice> devices) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                devices.add(device);
            }
            if (device.isUsbHub()) {
                findDevices((UsbHub) device, vendorId, productId, devices);
            }
        }
    }
}
