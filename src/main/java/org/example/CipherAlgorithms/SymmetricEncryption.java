package org.example.CipherAlgorithms;

import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
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

import java.io.File;
import java.io.IOException;

public class SymmetricEncryption {

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

    private EncryptionMode encryptionMode;
    private final Padding padding;
    private final CipherAlgorithms cipherAlgorithm;
    private byte[] initializationVector_IV;

    public SymmetricEncryption(EncryptionModes encryptionMode, PaddingMode paddingMode, CipherAlgorithms cipherAlgorithm, byte[] initializationVector_IV) {
        this.encryptionMode = switch (encryptionMode) {
            case ECB -> new ECB(cipherAlgorithm);
            case CBC -> new CBC(cipherAlgorithm, initializationVector_IV);
            case PCBC -> new PCBC(cipherAlgorithm, initializationVector_IV);
            case CFB -> new CFB(cipherAlgorithm, initializationVector_IV);
            case OFB -> new OFB(cipherAlgorithm, initializationVector_IV);
            case CTR -> new CTR(cipherAlgorithm, initializationVector_IV);
            case RANDOM_DELTA -> new RandomDelta(cipherAlgorithm, initializationVector_IV);
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

    public byte[] encrypt(byte[] textToEncrypt) {

        return new byte[0];
    }

    public byte[] decrypt(byte[] encryptedText) {

        return new byte[0];
    }


    public String encrypt(File fileForEncryption) throws IOException {

        return "";
    }

    public String decrypt(File fileForDecryption) throws IOException {

        return "";
    }

}
