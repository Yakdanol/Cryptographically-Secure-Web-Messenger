package org.example.CipherAlgorithms.Implementation.encryption_mode.impl.CTR;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;
import org.example.CipherAlgorithms.Tools.BinaryOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// todo Надо тестировать Buffer, даст ли он ускорение по скорости
public class CTR implements EncryptionMode, AutoCloseable {
    private final CipherAlgorithms cipherAlgorithm;
    private final byte[] IV;
    private final ExecutorService executorService;
    private final ThreadLocal<byte[]> threadLocalBuffer;

    public CTR(CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV) {
        this.cipherAlgorithm = cipherAlgorithm;
        this.IV = initializationVector_IV;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        this.threadLocalBuffer = ThreadLocal.withInitial(() -> new byte[cipherAlgorithm.getBlockSize()]);
    }

    @Override
    public byte[] encrypt(byte[] text) {
        return multiprocessingText(text);
    }

    @Override
    public byte[] decrypt(byte[] text) {
        return multiprocessingText(text);
    }

    private byte[] multiprocessingText(byte[] text) {
        int blockLength = cipherAlgorithm.getBlockSize();
        byte[] result = new byte[text.length];
        int countBlocks = text.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(countBlocks);

        for (int i = 0; i < countBlocks; ++i) {
            final int index = i;

            futures.add(executorService.submit(() -> {
                int startIndex = index * blockLength;
                byte[] block = new byte[blockLength];
                // byte[] block = threadLocalBuffer.get();
                System.arraycopy(text, startIndex, block, 0, blockLength);

                byte[] blockForProcess = new byte[blockLength];
                // byte[] blockForProcess = threadLocalBuffer.get();
                int length = blockLength - Integer.BYTES;
                System.arraycopy(IV, 0, blockForProcess, 0, length);

                byte[] counterInBytes = new byte[Integer.BYTES];
                for (int j = 0; j < counterInBytes.length; ++j) {
                    counterInBytes[j] = (byte) (index >> (3 - j) * 8);
                }
                System.arraycopy(counterInBytes, 0, blockForProcess, length, counterInBytes.length);

                byte[] encryptedBlock = BinaryOperations.xor(block, cipherAlgorithm.encryptBlock(blockForProcess));
                System.arraycopy(encryptedBlock, 0, result, startIndex, encryptedBlock.length);
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

