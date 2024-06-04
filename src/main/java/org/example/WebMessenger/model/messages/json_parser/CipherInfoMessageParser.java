package org.example.WebMessenger.model.messages.json_parser;

import lombok.extern.slf4j.Slf4j;
import org.example.CipherAlgorithms.Implementation.algorithms.CipherAlgorithms;
import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;
import org.example.CipherAlgorithms.Implementation.algorithms.Serpent.Serpent;
import org.example.CipherAlgorithms.SymmetricEncryption;
import org.example.WebMessenger.model.messages.CipherInfoMessage;

import java.math.BigInteger;
import java.util.Arrays;

import static org.example.CipherAlgorithms.Tools.BinaryOperations.byteToIntArray;

@Slf4j
public class CipherInfoMessageParser {
    private static final String UNEXPECTED_VALUE = "Unexpected value: ";

    private CipherInfoMessageParser() {
    }

    public static SymmetricEncryption getCipher(CipherInfoMessage cipherInfo, BigInteger privateKey, BigInteger modulo) {
        byte[] key = getKey(cipherInfo.getPublicKey(), cipherInfo.getSizeKeyInBits(), privateKey, modulo);
        byte[] initializationVector = cipherInfo.getInitializationVector();

        log.info(Arrays.toString(key));

        CipherAlgorithms cipherAlgorithm = getCipherService(
                cipherInfo.getAlgorithm(),
                key,
                cipherInfo.getSizeKeyInBits(),
                cipherInfo.getSizeBlockInBits()
        );

        return new SymmetricEncryption(
                getEncryptionMode(cipherInfo.getEncryptionMode()),
                getPadding(cipherInfo.getPadding()),
                cipherAlgorithm,
                initializationVector
        );
    }

    public static byte[] getKey(byte[] publicKey, int sizeKeyInBits, BigInteger privateKey, BigInteger modulo) {
        BigInteger publicKeyNumber = new BigInteger(publicKey);
        BigInteger key = publicKeyNumber.modPow(privateKey, modulo);
        byte[] keyBytes = key.toByteArray();
        byte[] result = new byte[sizeKeyInBits / Byte.SIZE];
        System.arraycopy(keyBytes, 0, result, 0, sizeKeyInBits / Byte.SIZE);
        return result;
    }

    public static CipherAlgorithms getCipherService(String Algorithm, byte[] key, int sizeKeyInBits, int sizeBlockInBits) {
        return switch (Algorithm) {
            case "SERPENT" -> new Serpent(sizeKeyInBits, byteToIntArray(key));
            case "RC5" -> new RC5(sizeKeyInBits, sizeBlockInBits, 16, key);
            default -> throw new IllegalStateException(UNEXPECTED_VALUE + Algorithm);
        };
    }

    public static SymmetricEncryption.PaddingMode getPadding(String Padding) {
        return switch (Padding) {
            case "ANSI_X923" -> SymmetricEncryption.PaddingMode.ANSI_X923;
            case "ISO_10126" -> SymmetricEncryption.PaddingMode.ISO_10126;
            case "PKCS7" -> SymmetricEncryption.PaddingMode.PKCS7;
            case "ZEROS" -> SymmetricEncryption.PaddingMode.ZEROS;
            default -> throw new IllegalStateException(UNEXPECTED_VALUE + Padding);
        };
    }

    public static SymmetricEncryption.EncryptionModes getEncryptionMode(String encryptionMode) {
        return switch (encryptionMode) {
            case "CBC" -> SymmetricEncryption.EncryptionModes.CBC;
            case "CFB" -> SymmetricEncryption.EncryptionModes.CFB;
            case "CTR" -> SymmetricEncryption.EncryptionModes.CTR;
            case "ECB" -> SymmetricEncryption.EncryptionModes.ECB;
            case "OFB" -> SymmetricEncryption.EncryptionModes.OFB;
            case "PCBC" -> SymmetricEncryption.EncryptionModes.PCBC;
            case "RANDOM_DELTA" -> SymmetricEncryption.EncryptionModes.RANDOM_DELTA;
            default -> throw new IllegalStateException(UNEXPECTED_VALUE + encryptionMode);
        };
    }
}
