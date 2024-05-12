package org.example.CipherAlgorithms.Tools;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Slf4j
public class Permutations {
    public enum RuleIndex {
        LeftZero,
        LeftOne,
        RightZero,
        RightOne
    }

    public static byte[] permutate(byte[] array, byte[] Pblock, RuleIndex rule) {
        // пока так, потом надо реализовать паддинг
        int resultSize = Pblock.length / 8 + (Pblock.length % 8 == 0 ? 0 : 1);
        byte[] result = new byte[resultSize]; // (Pblock.length + 7) / 8;

        for (int i = 0; i < Pblock.length; i++) {
            boolean bitValue = getBit(array, Pblock[i], rule);
            setBit(result, i, bitValue, rule);
        }

        return result;
    }

    // добавить 0xFF?
    private static boolean getBit(byte[] array, byte index, RuleIndex rule) {
        if (!checkBorders(index, array.length, rule))
            throw new IndexOutOfBoundsException("Error number of index: " + index);

        int byteIndex; // индекс в массиве array
        int bitIndex; // номер бита

        // для сведения нумерации с 1 к нумерации с 0
        if (rule == RuleIndex.LeftOne || rule == RuleIndex.RightOne) {
            index--;
        }

        switch (rule) {
            case LeftZero:
            case LeftOne:
                byteIndex = index / 8;
                bitIndex = 7 - index % 8;
                break;
            case RightZero:
            case RightOne:
                byteIndex = (array.length * 8 - 1 - index) / 8;
                bitIndex = index % 8;
                break;
            default:
                throw new IllegalArgumentException("Unknown indexing rule" + rule);
        }

        //boolean temp = ((array[byteIndex] >> bitIndex) & 1) == 1;
        return ((array[byteIndex] >> bitIndex) & 1) == 1;
    }

    // проверка выхода за границы массива байт
    public static boolean checkBorders(int index, int len, RuleIndex rule) {
        len *= 8;
        if (rule == RuleIndex.LeftOne || rule == RuleIndex.RightOne) {
            len++;
        }

        return (index < len && index >= 0);
    }

    private static void setBit(byte[] array, int index, boolean bit, RuleIndex rule) {
        int byteIndex;
        int bitIndex;

        switch (rule) {
            case LeftZero:
            case LeftOne:
                byteIndex = index / 8;
                bitIndex = 7 - index % 8;
                break;
            case RightZero:
            case RightOne:
                byteIndex = (array.length * 8 - 1 - index) / 8;
                bitIndex = 7 - index % 8;
                break;
            default:
                throw new IllegalArgumentException("Unknown indexing rule" + rule);
        }

        if (bit) {
            array[byteIndex] |= (byte) (1 << bitIndex);
        } else {
            array[byteIndex] &= (byte) ~(1 << bitIndex);
        }
        //var temp = array[byteIndex] & 0xFF;
    }

//    public static void main(String[] args) {
//        //byte[] testArray = new byte[] {(byte) 0b10110101}; // лево / право с 1
//        //byte[] testPermutation = new byte[] {1, 2, 4, 8, 3, 7, 6, 5};
//        //byte[] result = permutate(testArray, testPermutation, RuleIndex.LeftOne);
//
////        byte[] testArray = new byte[] {(byte) 0b10000000}; // лево / право с 0
////        byte[] testPermutation = new byte[] {1, 3, 7, 4, 2, 0, 5, 6};
////        byte[] result = permutate(testArray, testPermutation, RuleIndex.RightZero);
//
////        byte[] testArray = new byte[] {(byte) 0b10110101}; // Расширяющее
////        byte[] testPermutation = new byte[] {1, 1, 7, 4, 2, 4, 8, 5, 6, 6};
////        byte[] result = permutate(testArray, testPermutation, RuleIndex.LeftOne);
//
//        byte[] testArray = new byte[] {(byte) 0b10110101}; // Сужающее
//        byte[] testPermutation = new byte[] {1, 2, 5, 6, 8};
//        byte[] result = permutate(testArray, testPermutation, RuleIndex.LeftOne);
//
//        System.out.print("testArray (binary): ");
//        for (byte b : testArray) {
//            System.out.print(Integer.toBinaryString(b & 0xFF) + " ");
//        }
//        System.out.println("\n" + Arrays.toString(testPermutation));
//
//        System.out.print("Result (binary): ");
//        for (byte b : result) {
//            System.out.print(Integer.toBinaryString(b & 0xFF) + " ");
//        }
//
//    }
}

