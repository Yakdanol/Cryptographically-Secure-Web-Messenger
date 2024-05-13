package org.example.CipherAlgorithms;

import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;
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
import java.util.concurrent.CompletableFuture;

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
    private byte[] initializationVector_IV;

    public SymmetricEncryption(EncryptionModes encryptionMode, PaddingMode paddingMode, byte[] initializationVector_IV) {
        this.encryptionMode = switch (encryptionMode) {
            case ECB -> new ECB();
            case CBC -> new CBC();
            case PCBC -> new PCBC();
            case CFB -> new CFB();
            case OFB -> new OFB();
            case CTR -> new CTR();
            case RANDOM_DELTA -> new RandomDelta();
        };

        this.padding = switch(paddingMode) {
            case ZEROS -> new Zeros();
            case ANSI_X923 -> new ANSI_X923();
            case PKCS7 -> new PKCS7();
            case ISO_10126 -> new ISO_10126();
        };

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
