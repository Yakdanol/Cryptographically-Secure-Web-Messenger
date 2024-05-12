import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestRC5 {
    @Test
    void test_1_inputBlock_32_bits() {
        int inputBlockSizeInBits = 32;
        int countRounds = 10;
        int lenKeyInBits = 32;
        byte[] key = {1, 2, 3, 4};
        byte[] inputBlock = {10, 11, 12, 13};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    @Test
    void test_1_inputBlock_64_bits() {
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
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    @Test
    void test_1_inputBlock_128_bits_key32() {
        int inputBlockSizeInBits = 128;
        int countRounds = 10;
        int lenKeyInBits = 32;
        byte[] key = {1, 2, 3, 4};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    @Test
    void test_2_inputBlock_128_bits_key64() {
        int inputBlockSizeInBits = 128;
        int countRounds = 10;
        int lenKeyInBits = 64;
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25,};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock));

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    @Test
    void test_2_inputBlock_128_bits_key48() {
        int inputBlockSizeInBits = 128;
        int countRounds = 10;
        int lenKeyInBits = 48;
        byte[] key = {1, 2, 3, 4, 5, 6};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25,};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock));

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    @Test
    void test_2_inputBlock_128_bits_key40_rounds100() {
        int inputBlockSizeInBits = 128;
        int countRounds = 100;
        int lenKeyInBits = 40;
        byte[] key = {1, 2, 3, 4, 5};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25,};

        RC5 rc5 = new RC5(inputBlockSizeInBits, countRounds, lenKeyInBits, key);
        byte[] result = rc5.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = rc5.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock));

        assertArrayEquals(inputBlock, resultDecrypt);
    }


}
