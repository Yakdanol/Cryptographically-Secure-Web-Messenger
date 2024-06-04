package org.example.CipherAlgorithms.Tools.cipher_file;

import org.example.CipherAlgorithms.Tools.cipher_file.FileThreadCipher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface FileThreadTaskCipher {
    byte[] apply(String pathToInputFile, long skipValue, long sizePartBytesThread, boolean encryptOrDecrypt) throws IOException, ExecutionException, InterruptedException;
}
