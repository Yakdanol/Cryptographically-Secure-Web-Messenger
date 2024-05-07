package org.example.CipherAlgorithms.Implementation.algorithms;

import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;

import java.util.Arrays;

public class TestEncryption {
    public static void main(String[] args) {
        int inputBlockSizeInBits = 64;
        int countRounds = 10;
        int lenKeyInBits = 32;
        byte[] key = {1, 2, 3, 4};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock));
    }
}
