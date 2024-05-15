package org.example.CipherAlgorithms.Implementation.encryption_mode.impl.OFB;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;
import org.example.CipherAlgorithms.Tools.BinaryOperations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// todo Надо тестировать Buffer, даст ли он ускорение по скорости
public class OFB implements EncryptionMode {
    private final CipherAlgorithms cipherAlgorithm;
    private final byte[] IV;
    private final ThreadLocal<byte[]> threadLocalBuffer;

    public OFB(CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV) {
        this.cipherAlgorithm = cipherAlgorithm;
        this.IV = initializationVector_IV;
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
        byte[] previousBlock = IV;
        int length = text.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            // byte[] block = threadLocalBuffer.get();
            System.arraycopy(text, startIndex, block, 0, blockLength);

            byte[] encryptedPart = cipherAlgorithm.encryptBlock(previousBlock);
            byte[] encryptedOrDecryptedBlock = BinaryOperations.xor(block, encryptedPart);

            System.arraycopy(encryptedOrDecryptedBlock, 0, result, startIndex, encryptedOrDecryptedBlock.length);
            previousBlock = encryptedPart;
        }

        return result;
    }
}
