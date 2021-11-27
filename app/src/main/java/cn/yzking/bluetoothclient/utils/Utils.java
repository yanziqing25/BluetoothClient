package cn.yzking.bluetoothclient.utils;

import java.util.HashSet;
import java.util.Set;

import cn.yzking.bluetoothclient.Student;

public class Utils {
    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }

    public static Set<Student> parseData(byte[] data) {
        Set<Student> studentSet = new HashSet<>();
        StringBuilder id = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x03) {
                id = new StringBuilder();
                for (int j = i - 10; j < i; j++) {
                    id.append(data[j] - 0x30);
                }
            }
            if (data[i] == 0x0a) {
                byte n1 = (byte) (data[i - 4] - 0x30);
                byte n2 = (byte) (data[i - 3] - 0x30);
                byte n3 = (byte) (data[i - 2] - 0x30);
                byte n4 = (byte) (data[i - 1] - 0x30);
//                float temperature = n1 * 10 + n2 + (float) n3 / 10 + (float) n4 / 100;
                float temperature = (float) Math.round((n1 * 10 + n2 + (float) n3 / 10 + (float) n4 / 100) * 100) / 100;
                studentSet.add(new Student(id.toString(), temperature));
            }
        }
        return studentSet;
    }
}
