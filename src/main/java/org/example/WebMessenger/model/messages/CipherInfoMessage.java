package org.example.WebMessenger.model.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.WebMessenger.model.client.CipherInfo;
import org.example.WebMessenger.model.client.ChatInfo;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CipherInfoMessage {
    private static final ObjectMapper mapper = new ObjectMapper();
    private String typeMessage = "cipher_info"; // без final
    private long anotherClientId;
    private String Algorithm;
    private String Padding;
    private String encryptionMode;
    private int sizeKeyInBits;
    private int sizeBlockInBits;
    private byte[] initializationVector;
    private byte[] publicKey;
    private byte[] p;
    private byte[] g;

    public CipherInfoMessage(long anotherClientId, CipherInfo cipherInfo, ChatInfo chatInfo) {
        this.anotherClientId = anotherClientId;
        this.Algorithm = cipherInfo.getAlgorithm();
        this.Padding = cipherInfo.getPadding();
        this.encryptionMode = cipherInfo.getEncryptionMode();
        this.sizeKeyInBits = cipherInfo.getSizeKeyInBits();
        this.sizeBlockInBits = cipherInfo.getSizeBlockInBits();
        this.initializationVector = cipherInfo.getInitializationVector();
        this.p = chatInfo.getP();
        this.g = chatInfo.getG();
    }

    public byte[] toBytes() {
        try {
            return mapper.writeValueAsString(this).getBytes();
        } catch (JsonProcessingException ex) {
            log.error("Error while processing message to json bytes");
        }

        return new byte[0];
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            log.error("Error while processing message to json bytes");
        }

        return "";
    }
}