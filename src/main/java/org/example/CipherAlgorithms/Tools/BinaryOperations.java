package org.example.CipherAlgorithms.Tools;


import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class BinaryOperations {

    // разобраться с размером XOR между двумя массивами байт (одинаковый всегда или нет?)
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

    public static byte[] leftCycleShift(byte[] input, long shift) {
        byte[] result;
        long value = bytesToLong(input);
        long effectiveShift = shift % 64;
        value = (value << effectiveShift) | (value >>> (64 - effectiveShift)); // >> или >>> ?
        result = longToBytes(value);

        return result;
    }

    public static byte[] rightCycleShift(byte[] input, long shift) {
        byte[] result = new byte[input.length];
        long value = bytesToLong(input);
        long effectiveShift = shift % 64;
        value = (value >>> effectiveShift) | (value << (64 - effectiveShift)); // >> или >>> ?
        result = longToBytes(value);

        return result;
    }

    public static long leftCycleShift(long number, int numBits, long k) {
        long valueShift = Math.abs(k % numBits);
        return (number << valueShift) | ((number & (((1L << valueShift) - 1) << (numBits - valueShift))) >>> (numBits - valueShift));
    }

    public static long rightCycleShift(long number, int numBits, long k) {
        long valueShift = Math.abs(k % numBits);
        return (number >>> valueShift) | ((number & ((1L << valueShift) - 1)) << (numBits - valueShift));
    }

    public static int leftCycleShift(int number, int numBits, int k) {
        int valueShift = Math.abs(k % numBits);
        return (number << valueShift) | ((number & (((1 << valueShift) - 1) << (numBits - valueShift))) >>> (numBits - valueShift));
    }

    public static int rightCycleShift(int number, int numBits, int k) {
        int valueShift = Math.abs(k % numBits);
        return (number >>> valueShift) | ((number & ((1 << valueShift) - 1)) << (numBits - valueShift));
    }


    // для 8 байт, long
    public static long bytesToLong(byte[] input) {
        long result = 0;
        for (byte b : input) {
            result = (result << 8) | (b & 0xff);
        }

        return result;
    }

    // для 8 байт, long
    public static byte[] longToBytes(long input) {
        byte[] result = new byte[8];

        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (input & 0xFF);
            input >>= 8;
        }

        return result;
    }

    // todo протестировать
    public static byte[] intToByteArray(int[] array) {
        byte[] result = new byte[array.length * 4 ];

        ByteBuffer buffer = ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN);
        for (int value : array) {
            buffer.putInt(value);
        }

//        for (int i = 0; i < 4; i++) {
//            result[i * 4] = (byte) (array[i] >>> 24);
//            result[i * 4 + 1] = (byte) (array[i] >>> 16);
//            result[i * 4 + 2] = (byte) (array[i] >>> 8);
//            result[i * 4 + 3] = (byte) (array[i]);
//        }

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



    public static long getBits(byte[] bytes, int from, int countBits) {
        byte[] result = new byte[(countBits + Byte.SIZE - 1) / Byte.SIZE];

        for (int i = 0; i < countBits; i++) {
            if (from + i >= bytes.length * Byte.SIZE) {
                setBitFromEnd(result, i / countBits, false);
            } else {
                setBitFromEnd(result, i, getBitFromEnd(bytes, from + i) == 1);
            }
        }

        return bytesToLong(result);
    }

    public static long getBits(long block, int from, int countBits) {
        return (block << (Long.SIZE - from - 1) >>> (Long.SIZE - countBits));
    }

    public static int getBitFromEnd(byte[] bytes, int indexBit) {
        return (bytes[indexBit / Byte.SIZE] >> (Byte.SIZE - indexBit % Byte.SIZE - 1)) & 1;
    }

    public static void setBitFromEnd(byte[] bytes, int indexBit, boolean valueBit) {
        if (valueBit) {
            bytes[indexBit / Byte.SIZE] |= (byte) (1 << (Byte.SIZE - indexBit % Byte.SIZE - 1));
        } else {
            bytes[indexBit / Byte.SIZE] &= (byte) ~(1 << (Byte.SIZE - indexBit % Byte.SIZE - 1));
        }
    }

    // битовое сложение по модулю
    public static long sumModule(long first, long second, int numBits) {
        long result = 0;
        long reminder = 0;

        for (int i = 0; i < numBits; i++) {
            long tempSum = ((first >> i) & 1) ^ ((second >> i) & 1) ^ reminder;
            reminder = (((first >> i) & 1) + ((second >> i) & 1) + reminder) >> 1;
            result |= tempSum << i;
        }

        return result;
    }

    // битовое вычитание по модулю
    public static long subtractModule(long first, long second, int numBits) {
        return sumModule(first, ~second + 1, numBits);
    }

//    public static void main(String[] args) {
//        long a = 123456789123L;
//        long b = 987654321987L;
//        int module = 32;
//
////        System.out.println("My = " + sumModule(a, b, module));
//    }

}
