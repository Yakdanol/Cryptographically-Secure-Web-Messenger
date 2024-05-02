package org.example.CipherAlgorithms.Tools;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryOperations {

    // todo разобраться с размером XOR между двумя массивами байт (одинаковый всегда или нет?)
    public static byte[] xor(byte[] first, byte[] second) {
        // универсальный, для любой длины
        int maxLength = Math.max(first.length, second.length);
        byte[] result = new byte[maxLength];

        for (int i = 0; i < maxLength; i++) {
            byte firstByte = i < first.length ? first[i] : 0;
            byte secondByte = i < second.length ? second[i] : 0;
            result[i] = (byte) (firstByte ^ secondByte);
        }

        // для одинаковой длины
//        byte[] result = new byte[first.length];
//
//        for (int i = 0; i < result.length; i++) {
//            result[i] = (byte) (first[i] ^ second[i]);
//        }

        return result;
    }

    public static byte[] leftCycleShift(byte[] input, int shift) {
        byte[] result;
        long value = bytesToLong(input);
        int effectiveShift = shift % 64;
        value = (value << effectiveShift) | (value >>> (64 - effectiveShift)); // >> или >>> ?
        result = longToBytes(value);

        return result;
    }

    public static byte[] rightCycleShift(byte[] input, int shift) {
        byte[] result = new byte[input.length];
        long value = bytesToLong(input);
        int effectiveShift = shift % 64;
        value = (value >>> effectiveShift) | (value << (64 - effectiveShift)); // >> или >>> ?
        result = longToBytes(value);

        return result;
    }

    // для 8 байт
    public static long bytesToLong(byte[] input) {
        long result = 0;
        for (byte b : input) {
            result = (result << 8) | (b & 0xff);
        }

        return result;
    }

    // для 8 байт
    public static byte[] longToBytes(long input) {
        byte[] result = new byte[8];

        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (input & 0xFF);
            input >>= 8;
        }

        return result;
    }

    public static byte[] getLeftNbits(byte[] input, int n) {
        int len = n / 8 + (n % 8 == 0 ? 0 : 1);
        byte[] result = new byte[len];

        for (int i = 0; i < n / 8; i++) {
            result[i] = input[i];
            System.out.println(byteToString(result[i]));
        }

        // если остался нецелый кусок
        if (n % 8 != 0) {
            result[len - 1] = (byte) (input[len - 1] >> (8 - (n % 8)));
            System.out.println(byteToString(result[len - 1]));
        }

        return result;
    }

    public static byte[] getRightNbits(byte[] input, int n) {
        int len = n / 8 + (n % 8 == 0 ? 0 : 1);
        byte[] result = new byte[len];

        long value = bytesToLong(input);
        value &= (1L << n) - 1; // Отбираем правые n битов
        for (int i = 0; i < n / 8; i++) {
            result[len - i - 1] = (byte) (value & ((1 << 8) - 1));
            value >>>= 8;
        }

        if (n % 8 != 0) {
            result[0] = (byte) (value & ((1 << (n % 8)) - 1));
        }

        return result;
    }

    public static String byteToString(byte b) {
        StringBuilder result = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            int bit = (b >> i) & 1;
            result.append(bit);
        }
        return result.toString();
    }

}