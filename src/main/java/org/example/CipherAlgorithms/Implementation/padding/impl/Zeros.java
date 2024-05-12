package org.example.CipherAlgorithms.Implementation.padding.impl;

import org.example.CipherAlgorithms.Implementation.padding.Padding;

// ну надо бы исправить )
public class Zeros extends Padding {
    @Override
    protected byte[] getArrayPadding(byte countBytesPadding) {
        byte[] padding = new byte[countBytesPadding];

        for (int i = 0; i < padding.length - 1; i++) {
            padding[i] = 0;
        }

        padding[padding.length - 1] = countBytesPadding;
        return padding;
    }
}

