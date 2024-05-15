package org.example.CipherAlgorithms.Implementation.encryption_mode.impl.PCBC;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;
import org.example.CipherAlgorithms.Tools.BinaryOperations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// todo Надо тестировать Buffer, даст ли он ускорение по скорости
public class PCBC implements EncryptionMode, AutoCloseable {
    private final CipherAlgorithms cipherAlgorithm;
    private final byte[] IV;
    private final ExecutorService executorService;
    private final ThreadLocal<byte[]> threadLocalBuffer;

    public PCBC(CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV) {
        this.cipherAlgorithm = cipherAlgorithm;
        this.IV = initializationVector_IV;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        this.threadLocalBuffer = ThreadLocal.withInitial(() -> new byte[cipherAlgorithm.getBlockSize()]);
    }

    @Override
    public byte[] encrypt(byte[] text) {
        return multiprocessingText(text, true);
    }

    @Override
    public byte[] decrypt(byte[] text) {
        return multiprocessingText(text, false);
    }

    private byte[] multiprocessingText(byte[] text, boolean encryptOrDecrypt) {
        int blockLength = cipherAlgorithm.getBlockSize();
        byte[] result = new byte[text.length];
        byte[] blockForXor = IV;
        int length = text.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            // byte[] block = threadLocalBuffer.get();
            System.arraycopy(text, startIndex, block, 0, blockLength);

            byte[] encryptedOrDecryptedBlock = encryptOrDecrypt ?
                    cipherAlgorithm.encryptBlock(BinaryOperations.xor(block, blockForXor)) : 
                    BinaryOperations.xor(blockForXor, cipherAlgorithm.decryptBlock(block));
            System.arraycopy(encryptedOrDecryptedBlock, 0, result, startIndex, encryptedOrDecryptedBlock.length);
            blockForXor = BinaryOperations.xor(encryptedOrDecryptedBlock, block);
        }

        return result;
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        } finally {
            // threadLocalBuffer.remove();
        }
    }
}

