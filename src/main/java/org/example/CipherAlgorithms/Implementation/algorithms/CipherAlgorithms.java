package org.example.CipherAlgorithms.Implementation.algorithms;

public interface CipherAlgorithms {
    int getBlockSize();

    byte[] encryptBlock(byte[] inputBlock);

    byte[] decryptBlock(byte[] inputBlock);
}
