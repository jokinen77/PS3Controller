/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.ps3controller;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakko
 */
public class PS3Controller {
    
    private static final Logger LOG = LoggerFactory.getLogger(PS3Controller.class);
    
    public static final short productId = 616;
    public static final short vendorId = 1356;

    private UsbDevice device;
    private UsbInterface intf;
    private UsbPipe pipe;

    private PS3ControllerProperties properties;

    private Robot robot;
    
    private int listenInterval = 0;
    private boolean reverseRightY = false;
    private boolean reverseRightX = false;

    public PS3Controller(UsbDevice device) throws AWTException {
        LOG.info("PS3 controller device: " + device);
        this.device = device;
        this.robot = new Robot();
    }

    public void initialize() throws UsbException, IOException {
        this.properties = new PS3ControllerProperties();
        this.listenInterval = this.properties.getIntegerValue("listen_interval");
        this.reverseRightX = this.properties.getBooleanValue("reverse_right_x_axis");
        this.reverseRightY = this.properties.getBooleanValue("reverse_right_y_axis");
        this.intf = getIface();
        this.claim();
        UsbEndpoint endpoint = this.getEndpoint();
        this.pipe = endpoint.getUsbPipe();
        this.pipe.open();
        LOG.info("PS3 controller initialized!");
    }

    public void listen() throws InterruptedException {
        LOG.info("Starting to listen the PS3 controller...");
        while (true) {
            Thread.sleep(listenInterval);
            Set<PS3Button> toRelease = new HashSet<>();
            toRelease.addAll(Arrays.asList(PS3Button.values()));
            ArrayDeque<PS3Button> pressedButtons;
            try {
                pressedButtons = getPressedButtons();
            } catch (UsbException ex) {
                LOG.error("Can't get pressed buttons from the PS3 controller! Meaby device is unplugged? Message: " + ex.getMessage());
                break;
            }
            pressedButtons.forEach(button -> {
                toRelease.remove(button);
                this.press(this.properties.getButtonId(button));
            });
            toRelease.forEach(button -> {
                this.release(this.properties.getButtonId(button));
            });
        }
    }

    private ArrayDeque<PS3Button> getPressedButtons() throws UsbException {
        ArrayDeque<PS3Button> pressedButtons = new ArrayDeque<>();
        byte[] data = new byte[49];
        int received = this.pipe.syncSubmit(data);
        LOG.debug("Data: " + Arrays.toString(data));
        this.identifyPressedButtons(data, pressedButtons);
        return pressedButtons;
    }

    private UsbInterface getIface() {
        UsbConfiguration config = this.device.getActiveUsbConfiguration();
        return (UsbInterface) config.getUsbInterface((byte) 0);
    }

    private void claim() throws UsbException {
        this.intf.claim(new UsbInterfacePolicy() {
            @Override
            public boolean forceClaim(UsbInterface usbInterface) {
                return true;
            }
        });
        LOG.info("PS3 controller's interface claimed!");
    }

    private UsbEndpoint getEndpoint() {
        return (UsbEndpoint) this.intf.getUsbEndpoint((byte) -127);
    }

    public static List<UsbDevice> findPS3Controllers() throws UsbException {
        List<UsbDevice> devices = UsbUtils.findDevices(vendorId, productId);
        LOG.info("Found " + devices.size() + " PS3 controller!");
        return devices;
    }

    private void identifyPressedButtons(byte[] data, ArrayDeque<PS3Button> pressedButtons) {
        if (data[2] == 1) {
            pressedButtons.addFirst(PS3Button.SELECT);
        }

        if (data[2] == 2) {
            pressedButtons.addFirst(PS3Button.L3);
        }

        if (data[2] == 4) {
            pressedButtons.addFirst(PS3Button.R3);
        }

        if (data[2] == 8) {
            pressedButtons.addFirst(PS3Button.START);
        }

        if (data[2] != 0 && data[14] != 0) {
            pressedButtons.addFirst(PS3Button.UP);
        }

        if (data[2] != 0 && data[15] != 0) {
            pressedButtons.addFirst(PS3Button.RIGHT);
        }

        if (data[2] != 0 && data[16] != 0) {
            pressedButtons.addFirst(PS3Button.DOWN);
        }

        if (data[2] != 0 && data[17] != 0) {
            pressedButtons.addFirst(PS3Button.LEFT);
        }

        // CROSS, TRIANGLE, ...
        if (data[3] != 0 && data[22] != 0) {
            pressedButtons.addFirst(PS3Button.TRIANGLE);
        }

        if (data[3] != 0 && data[23] != 0) {
            pressedButtons.addFirst(PS3Button.CIRCLE);
        }

        if (data[3] != 0 && data[24] != 0) {
            pressedButtons.addFirst(PS3Button.CROSS);
        }

        if (data[3] != 0 && data[25] != 0) {
            pressedButtons.addFirst(PS3Button.SQUARE);
        }

        // R1 R2 L1 L2
        if (data[3] != 0 && data[21] != 0) {
            pressedButtons.addFirst(PS3Button.R1);
        }

        if (data[3] != 0 && data[19] != 0) {
            pressedButtons.addFirst(PS3Button.R2);
        }

        if (data[3] != 0 && data[18] != 0) {
            pressedButtons.addFirst(PS3Button.L2);
        }

        if (data[3] != 0 && data[20] != 0) {
            pressedButtons.addFirst(PS3Button.L1);
        }

        // Left joystick X-axis
        if (data[6] >= 0 && data[6] < 100) {
            pressedButtons.addFirst(PS3Button.L_LEFT);
        }

        if (data[6] <= -1 && data[6] > -100) {
            pressedButtons.addFirst(PS3Button.L_RIGHT);
        }

        // Left joystick Y-axis
        if (data[7] <= -1 && data[7] > -100) {
            pressedButtons.addFirst(PS3Button.L_DOWN);
        }

        if (data[7] >= 0 && data[7] < 100) {
            pressedButtons.addFirst(PS3Button.L_UP);
        }

        // Right joystick X-axis
        if (data[8] >= 0 && data[8] < 100) {
            if (!this.reverseRightX) {
                pressedButtons.addFirst(PS3Button.R_LEFT);
            } else {
                pressedButtons.addFirst(PS3Button.R_RIGHT);
            }
        }

        if (data[8] < 0 && data[8] > -100) {
            if (!this.reverseRightX) {
                pressedButtons.addFirst(PS3Button.R_RIGHT);
            } else {
                pressedButtons.addFirst(PS3Button.R_LEFT);
            }
        }

        // Right joystick Y-axis
        if (data[9] < 0 && data[9] > -100) {
            if (!this.reverseRightY) {
                pressedButtons.addFirst(PS3Button.R_DOWN);
            } else {
                pressedButtons.addFirst(PS3Button.R_UP);
            }
        }

        if (data[9] >= 0 && data[9] < 100) {
            if (!this.reverseRightY) {
                pressedButtons.addFirst(PS3Button.R_UP);
            } else {
                pressedButtons.addFirst(PS3Button.R_DOWN);
            }
        }
    }

    private void press(int i) {
        robot.keyPress(i);
    }

    private void release(int i) {
        robot.keyRelease(i);
    }
    
}
