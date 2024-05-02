package org.example.CipherAlgorithms.Implementation.encryption_mode;

public interface EncryptionMode {
        byte[] encrypt(byte[] text);

        byte[] decrypt(byte[] text);
}
