package org.example.WebMessenger.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.example.CipherAlgorithms.SymmetricEncryption;
import org.example.WebMessenger.kafka.KafkaWriter;
import org.example.WebMessenger.model.messages.CipherInfoMessage;
import org.example.WebMessenger.model.messages.KeyMessage;
import org.example.WebMessenger.model.messages.Message;
import org.example.WebMessenger.model.messages.json_parser.CipherInfoMessageParser;
import org.example.WebMessenger.model.messages.json_parser.MessageParser;
import org.example.WebMessenger.service.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Route("chat")
public class Chat extends VerticalLayout implements HasUrlParameter<String> {
    private final Server server;
    private long clientId;
    private long chatId;
    private final KafkaWriter kafkaWriter;
    private String outputTopic;
    private volatile SymmetricEncryption cipherEncrypt;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private MessagesLayoutWrapper messagesLayoutWrapper;
    private long anotherClientId;
    private final Backend backend;

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        log.info("set parameter");

        String[] params = parameter.split("/");

        clientId = Long.parseLong(params[0]);
        chatId = Long.parseLong(params[1]);

        if (server.notExistClient(clientId)) {
            Notification.show("User not found");
            setEnabled(false);
        } else {
            service.submit(backend::startKafka);
            server.addWindow("chat/" + clientId + "/" + chatId, event.getUI());
        }
    }

    public Chat(Server server, KafkaWriter kafkaWriter) {
        this.server = server;
        this.kafkaWriter = kafkaWriter;
        this.outputTopic = null;
        this.cipherEncrypt = null;
        new Frontend().setPage();
        this.backend = new Backend();
    }

    // Выход из чата
    @Override
    protected void onDetach(DetachEvent event) {
        server.disconnectFromChat(clientId, chatId);

        if (outputTopic != null) {
            kafkaWriter.processing(new Message("disconnect", null, null, 0, null).toBytes(), outputTopic);
        }

        server.disconnectFromChat(clientId, chatId);
        backend.close();
        service.shutdown();

        try {
            if (!service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("end service");
    }

    public class MessagesLayoutWrapper {
        private final VerticalLayout messagesLayout;
        private final KafkaWriter kafkaWriter;

        public enum Destination {
            OWN,
            ANOTHER
        }

        public MessagesLayoutWrapper(VerticalLayout messagesLayout, KafkaWriter kafkaWriter) {
            this.messagesLayout = messagesLayout;
            this.kafkaWriter = kafkaWriter;
        }

        public void showTextMessage(String textMessage, Destination destination) {
            Optional<UI> uiOptional = getUI();

            if (uiOptional.isPresent()) {
                UI ui = uiOptional.get();

                ui.access(() -> {
                    Div messageDiv = new Div();
                    messageDiv.setText(textMessage);

                    String timeReceived = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    Span timeSpan = new Span(timeReceived);
                    timeSpan.getStyle().set("font-size", "small").set("color", "gray");

                    if (destination.equals(Destination.OWN)) {
                        messageDiv.getStyle()
                                .set("margin-left", "auto")
                                .set("background-color", "#cceeff");
                        timeSpan.getStyle().set("margin-left", "auto");

                        setPossibilityToDelete(messagesLayout, messageDiv);
                    } else {
                        messageDiv.getStyle()
                                .set("margin-right", "auto")
                                .set("background-color", "#f2f2f2");
                        timeSpan.getStyle().set("margin-right", "auto");
                    }

                    messageDiv.getStyle()
                            .set("border-radius", "5px")
                            .set("padding", "10px")
                            .set("border", "1px solid #ddd");

//                    VerticalLayout messageLayout = new VerticalLayout(timeSpan, messageDiv);
//                    messagesLayout.add(messageLayout);

                    messagesLayout.add(timeSpan, messageDiv); // без времени
                    messagesLayout.getElement().executeJs("this.scrollTo(0, this.scrollHeight);");
                });
            }
        }

        public void showImageMessage(String nameFile, byte[] data, Destination destination) {
            Optional<UI> uiOptional = getUI();

            if (uiOptional.isPresent()) {
                UI ui = uiOptional.get();

                ui.access(() -> {
                    Div imageDiv = new Div();
                    String timeReceived = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    Span timeSpan = new Span(timeReceived);
                    timeSpan.getStyle().set("font-size", "small").set("color", "gray");

                    StreamResource resource = new StreamResource(nameFile, () -> new ByteArrayInputStream(data));
                    Image image = new Image(resource, "Uploaded image");

                    imageDiv.add(image);

                    if (destination.equals(Destination.OWN)) {
                        imageDiv.getStyle()
                                .set("margin-left", "auto")
                                .set("background-color", "#cceeff");
                        setPossibilityToDelete(messagesLayout, imageDiv);
                        timeSpan.getStyle().set("margin-left", "auto");
                    } else {
                        imageDiv.getStyle()
                                .set("margin-right", "auto")
                                .set("background-color", "#f2f2f2");
                        timeSpan.getStyle().set("margin-right", "auto");
                    }

                    imageDiv.getStyle()
                            .set("overflow", "hidden")
                            .set("padding", "10px")
                            .set("border-radius", "5px")
                            .set("border", "1px solid #ddd")
                            .set("width", "60%")
                            .set("flex-shrink", "0");

                    image.getStyle()
                            .set("width", "100%")
                            .set("height", "100%");

//                    VerticalLayout messageLayout = new VerticalLayout(timeSpan, imageDiv);
//                    messagesLayout.add(messageLayout);

                    messagesLayout.add(timeSpan, imageDiv); // без времени
                    messagesLayout.getElement().executeJs("this.scrollTo(0, this.scrollHeight);");
                });
            }
        }

        public void showFileMessage(String nameFile, byte[] data, Destination destination) {
            Optional<UI> uiOptional = getUI();

            if (uiOptional.isPresent()) {
                UI ui = uiOptional.get();

                ui.access(() -> {

                    Div fileDiv = new Div();
                    StreamResource resource = new StreamResource(nameFile, () -> new ByteArrayInputStream(data));

                    Anchor downloadLink = new Anchor(resource, "");
                    downloadLink.getElement().setAttribute("download", true);
                    Button downloadButton = new Button(nameFile, event -> downloadLink.getElement().callJsFunction("click"));
                    fileDiv.add(downloadButton, downloadLink);

                    String timeReceived = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    Span timeSpan = new Span(timeReceived);
                    timeSpan.getStyle().set("font-size", "small").set("color", "gray");

                    if (destination.equals(Destination.OWN)) {
                        timeSpan.getStyle().set("margin-left", "auto");
                        fileDiv.getStyle()
                                .set("margin-left", "auto")
                                .set("background-color", "#cceeff");
                        setPossibilityToDelete(messagesLayout, fileDiv);
                    } else {
                        fileDiv.getStyle()
                                .set("margin-right", "auto")
                                .set("background-color", "#f2f2f2");
                        timeSpan.getStyle().set("margin-right", "auto");
                    }

                    fileDiv.getStyle()
                            .set("display", "inline-block")
                            .set("max-width", "80%")
                            .set("overflow", "hidden")
                            .set("padding", "10px")
                            .set("border-radius", "5px")
                            .set("border", "1px solid #ddd")
                            .set("flex-shrink", "0");

                    messagesLayout.add(timeSpan, fileDiv);
                    messagesLayout.getElement().executeJs("this.scrollTo(0, this.scrollHeight);");
                });
            }
        }

        private void setPossibilityToDelete(VerticalLayout messagesLayout, Div fileDiv) {
            messagesLayout.getElement().executeJs("this.scrollTo(0, this.scrollHeight);");

            fileDiv.addClickListener(event -> {
                int indexMessage = messagesLayout.indexOf(fileDiv);
                messagesLayout.remove(fileDiv);
                kafkaWriter.processing(new Message("delete_message", "text", null, indexMessage, null).toBytes(), outputTopic);
            });
        }

        private void clearMessages() {
            Optional<UI> uiOptional = getUI();

            if (uiOptional.isPresent()) {
                UI ui = uiOptional.get();
                ui.access(messagesLayout::removeAll);
            }
        }

        private void deleteMessage(int index) {
            Optional<UI> uiOptional = getUI();

            if (uiOptional.isPresent()) {
                UI ui = uiOptional.get();
                ui.access(() -> {
                    Component componentToRemove = messagesLayout.getComponentAt(index);
                    messagesLayout.remove(componentToRemove);
                });
            }
        }
    }

    public class Frontend {
        private static final String TYPE_MESSAGE = "message";
        private final TextField messageField;
        private final List<Pair<String, InputStream>> filesData = new ArrayList<>();
        private final VerticalLayout layoutColumn3;

        public Frontend() {
            messageField = new TextField();
            messageField.setWidth("100%");

            HorizontalLayout layoutRow = new HorizontalLayout();
            VerticalLayout layoutColumn = new VerticalLayout();
            layoutColumn3 = new VerticalLayout();
            H3 h3 = new H3();

            layoutColumn.setAlignSelf(FlexComponent.Alignment.CENTER, h3);
            layoutRow.addClassName("gap-medium");
            layoutRow.setWidth("100%");
            layoutRow.getStyle().set("flex-grow", "1");

            layoutColumn.setWidth("300px");
            layoutColumn.setHeight("100%");

            h3.setText("Chats");
            layoutColumn.setAlignSelf(Alignment.START, h3);
            h3.setWidth("max-content");

            layoutColumn3.addClassName("padding-xsmall");
            layoutColumn3.setWidth("100%");
            layoutColumn3.setHeight("100%");
            layoutColumn3.getStyle().set("flex-grow", "1");
            layoutColumn3.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            layoutColumn3.setAlignItems(FlexComponent.Alignment.START);

            add(layoutRow);
            layoutRow.add(layoutColumn);
            layoutColumn.add(h3);
            layoutRow.add(layoutColumn3);

            layoutRow.setHeight("100%"); // Устанавливаем высоту в 100% для layoutRow
            setHeight("100vh"); // Устанавливаем высоту в 100vh для Frontend
        }

        public void setPage() {
            VerticalLayout messagesLayout = new VerticalLayout();

            messagesLayout.getStyle()
                    .set("max-width", "100%")
                    .set("max-height", "100%")
                    .set("border", "1px solid blue")
                    .set("border-radius", "10px")
                    .set("padding", "10px")
                    .set("overflow-y", "auto");

            messagesLayout.setWidth("100%");
            messagesLayout.setHeight("100%");

            HorizontalLayout inputLayout = getInputLayout();
            layoutColumn3.add(messagesLayout, inputLayout);
            layoutColumn3.setFlexGrow(1.0, messagesLayout); // Добавляем гибкость для messagesLayout

            messagesLayoutWrapper = new MessagesLayoutWrapper(messagesLayout, kafkaWriter);
        }

        public void sendMessage(Upload upload) {
            if (cipherEncrypt == null) {
                Notification.show("Ошибка: не удалось отправить сообщение");
            } else {
                try {
                    for (Pair<String, InputStream> file : filesData) {
                        byte[] bytesFile = readBytesFromInputStream(file.getRight());
                        String format = getTypeFormat(file.getLeft());
                        Message message = new Message(TYPE_MESSAGE, format, file.getLeft(), 0, bytesFile);
                        byte[] messageBytes = message.toBytes();
                        log.info("TRYING TO SEND FILE");
                        kafkaWriter.processing(cipherEncrypt.encrypt(messageBytes), outputTopic);
                        log.info("END SENDING FILE");
                        server.saveMessage(clientId, anotherClientId, message);

                        if (format.equals("image")) {
                            messagesLayoutWrapper.showImageMessage(file.getLeft(), bytesFile, MessagesLayoutWrapper.Destination.OWN);
                        } else {
                            messagesLayoutWrapper.showFileMessage(file.getLeft(), bytesFile, MessagesLayoutWrapper.Destination.OWN);
                        }
                    }

                    upload.clearFileList();
                    filesData.clear();

                    String textMessage = messageField.getValue();

                    if (!textMessage.isEmpty()) {
                        Message message = new Message(TYPE_MESSAGE, "text", "text", 0, textMessage.getBytes());
                        byte[] messageBytes = message.toBytes();
                        kafkaWriter.processing(cipherEncrypt.encrypt(messageBytes), outputTopic);
                        server.saveMessage(clientId, anotherClientId, message);
                        messagesLayoutWrapper.showTextMessage(textMessage, MessagesLayoutWrapper.Destination.OWN);
                    }

                    messageField.clear();
                } catch (IOException | RuntimeException | ExecutionException ex) {
                    log.error(ex.getMessage());
                    log.error(Arrays.deepToString(ex.getStackTrace()));
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                    log.error(Arrays.deepToString(ex.getStackTrace()));
                    Thread.currentThread().interrupt();
                }
            }
        }

        private HorizontalLayout getInputLayout() {
            HorizontalLayout inputLayout = new HorizontalLayout();
            inputLayout.setWidth("100%");
            inputLayout.setAlignItems(Alignment.BASELINE);
            inputLayout.setJustifyContentMode(JustifyContentMode.START);
            inputLayout.setSpacing(true);

            Upload upload = getUploadButton();
            Button sendButtonText = new Button("Send");
            sendButtonText.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            sendButtonText.addClickListener(e -> sendMessage(upload));

            inputLayout.add(upload, messageField, sendButtonText); // Добавляем upload слева
            inputLayout.getStyle().set("margin", "0");
            inputLayout.getStyle().set("padding", "0");
            return inputLayout;
        }

        private Upload getUploadButton() {
            MultiFileMemoryBuffer multiFileMemoryBuffer = new MultiFileMemoryBuffer();
            Upload uploadButton = new Upload(multiFileMemoryBuffer);

            // Создаем кнопку с иконкой файла
            Button buttonLoadFile = new Button(new Icon(VaadinIcon.FILE_O));
            buttonLoadFile.setWidth("40px");
            buttonLoadFile.setHeight("40px");
            buttonLoadFile.getStyle().set("border-radius", "50%"); // Делаем кнопку круглой
            buttonLoadFile.getStyle().set("background-color", "#1E90FF"); // Устанавливаем синий цвет фона
            buttonLoadFile.getStyle().set("color", "white"); // Устанавливаем белый цвет иконки

            uploadButton.setUploadButton(buttonLoadFile);
            uploadButton.setWidth("200px");
            uploadButton.getStyle()
                    .set("padding", "0")
                    .set("margin", "0")
                    .set("border", "none");
            uploadButton.setDropLabel(new Span(""));
            uploadButton.setDropLabelIcon(new Span(""));

            // Лимит на размер загружаемых файлов до 100 МБ
            uploadButton.setMaxFileSize(100 * 1024 * 1024); // 100МБ в байтах

            uploadButton.addSucceededListener(event -> {
                String fileName = event.getFileName();
                filesData.add(Pair.of(fileName, multiFileMemoryBuffer.getInputStream(fileName)));
            });

            // Обработчик ошибки при превышении лимита размера файла
            uploadButton.addFileRejectedListener(event -> {
                Notification.show("Файл слишком большой. Максимальный размер файла: 100 МБ.", 3000, Notification.Position.MIDDLE);
            });

            return uploadButton;
        }

        private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int bytesRead;
            byte[] data = new byte[1024];

            while ((bytesRead = inputStream.read(data, 0, data.length)) > 0) {
                buffer.write(data, 0, bytesRead);
            }

            buffer.flush();

            return buffer.toByteArray();
        }

        String getTypeFormat(String fileName) {
            int lastDotIndex = fileName.lastIndexOf('.');
            String extension = fileName.substring(lastDotIndex + 1);

            if (extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg")) {
                return "image";
            }

            return "other";
        }
    }

    public class Backend {
        private static final String bootstrapServer = "localhost:9093";
        private static final String autoOffsetReset = "earliest";
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        private static final Random RANDOM = new Random();
        private volatile SymmetricEncryption cipherDecrypt;
        private volatile boolean isRunning = true;
        private CipherInfoMessage cipherInfoAnotherClient;
        private byte[] privateKey;
        private byte[] publicKeyAnother;
        private byte[] p;

        public void startKafka() {
            CipherInfoMessage cipherInfoThisClient = server.getCipherInfoMessageClient(clientId, chatId);

            KafkaConsumer<byte[], byte[]> kafkaConsumer = new KafkaConsumer<>(
                    Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer,
                            ConsumerConfig.GROUP_ID_CONFIG, "group_" + clientId + "_" + chatId,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset
                    ),
                    new ByteArrayDeserializer(),
                    new ByteArrayDeserializer()
            );
            kafkaConsumer.subscribe(Collections.singletonList("input_" + clientId + "_" + chatId));

            try {
                while (isRunning) {
                    ConsumerRecords<byte[], byte[]> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

                    for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
                        String jsonMessage = new String(consumerRecord.value());

                        if (jsonMessage.contains("cipher_info")) {
                            cipherInfoAnotherClient = OBJECT_MAPPER.readValue(jsonMessage, CipherInfoMessage.class);

                            outputTopic = "input_" + cipherInfoAnotherClient.getAnotherClientId() + "_" + chatId;
                            privateKey = generatePrivateKey();
                            p = cipherInfoAnotherClient.getP();
                            anotherClientId = cipherInfoAnotherClient.getAnotherClientId();
                            byte[] publicKey = generatePublicKey(privateKey, p, cipherInfoAnotherClient.getG());

                            log.info("Client {} get cipher info", clientId);
                            log.info(cipherInfoAnotherClient.toString());

                            kafkaWriter.processing(new KeyMessage("key_info", publicKey).toBytes(), outputTopic);

                            if (publicKeyAnother != null) {
                                cipherInfoAnotherClient.setPublicKey(publicKeyAnother);
                                cipherDecrypt = CipherInfoMessageParser.getCipher(cipherInfoAnotherClient, new BigInteger(privateKey), new BigInteger(p));

                                cipherInfoThisClient.setPublicKey(publicKeyAnother);
                                cipherEncrypt = CipherInfoMessageParser.getCipher(cipherInfoThisClient, new BigInteger(privateKey), new BigInteger(p));
                            }
                        } else if (jsonMessage.contains("key_info")) {
                            log.info("Client {} get key info", clientId);

                            KeyMessage keyMessage = OBJECT_MAPPER.readValue(jsonMessage, KeyMessage.class);

                            if (cipherInfoAnotherClient != null) {
                                cipherInfoAnotherClient.setPublicKey(keyMessage.getPublicKey());
                                cipherDecrypt = CipherInfoMessageParser.getCipher(cipherInfoAnotherClient, new BigInteger(privateKey), new BigInteger(p));

                                cipherInfoThisClient.setPublicKey(keyMessage.getPublicKey());
                                cipherEncrypt = CipherInfoMessageParser.getCipher(cipherInfoThisClient, new BigInteger(privateKey), new BigInteger(p));
                            } else {
                                publicKeyAnother = keyMessage.getPublicKey();
                            }
                        } else if (jsonMessage.contains("delete_message")) {
                            log.info("get disconnect message");
                            Message deleteMessage = OBJECT_MAPPER.readValue(jsonMessage, Message.class);
                            messagesLayoutWrapper.deleteMessage(deleteMessage.getIndexMessage());
                        } else if (jsonMessage.contains("disconnect")) {
                            cipherDecrypt = null;
                            cipherEncrypt = null;
                            messagesLayoutWrapper.clearMessages();
                        } else {
                            Message message = MessageParser.parseMessage(new String(cipherDecrypt.decrypt(consumerRecord.value())));

                            if (message != null && message.getBytes() != null) {
                                log.info("Client {} get message", clientId);

                                server.saveMessage(anotherClientId, clientId, message);

                                if (message.getTypeFormat().equals("text")) {
                                    messagesLayoutWrapper.showTextMessage(new String(message.getBytes()), MessagesLayoutWrapper.Destination.ANOTHER);
                                } else if (message.getTypeFormat().equals("image")) {
                                    messagesLayoutWrapper.showImageMessage(message.getFileName(), message.getBytes(), MessagesLayoutWrapper.Destination.ANOTHER);
                                } else {
                                    messagesLayoutWrapper.showFileMessage(message.getFileName(), message.getBytes(), MessagesLayoutWrapper.Destination.ANOTHER);
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
                log.error(Arrays.deepToString(ex.getStackTrace()));
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error(ex.getMessage());
                log.error(Arrays.deepToString(ex.getStackTrace()));
            }

            kafkaConsumer.close();
            log.info("End kafka reader client {}", clientId);
        }

        private byte[] generatePrivateKey() {
            return new BigInteger(100, RANDOM).toByteArray();
        }

        private byte[] generatePublicKey(byte[] privateKey, byte[] p, byte[] g) {
            BigInteger pNumber = new BigInteger(p);
            BigInteger gNumber = new BigInteger(g);
            BigInteger key = new BigInteger(privateKey);
            return gNumber.modPow(key, pNumber).toByteArray();
        }

        public void close() {
            isRunning = false;
        }
    }
}

