package ie.hocusfocus.utils;

import com.axio.melonplatformkit.DeviceHandle;

/**
 * Utility class to hold general Melon Utilities.
 */
public class MelonUtils {

    /**
     * Extracts the name that is written on the Melon.
     *
     * @param deviceHandle the DeviceHandle for the melon
     * @return The name written on the melon or UNKN if it could not be determined.
     */
    public static String getMelonName(DeviceHandle deviceHandle) {
        try {
            String serial = deviceHandle.getName().split("_")[1];
            return serial.substring(0, 4);
        } catch (Exception e) {
            return "UNKN";
        }
    }
}
