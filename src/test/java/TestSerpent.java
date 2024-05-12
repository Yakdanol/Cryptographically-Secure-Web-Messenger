import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;
import org.example.CipherAlgorithms.Implementation.algorithms.Serpent.Serpent;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestSerpent {
    // все шифрующие блоки по 128 бит
    /**
     * Тест: ключ длиной 128 бит
     */
    @Test
    void test_1_key_128_bits() {
        int[] key = {1, 2, 3, 4};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25};

        Serpent serpent = new Serpent(128, key);
        byte[] result = serpent.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = serpent.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    /**
     * Тест: ключ длиной 196 бит
     */
    @Test
    void test_2_key_192_bits() {
        int[] key = {1, 2, 3, 4, 5, 6};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25};

        Serpent serpent = new Serpent(128, key);
        byte[] result = serpent.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = serpent.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }

    /**
     * Тест: ключ длиной 256 бит
     */
    @Test
    void test_3_key_256_bits() {
        int[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] inputBlock = {10, 11, 12, 13, 14, 15, 16, 17,
                             18, 19, 20, 21, 22, 23, 24, 25};

        Serpent serpent = new Serpent(128, key);
        byte[] result = serpent.encryptBlock(inputBlock);
        System.out.println("Result encryption: " + Arrays.toString(result));

        byte[] resultDecrypt = serpent.decryptBlock(result);
        System.out.println("Result decryption: " + Arrays.toString(resultDecrypt));
        System.out.println("Original array: " + Arrays.toString(inputBlock) + "\n");

        assertArrayEquals(inputBlock, resultDecrypt);
    }
}
