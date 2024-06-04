package org.example.CipherAlgorithms.Tools.cipher_file.impl;

import lombok.AllArgsConstructor;
import org.example.CipherAlgorithms.Tools.cipher_file.FileThreadCipher;
import org.example.CipherAlgorithms.Tools.cipher_file.FileThreadTaskCipher;
import org.example.CipherAlgorithms.Implementation.encryption_mode.EncryptionMode;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
public class FileThreadTaskCipherImpl implements FileThreadTaskCipher {
    private EncryptionMode encryptionMode;

    @Override
    public byte[] apply(String pathToInputFile, long skipValue, long sizePartBytesThread, boolean encryptOrDecrypt) throws IOException, ExecutionException, InterruptedException {
        byte[] text = new byte[(int) sizePartBytesThread];

        try (RandomAccessFile file = new RandomAccessFile(pathToInputFile, "r")) {
            file.seek(skipValue);
            int countBytes = file.read(text);

            if (countBytes != sizePartBytesThread) {
                byte[] trimText = new byte[countBytes];
                System.arraycopy(text, 0, trimText, 0, countBytes);
                text = trimText;
            }
        } catch (IOException ex) {
            throw new IOException(ex);
        }

        if (encryptOrDecrypt) {
            return encryptionMode.encrypt(text);
        } else {
            return encryptionMode.decrypt(text);
        }
    }
}
