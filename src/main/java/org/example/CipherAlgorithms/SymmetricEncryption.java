package org.example.CipherAlgorithms;

import lombok.extern.slf4j.Slf4j;
import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Tools.cipher_file.FileThreadCipher;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.CBC.CBC;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.CFB.CFB;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.CTR.CTR;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.ECB.ECB;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.OFB.OFB;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.PCBC.PCBC;
import org.example.CipherAlgorithms.Implementation.encryption_mode.impl.RandomDelta.RandomDelta;
import org.example.CipherAlgorithms.Implementation.padding.Padding;
import org.example.CipherAlgorithms.Implementation.padding.impl.ANSI_X923;
import org.example.CipherAlgorithms.Implementation.padding.impl.Zeros;
import org.example.CipherAlgorithms.Implementation.padding.impl.ISO_10126;
import org.example.CipherAlgorithms.Implementation.padding.impl.PKCS7;
import org.example.CipherAlgorithms.Tools.cipher_file.impl.FileThreadCipherImpl;
import org.example.CipherAlgorithms.Tools.cipher_file.impl.FileThreadTaskCipherImpl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class SymmetricEncryption implements AutoCloseable {

    public enum EncryptionModes {
        ECB,
        CBC,
        PCBC,
        CFB,
        OFB,
        CTR,
        RANDOM_DELTA
    }

    public enum EncryptionAlgorithm {
        RC5,
        SERPENT
    }

    public enum PaddingMode {
        ZEROS,
        ANSI_X923,
        PKCS7,
        ISO_10126
    }

    private final ExecutorService executorService;
    private final EncryptionMode encryptionMode;
    private final Padding padding;
    private final CipherAlgorithms cipherAlgorithm;
    private final byte[] initializationVector_IV;
    private static final int BLOCK_SIZE = 1024;

    public SymmetricEncryption(EncryptionModes encryptionMode, PaddingMode paddingMode, CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

        this.encryptionMode = switch (encryptionMode) {
            case ECB -> new ECB(cipherAlgorithm, executorService);
            case CBC -> new CBC(cipherAlgorithm, initializationVector_IV, executorService);
            case PCBC -> new PCBC(cipherAlgorithm, initializationVector_IV, executorService);
            case CFB -> new CFB(cipherAlgorithm, initializationVector_IV, executorService);
            case OFB -> new OFB(cipherAlgorithm, initializationVector_IV);
            case CTR -> new CTR(cipherAlgorithm, initializationVector_IV, executorService);
            case RANDOM_DELTA -> new RandomDelta(cipherAlgorithm, initializationVector_IV, executorService);
        };

        this.padding = switch(paddingMode) {
            case ZEROS -> new Zeros();
            case ANSI_X923 -> new ANSI_X923();
            case PKCS7 -> new PKCS7();
            case ISO_10126 -> new ISO_10126();
        };

        this.cipherAlgorithm = cipherAlgorithm;
        this.initializationVector_IV = initializationVector_IV;
    }

//    public CompletableFuture<byte[]> encrypt(byte[] textToEncrypt) {
//        log.info("Starting encryption");
//        return CompletableFuture.supplyAsync(() ->
//                encryptionMode.encrypt(padding.addPadding(textToEncrypt, cipherAlgorithm.getBlockSize())));
//    }

    public byte[] encrypt(byte[] textToEncrypt) throws ExecutionException, InterruptedException {
        log.info("Starting encrypt byte text");
        try {
            return encryptionMode.encrypt(padding.addPadding(textToEncrypt, cipherAlgorithm.getBlockSize()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(Arrays.deepToString(ex.getStackTrace()));
        }

        return new byte[0];
    }

//    public CompletableFuture<byte[]> decrypt(byte[] textToDecrypt) {
//        log.info("Starting decryption");
//        return CompletableFuture.supplyAsync(() ->
//                padding.removePadding(encryptionMode.decrypt(textToDecrypt)));
//    }

    public byte[] decrypt(byte[] textToDecrypt) throws ExecutionException, InterruptedException {
        log.info("Starting decrypt byte text");
        try {
            return padding.removePadding(encryptionMode.decrypt(textToDecrypt));
        }
//        } catch (InterruptedException ex) {
//            log.error(ex.getMessage());
//            log.error(Arrays.deepToString(ex.getStackTrace()));
//            Thread.currentThread().interrupt();
//        }
        catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(Arrays.deepToString(ex.getStackTrace()));
        }

        return new byte[0];
    }

//    public CompletableFuture<String> encryptFile(String fileForEncryption) throws IOException {
//        log.info("Starting File encryption");
//        return asyncProcess(fileForEncryption, true);
//    }

    public String encryptFile(String pathToInputFile) {
        String encryptFile = null;
        int sizeBlockBytes = cipherAlgorithm.getBlockSize();

        try {
            String fileWithPadding = padding.addPadding(pathToInputFile, sizeBlockBytes);
            encryptFile = new FileThreadCipherImpl(
                    sizeBlockBytes,
                    new FileThreadTaskCipherImpl(encryptionMode)
            ).cipher(fileWithPadding, addPostfixToFileName(pathToInputFile, "_enc"), true);

            Files.delete(Path.of(fileWithPadding));
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
            log.error(Arrays.toString(ex.getStackTrace()));
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(Arrays.toString(ex.getStackTrace()));
        }

        return encryptFile;
    }

//    public CompletableFuture<String> decryptFile(String fileForDecryption) throws IOException {
//        log.info("Starting File decryption");
//        return asyncProcess(fileForDecryption, false);
//    }

    public String decryptFile(String pathToInputFile) {
        String decryptFile = null;
        int sizeBlockBytes = cipherAlgorithm.getBlockSize();

        try {
            decryptFile = new FileThreadCipherImpl(
                    sizeBlockBytes,
                    new FileThreadTaskCipherImpl(encryptionMode)
            ).cipher(pathToInputFile, addPostfixToFileName(pathToInputFile, "_dec"), false);
            String removePaddingFile = padding.removePadding(decryptFile);

            if (!(new File(removePaddingFile).renameTo(new File(decryptFile)))) {
                log.error("Error while renaming file");
            }
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
            log.error(Arrays.toString(ex.getStackTrace()));
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(Arrays.toString(ex.getStackTrace()));
        }

        return decryptFile;
    }

    private String addPostfixToFileName(String pathToInputFile, String postfix) {
        log.info("Starting add Postfix to file");
        int dotIndex = pathToInputFile.lastIndexOf('.');
        String baseName = pathToInputFile.substring(0, dotIndex);
        String extension = pathToInputFile.substring(dotIndex);
        return baseName + postfix + extension;
    }

//    private CompletableFuture<String> asyncProcess(String inputFile, boolean encryptOrDecrypt) {
//        if (inputFile == null) {
//            throw new RuntimeException("Input file is null");
//        }
//
//        try {
//            File file = new File(inputFile);
//            if (!file.exists()) {
//                throw new FileNotFoundException(inputFile);
//            }
//            long fileLength = file.length();
//
//            String outputFile;
//            if (encryptOrDecrypt) {
//                outputFile = padding.addPadding(inputFile, cipherAlgorithm.getBlockSize());
//            } else {
//                outputFile = padding.removePadding(inputFile);
//            }
//
//            return CompletableFuture
//                    .supplyAsync(() -> processFile(inputFile, outputFile, fileLength, encryptOrDecrypt));
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private String processFile(String inputFile, String outputFile, long fileLength, boolean encryptOrDecrypt) {
//        log.info("Start processing file");
//
//        if (inputFile == null || outputFile == null) {
//            throw new RuntimeException("Input and output files cannot be null");
//        }
//
//        List<Future<?>> futures = new ArrayList<>();
//        for (var readBytes = 0L; readBytes < fileLength; readBytes += BLOCK_SIZE) {
//            final long finalReadBytes = readBytes;
//
//            futures.add(executorService.submit(() -> {
//                byte[] block = readBlock(inputFile, finalReadBytes, fileLength);
//
//                if (encryptOrDecrypt) {
//                    block = encryptionMode.encrypt(block);
//                } else {
//                    block = encryptionMode.decrypt(block);
//                }
//
//                writeFile(outputFile, block, finalReadBytes);
//            }));
//        }
//
//        for (var future : futures) {
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException ex) {
//                log.error("Error in processing block: " + ex.getMessage());
//                throw new RuntimeException(ex);
//            }
//        }
//
//        log.info("Finished processing file");
//        return outputFile;
//    }
//
//    private byte[] readBlock(String inputFile, long offset, long fileLength) {
//        log.info("Start reading block: " + offset);
//        try (RandomAccessFile file = new RandomAccessFile(inputFile, "r")) {
//            file.seek(offset);
//            int bytesRead = 0;
//
//            long unreadBytes = fileLength - offset;
//            int arrayLength = (int) (unreadBytes < BLOCK_SIZE ? unreadBytes : BLOCK_SIZE);
//
//            byte[] bytes = new byte[arrayLength];
//
//            while (bytesRead < BLOCK_SIZE && file.getFilePointer() < fileLength) {
//                bytes[bytesRead++] = file.readByte();
//            }
//
//            return bytes;
//        } catch (IOException ex) {
//            log.error("Error reading block at offset " + offset + ": " + ex.getMessage());
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private void writeFile(String outputFile, byte[] bytes, long offset) {
//        log.info("Start writing file: " + offset);
//        try (RandomAccessFile output = new RandomAccessFile(outputFile, "rw")) {
//            output.seek(offset);
//            for (var value : bytes) {
//                output.write(value);
//            }
//        } catch (IOException ex) {
//            log.error("Error writing block at offset " + offset + ": " + ex.getMessage());
//            throw new RuntimeException(ex);
//        }
//    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
        }
    }
}
