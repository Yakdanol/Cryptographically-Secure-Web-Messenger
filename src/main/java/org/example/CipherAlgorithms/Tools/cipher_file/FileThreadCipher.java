package org.example.CipherAlgorithms.Tools.cipher_file;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface FileThreadCipher {
    String cipher(String pathToInputFile, String pathToOutputFile, boolean encryptOrDecrypt) throws IOException, ExecutionException, InterruptedException;
}
