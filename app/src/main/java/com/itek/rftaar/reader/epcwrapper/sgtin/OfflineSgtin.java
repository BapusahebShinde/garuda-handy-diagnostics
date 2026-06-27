package com.itek.rftaar.reader.epcwrapper.sgtin;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class OfflineSgtin {


    public static long generateSerialNumber(String deviceId, String barcode, String tid,String MaxSerial) {
        try {
            long MAXSERIAL = Long.parseLong(MaxSerial);
            // Generate a UUID as a base
            UUID uuid = UUID.randomUUID();

            // Incorporate a timestamp for additional uniqueness
            long timestamp = System.currentTimeMillis() / 1000; // Seconds since epoch

            // Combine UUID, timestamp, device ID, barcode, and TID into a byte array
            byte[] uuidBytes = getUUIDBytes(uuid);
            byte[] timestampBytes = ByteBuffer.allocate(Long.BYTES).putLong(timestamp).array();
            byte[] deviceIdBytes = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                deviceIdBytes = deviceId.getBytes(StandardCharsets.UTF_8);
            }
            byte[] barcodeBytes = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                barcodeBytes = barcode.getBytes(StandardCharsets.UTF_8);
            }
            byte[] tidBytes = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                tidBytes = tid.getBytes(StandardCharsets.UTF_8);
            }

            byte[] data = new byte[uuidBytes.length + timestampBytes.length + deviceIdBytes.length + barcodeBytes.length + tidBytes.length];
            System.arraycopy(uuidBytes, 0, data, 0, uuidBytes.length);
            System.arraycopy(timestampBytes, 0, data, uuidBytes.length, timestampBytes.length);
            System.arraycopy(deviceIdBytes, 0, data, uuidBytes.length + timestampBytes.length, deviceIdBytes.length);
            System.arraycopy(barcodeBytes, 0, data, uuidBytes.length + timestampBytes.length + deviceIdBytes.length, barcodeBytes.length);
            System.arraycopy(tidBytes, 0, data, uuidBytes.length + timestampBytes.length + deviceIdBytes.length + barcodeBytes.length, tidBytes.length);

            // Use SHA-256 for better distribution and collision resistance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);

            // Extract a portion of the hash as the serial number
            long serialNumber = 0;
            for (int i = 0; i < 8; i++) {
                serialNumber = (serialNumber << 8) | (hash[i] & 0xFF);
            }

            // Adjust the range to fit within 9999999999
            return Math.abs(serialNumber % MAXSERIAL);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static byte[] getUUIDBytes(UUID uuid) {
        byte[] uuidBytes = uuid.toString().getBytes();
        return uuidBytes;
    }
}
