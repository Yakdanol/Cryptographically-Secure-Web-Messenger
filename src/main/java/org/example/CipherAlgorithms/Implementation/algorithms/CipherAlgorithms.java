package org.example.CipherAlgorithms.Implementation.algorithms;

public interface CipherAlgorithms {
    byte[] encryptBlock(byte[] inputBlock);

    byte[] decryptBlock(byte[] inputBlock);



}

