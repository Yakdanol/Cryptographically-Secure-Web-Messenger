package org.example.CipherAlgorithms.Implementation.encryption_mode.impl.ECB;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// todo Надо тестировать Buffer, даст ли он ускорение по скорости
public class ECB implements EncryptionMode, AutoCloseable {
    private final CipherAlgorithms cipherAlgorithm;
    private final ExecutorService executorService;
    private final ThreadLocal<byte[]> threadLocalBuffer;

    public ECB(CipherAlgorithms cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
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
        byte[] result = new byte[text.length];
        int blockLength = cipherAlgorithm.getBlockSize();
        int countBlocks = text.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(countBlocks);

        for (int i = 0; i < countBlocks; ++i) {
            final int index = i;
            
            futures.add(executorService.submit(() -> {
                int startIndex = index * blockLength;
                byte[] block = new byte[blockLength];
                // byte[] block = threadLocalBuffer.get(); // оптимизация с помощью буфера
                System.arraycopy(text, startIndex, block, 0, blockLength);
                block = encryptOrDecrypt ?
                        cipherAlgorithm.encryptBlock(block) :
                        cipherAlgorithm.decryptBlock(block);
                System.arraycopy(block, 0, result, startIndex, block.length);
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
            //threadLocalBuffer.remove();
        }
    }
}
