package org.example.WebMessenger.service;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.example.DiffieHellman.DiffieHellman;
import org.example.WebMessenger.kafka.KafkaWriter;
import org.example.WebMessenger.model.client.CipherInfo;
import org.example.WebMessenger.model.client.ClientInfo;
import org.example.WebMessenger.model.client.MessageInfo;
import org.example.WebMessenger.model.client.ChatInfo;
import org.example.WebMessenger.model.messages.CipherInfoMessage;
import org.example.WebMessenger.model.messages.Message;
import org.example.WebMessenger.repository.CipherInfoRepository;
import org.example.WebMessenger.repository.ClientRepository;
import org.example.WebMessenger.repository.MessageInfoRepository;
import org.example.WebMessenger.repository.ChatRepository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class Server {
    private static final Random RANDOM = new Random();
    private static final Map<String, UI> openWindows = new HashMap<>();
    private static final Map<Long, Pair<Long, Long>> chatConnections = new HashMap<>();
    private final CipherInfoRepository cipherInfoRepository;
    private final ClientRepository clientRepository;
    private final ChatRepository chatRepository;
    private final KafkaWriter kafkaWriter;
    private final MessageInfoRepository messageInfoRepository;

    public Server(MessageInfoRepository messageInfoRepository, CipherInfoRepository cipherInfoRepository, ClientRepository clientRepository, ChatRepository chatRepository, KafkaWriter kafkaWriter) {
        this.messageInfoRepository = messageInfoRepository;
        this.cipherInfoRepository = cipherInfoRepository;
        this.clientRepository = clientRepository;
        this.chatRepository = chatRepository;
        this.kafkaWriter = kafkaWriter;
    }

    public synchronized ClientInfo authorization(String name, long password, String Algorithm) {
        CipherInfo cipherInfo = cipherInfoRepository.save(getCipherInfo(Algorithm, "ANSI_X923", "ECB"));

        return clientRepository.save(ClientInfo.builder()
                .name(name)
                .password(password)
                .idCipherInfo(cipherInfo.getId())
                .chats(new long[0])
                .build()
        );
    }

    public synchronized boolean connectToChat(long clientId, long chatId, String Algorithm, String Padding, String encryptionMode) {
        Optional<ClientInfo> clientInfoOptional = clientRepository.findById(clientId);
        //CipherInfo cipherInfo = cipherInfoRepository.save(getCipherInfo(Algorithm, Padding, encryptionMode));

        if (clientInfoOptional.isPresent()) {
            if (chatConnections.containsKey(chatId)) {
                Pair<Long, Long> chat = chatConnections.get(chatId);

                if ((chat.getLeft() == null || chat.getRight() == null) &&
                        !((chat.getLeft() != null && chat.getLeft() == clientId) || (chat.getRight() != null && chat.getRight() == clientId))) {
                    Long anotherClientId = chat.getLeft() == null ? chat.getRight() : chat.getLeft();
                    chatConnections.put(chatId, Pair.of(clientId, anotherClientId));

                    ClientInfo updateClient = clientRepository.addChat(clientId, chatId);

                    if (updateClient == null) {
                        return false;
                    }

                    return startChat(clientId, anotherClientId, chatId);
                }

                return false;
            } else {
                if (!chatRepository.existsChatInfoByChatId(chatId)) {
                    BigInteger[] chatParameters = DiffieHellman.generateParameters(300);

                    byte[] p = chatParameters[0].toByteArray();
                    byte[] g = chatParameters[1].toByteArray();

                    chatRepository.save(
                            ChatInfo.builder()
                                    .chatId(chatId)
                                    .p(p)
                                    .g(g)
                                    .build()
                    );
                }

                //ClientInfo client = clientRepository.findById(1L);

                chatConnections.put(chatId, Pair.of(clientId, null));
                clientRepository.addChat(clientId, chatId);

                return true;
            }
        }

        return false;
    }

    public synchronized void disconnectFromChat(long clientId, long chatId) {
        if (chatConnections.containsKey(chatId)) {
            log.info("trying to disconnect");
            Pair<Long, Long> chat = chatConnections.get(chatId);

            if (chat.getLeft() != null && chat.getLeft() == clientId) {
                chatConnections.put(chatId, Pair.of(null, chat.getRight()));
            } else if (chat.getRight() != null && chat.getRight() == clientId) {
                chatConnections.put(chatId, Pair.of(chat.getLeft(), null));
            } else {
                return;
            }

            Pair<Long, Long> updatedChat = chatConnections.get(chatId);

            if (updatedChat.getLeft() == null && updatedChat.getRight() == null) {
                log.info("remove chatId");
                chatConnections.remove(chatId);
            }

            String url = "chat/" + clientId + "/" + chatId;

            UI ui = openWindows.get(url);

            if (ui != null) {
                ui.getPage().executeJs("window.close()");
                removeWindow(url);
            }

            clientRepository.removeChat(clientId, chatId);
        }
    }

    private boolean startChat(long firstClientId, long secondClientId, long chatId) {
        log.info("Start chat...");
        CipherInfo firstCipherInfo = getCipherInfoById(firstClientId);
        CipherInfo secondCipherInfo = getCipherInfoById(secondClientId);
        ChatInfo chatInfo = getChatInfoById(chatId);

        String outputTopicFirst = "input_" + secondClientId + "_" + chatId;
        String outputTopicSecond = "input_" + firstClientId + "_" + chatId;

        if (firstCipherInfo != null && secondCipherInfo != null && chatInfo != null) {
            CipherInfoMessage firstMessage = new CipherInfoMessage(firstClientId, firstCipherInfo, chatInfo);
            CipherInfoMessage secondMessage = new CipherInfoMessage(secondClientId, secondCipherInfo, chatInfo);

            kafkaWriter.processing(firstMessage.toBytes(), outputTopicFirst);
            kafkaWriter.processing(secondMessage.toBytes(), outputTopicSecond);

            return true;
        }

        return false;
    }

    public boolean notExistClient(long clientId) {
        return !clientRepository.existsById(clientId);
    }

    public synchronized void saveMessage(long from, long to, Message message) {
        messageInfoRepository.save(MessageInfo.builder()
                .from(from)
                .to(to)
                .message(message)
                .build());
    }

    public CipherInfoMessage getCipherInfoMessageClient(long clientId, long chatId) {
        CipherInfo cipherInfo = getCipherInfoById(clientId);
        ChatInfo chatInfo = getChatInfoById(chatId);

        if (cipherInfo != null && chatInfo != null) {
            return new CipherInfoMessage(clientId, cipherInfo, chatInfo);
        }

        return null;
    }

    public CipherInfo getCipherInfoById(long clientId) {
        Optional<ClientInfo> clientInfoOptional = clientRepository.findById(clientId);

        if (clientInfoOptional.isPresent()) {
            ClientInfo clientInfo = clientInfoOptional.get();

            return cipherInfoRepository.findById(clientInfo.getIdCipherInfo()).orElse(null);
        }

        return null;
    }

    public ChatInfo getChatInfoById(long chatId) {
        return chatRepository.getChatInfoByChatId(chatId).orElse(null);
    }

    private CipherInfo getCipherInfo(String Algorithm, String Padding, String encryptionMode) {


        return switch (Algorithm) {
            case "RC5" -> CipherInfo.builder()
                    .Algorithm("RC5")
                    .Padding(Padding)
                    .encryptionMode(encryptionMode)
                    .sizeKeyInBits(64)
                    .sizeBlockInBits(64)
                    .initializationVector(generateInitVector(8))
                    .build();
            case "SERPENT" -> CipherInfo.builder()
                    .Algorithm("SERPENT")
                    .Padding(Padding)
                    .encryptionMode(encryptionMode)
                    .sizeKeyInBits(128)
                    .sizeBlockInBits(128)
                    .initializationVector(generateInitVector(16))
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + Algorithm);
        };
    }

    private byte[] generateInitVector(int size) {
        byte[] vector = new byte[size];

        for (int i = 0; i < size; i++) {
            vector[i] = (byte) RANDOM.nextInt(128);
        }

        return vector;
    }

    public synchronized void addWindow(String url, UI ui) {
        openWindows.put(url, ui);
    }

    public synchronized void removeWindow(String url) {
        openWindows.remove(url);
    }

    public synchronized boolean isNotOpenWindow(String url) {
        return !openWindows.containsKey(url);
    }
}
