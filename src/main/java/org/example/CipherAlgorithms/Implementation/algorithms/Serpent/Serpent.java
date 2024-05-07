package org.example.CipherAlgorithms.Implementation.algorithms.Serpent;

import lombok.extern.slf4j.Slf4j;
import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;

@Slf4j
public class Serpent implements CipherAlgorithms {
    @Override
    public int getBlockSize() {

        return 0;
    }

    @Override
    public byte[] encryptBlock(byte[] inputBlock) {


        return new byte[0];
    }

    @Override
    public byte[] decryptBlock(byte[] inputBlock) {


        return new byte[0];
    }
}
