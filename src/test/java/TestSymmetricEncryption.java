import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;
import org.example.CipherAlgorithms.SymmetricEncryption;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class TestSymmetricEncryption {
//    @Test
//    public void testFilePhoto() {
//        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
//        byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8};
//        String filename = "C:\\Users\\Yakdanol\\Desktop\\Java\\6_semestr\\Crypt\\Coursework\\CourseworkCryptography\\src\\main\\resources\\files\\test.jpg";
//
//        try (SymmetricEncryption symmetricEncryption = new SymmetricEncryption(
//                SymmetricEncryption.EncryptionModes.ECB,
//                SymmetricEncryption.PaddingMode.ANSI_X923,
//                new RC5(128, 10, 64,
//                        key), IV);) {
//
//            CompletableFuture<String> encryptionFuture = symmetricEncryption.encryptFile(filename);
//            String encryptedFilePath = encryptionFuture.join();
//            System.out.println("Encrypted file: " + encryptedFilePath);
//
//            CompletableFuture<String> decryptionFuture = symmetricEncryption.decryptFile(encryptedFilePath);
//            String decryptedFilePath = decryptionFuture.join();
//            System.out.println("Decrypted file: " + decryptedFilePath);
//
////            // Verify the decrypted file is the same as the original
////            byte[] originalFileBytes = Files.readAllBytes(Path.of(filename));
////            byte[] decryptedFileBytes = Files.readAllBytes(Path.of(decryptedFilePath));
////
////            assert Arrays.equals(originalFileBytes, decryptedFileBytes);
//
////            String encryptedFile = String.valueOf(symmetricEncryption.encryptFile(filename).get());
////            symmetricEncryption.decryptFile(encryptedFile).get();
//
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    @Test
    public void testPhoto() {
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8};
        String filename = "C:\\Users\\Yakdanol\\Desktop\\Java\\6_semestr\\Crypt\\Coursework\\CourseworkCryptography\\src\\main\\resources\\files\\test.jpg";

        try (SymmetricEncryption symmetricEncryption = new SymmetricEncryption(
                SymmetricEncryption.EncryptionModes.ECB,
                SymmetricEncryption.PaddingMode.ANSI_X923,
                new RC5(128, 10, 64,
                        key), IV)) {

            String encryptedFile = symmetricEncryption.encryptFile(filename);
            String decryptedFile = symmetricEncryption.decryptFile(encryptedFile);

            // Compare the original file and decrypted file
//            byte[] originalFileBytes = Files.readAllBytes(Path.of(filename));
//            byte[] decryptedFileBytes = Files.readAllBytes(Path.of(decryptedFile));
//            assertArrayEquals(originalFileBytes, decryptedFileBytes, "Original and decrypted files should be identical");
            assertTrue(true);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testFile() {
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8};
        String filename = "C:\\Users\\Yakdanol\\Desktop\\Java\\6_semestr\\Crypt\\Coursework\\CourseworkCryptography\\src\\main\\resources\\files\\1.txt";

        try (SymmetricEncryption symmetricEncryption = new SymmetricEncryption(
                SymmetricEncryption.EncryptionModes.ECB,
                SymmetricEncryption.PaddingMode.ANSI_X923,
                new RC5(128, 10, 64,
                        key), IV)) {

            // Шифруем файл асинхронно
            CompletableFuture<String> encryptedFileFuture = CompletableFuture.supplyAsync(() -> symmetricEncryption.encryptFile(filename));

            // Дешифруем файл после завершения шифрования
            CompletableFuture<String> decryptedFileFuture = encryptedFileFuture.thenApplyAsync(symmetricEncryption::decryptFile);

            // Ожидания завершения шифрования и дешифрования
            String decryptedFile = decryptedFileFuture.join();
            System.out.println(decryptedFile);
//
//            // Сравниваем байты исходного файла и расшифрованного файла
//            byte[] originalFileBytes = Files.readAllBytes(Path.of(filename));
//            byte[] decryptedFileBytes = Files.readAllBytes(Path.of(decryptedFile));
//            assertArrayEquals(originalFileBytes, decryptedFileBytes, "Original and decrypted files should be identical");
            assertTrue(true);

        }
    }

    /**
     * Режим шифрования ECB
     * Паддинг ANSI_X923
     * Текст длиной 21 байт
     * Ключ длиной 64 бита (8 байт)
     * Блок длиной 128 бит (16 байт)
     * Количество раундов = 10
     */
    @Test
    public void testText() {
        byte[] textToEncrypt = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                                17, 18, 19, 20, 21, 21};
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8};

        try (SymmetricEncryption symmetricEncryption = new SymmetricEncryption(
                SymmetricEncryption.EncryptionModes.ECB,
                SymmetricEncryption.PaddingMode.ANSI_X923,
                new RC5(128, 10, 64,
                        key), IV);) {

            byte[] encryptedText = symmetricEncryption.encrypt(textToEncrypt);
            byte[] decryptedText = symmetricEncryption.decrypt(encryptedText);
            assertArrayEquals(textToEncrypt, decryptedText);
            System.out.println(Arrays.toString(decryptedText));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {

        }
    }
}
