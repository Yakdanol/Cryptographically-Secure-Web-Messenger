package org.example.CipherAlgorithms.Implementation.encryption_mode.impl.CFB;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;
import org.example.CipherAlgorithms.Tools.BinaryOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// todo Надо тестировать Buffer, даст ли он ускорение по скорости
public class CFB implements EncryptionMode, AutoCloseable {
    private final CipherAlgorithms cipherAlgorithm;
    private final byte[] IV;
    private final ExecutorService executorService;
    private final ThreadLocal<byte[]> threadLocalBuffer;

    public CFB(CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV, ExecutorService executorService) {
        this.cipherAlgorithm = cipherAlgorithm;
        this.IV = initializationVector_IV;
        this.executorService = executorService;
        this.threadLocalBuffer = ThreadLocal.withInitial(() -> new byte[cipherAlgorithm.getBlockSize()]);
    }
    
    @Override
    public byte[] encrypt(byte[] text) {
        int blockLength = cipherAlgorithm.getBlockSize();
        byte[] result = new byte[text.length];
        byte[] previousBlock = IV;
        int length = text.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            // byte[] block = threadLocalBuffer.get();
            System.arraycopy(text, startIndex, block, 0, blockLength);

            // XOR результата шифрования и открытого текста
            byte[] encryptedBlock = BinaryOperations.xor(block, cipherAlgorithm.encryptBlock(previousBlock));
            System.arraycopy(encryptedBlock, 0, result, startIndex, encryptedBlock.length);
            previousBlock = encryptedBlock;
        }

        return result;
    }

    @Override
    public byte[] decrypt(byte[] text) {
        int blockLength = cipherAlgorithm.getBlockSize();
        byte[] result = new byte[text.length];
        int countBlocks = text.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(countBlocks);

        for (int i = 0; i < countBlocks; ++i) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                byte[] previousBlock = (index == 0) ? IV : new byte[blockLength]; // threadLocalBuffer.get()
                if (index != 0) {
                    System.arraycopy(text, (index - 1) * blockLength, previousBlock, 0, blockLength);
                }

                int startIndex = index * blockLength;
                byte[] currentBlock = new byte[blockLength];
                // byte[] currentBlock = threadLocalBuffer.get();
                System.arraycopy(text, startIndex, currentBlock, 0, blockLength);

                // XOR с предыдущим зашифрованным блоком
                byte[] decryptedBlock = BinaryOperations.xor(currentBlock, cipherAlgorithm.decryptBlock(previousBlock));
                System.arraycopy(decryptedBlock, 0, result, startIndex, decryptedBlock.length);
            }));
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
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
