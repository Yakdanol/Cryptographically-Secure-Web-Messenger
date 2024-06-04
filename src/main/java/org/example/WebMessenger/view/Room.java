package org.example.WebMessenger.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.example.WebMessenger.model.client.CipherInfo;
import org.example.WebMessenger.service.Server;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route("")
public class Room extends VerticalLayout implements HasUrlParameter<String> {
    private long clientId;
    private final Server server;
    private final VerticalLayout chatListLayout = new VerticalLayout(); // Для списка чатов

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.clientId = Long.parseLong(parameter);

        if (server.notExistClient(clientId)) {
            Notification.show("User not found");
            setEnabled(false);
        }
    }

    public Room(Server server) {
        log.info("Chat start");
        this.server = server;

        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        VerticalLayout layoutColumn4 = new VerticalLayout();

        H3 h3 = new H3();
        //Span idText = new Span("Id = " + clientId);
        Button buttonSecondary = new Button();
        MessageList messageList = new MessageList();
        MessageInput messageInput = new MessageInput();

        setWidth("100%");
        setHeight("100%");
        getStyle().set("flex-grow", "1");

        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("100%");
        layoutRow.getStyle().set("flex-grow", "1");

        layoutColumn2.addClassName(LumoUtility.Gap.XSMALL);
        layoutColumn2.addClassName(LumoUtility.Padding.XSMALL);
        layoutColumn2.setWidth("200px");
        layoutColumn2.setHeight("100%");
        layoutColumn2.setJustifyContentMode(JustifyContentMode.END);
        layoutColumn2.setAlignItems(Alignment.END);

        h3.setText("Chats");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.START, h3);
        h3.setWidth("max-content");

        layoutColumn3.setWidthFull();
        layoutColumn3.setHeight("100%");
        layoutColumn3.getStyle().set("flex-grow", "1");

        layoutRow2.setWidthFull();
        layoutRow2.setHeight("min-content");
        layoutRow2.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow2.setAlignItems(Alignment.END);
        layoutRow2.setJustifyContentMode(JustifyContentMode.END);

        //idText.setWidth("max-content");

        buttonSecondary.setText("+");
        buttonSecondary.addClickListener(event -> openDialog());
        layoutRow2.setAlignSelf(FlexComponent.Alignment.CENTER, buttonSecondary);
        buttonSecondary.setWidth("min-content");

        layoutColumn4.setSpacing(false);
        layoutColumn4.setPadding(false);
        layoutColumn4.setWidth("100%");
        layoutColumn4.setHeight("100%");
        layoutColumn4.getStyle().set("flex-grow", "1");
        layoutColumn4.setJustifyContentMode(JustifyContentMode.CENTER);
        layoutColumn4.setAlignItems(Alignment.CENTER);

        messageList.setWidth("100%");
        messageList.setHeight("100%");
        messageList.getStyle().set("flex-grow", "1");

        setMessageListSampleData(messageList);

        layoutColumn4.setAlignSelf(FlexComponent.Alignment.CENTER, messageInput);
        messageInput.setWidth("100%");

        add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(h3);
        layoutColumn2.add(layoutColumn3);
        layoutColumn2.add(layoutRow2);
        //layoutRow2.add(idText);
        layoutRow2.add(buttonSecondary);
        layoutRow.add(layoutColumn4);
        layoutColumn4.add(messageList);
        layoutColumn4.add(messageInput);

        // Отображений тестового чата
        messageList.setVisible(false);
        messageInput.setVisible(false);

        layoutColumn3.add(chatListLayout);
        chatListLayout.setWidthFull();
    }

    private void openDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.CENTER);

        IntegerField idField = new IntegerField();
        idField.setLabel("Chat id");
        idField.setPlaceholder("Enter id Chat");
        idField.setWidth("150px");

        ComboBox<SampleItem> comboBoxCipherAlgorithm = new ComboBox<>();
        ComboBox<SampleItem> comboBoxEncryptionMode = new ComboBox<>();
        ComboBox<SampleItem> comboBoxPadding = new ComboBox<>();
        setDetailsSampleData(dialogLayout, comboBoxCipherAlgorithm, comboBoxEncryptionMode, comboBoxPadding);

        String Algorithm = comboBoxCipherAlgorithm.getValue().value;
        String Padding = comboBoxPadding.getValue().value;
        String encryptionMode = comboBoxEncryptionMode.getValue().value;

        Button createNewChatButton = createNewChat(dialog, idField, Algorithm, Padding, encryptionMode);
        createNewChatButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, createNewChatButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        dialogLayout.add(idField, comboBoxCipherAlgorithm, comboBoxEncryptionMode, comboBoxPadding, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private Button createNewChat(Dialog dialog, IntegerField idField, String Algorithm, String Padding, String encryptionMode) {
        // положить cipherInfo в чат

        return new Button("Create New Chat", e -> {
            Integer clientId = idField.getValue();

            Notification.show("Dialogue with the client id " + clientId + " successfully created")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            addChatInfoBlock(String.valueOf(clientId), Algorithm, Padding, encryptionMode); // Добавляем новый чат
            dialog.close();
        });
    }

    // Тестировочный чат
    private void setMessageListSampleData(MessageList messageList) {
        MessageListItem message1 = new MessageListItem("Nature does not hurry, yet everything gets accomplished.",
                LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC), "Matt Mambo");
        message1.setUserColorIndex(1);
        MessageListItem message2 = new MessageListItem(
                "Using your talent, hobby or profession in a way that makes you contribute with something good to this world is truly the way to go.",
                LocalDateTime.now().minusMinutes(55).toInstant(ZoneOffset.UTC), "Linsey Listy");
        message2.setUserColorIndex(2);
        messageList.setItems(message1, message2);
    }

    private void setDetailsSampleData(VerticalLayout layout, ComboBox<SampleItem> comboBoxCipherAlgorithm, ComboBox<SampleItem> comboBoxEncryptionMode, ComboBox<SampleItem> comboBoxPadding) {
        comboBoxCipherAlgorithm.setLabel("Cipher Algorithm");
        comboBoxCipherAlgorithm.setWidth("min-content");
        setComboBoxCipherAlgorithm(comboBoxCipherAlgorithm);

        comboBoxEncryptionMode.setLabel("Encryption mode");
        comboBoxEncryptionMode.setWidth("min-content");
        setComboBoxEncryptionMode(comboBoxEncryptionMode);

        comboBoxPadding.setLabel("Padding mode");
        comboBoxPadding.setWidth("min-content");
        setComboBoxPadding(comboBoxPadding);

        // Установка значений по умолчанию
        comboBoxCipherAlgorithm.setValue(new SampleItem("RC5", "RC5", null));
        comboBoxEncryptionMode.setValue(new SampleItem("ECB", "ECB", null));
        comboBoxPadding.setValue(new SampleItem("ANSI_X923", "ANSI X.923", null));

        layout.add(comboBoxCipherAlgorithm);
        layout.add(comboBoxEncryptionMode);
        layout.add(comboBoxPadding);
    }

    record SampleItem(String value, String label, Boolean disabled) {
    }

    private void setComboBoxCipherAlgorithm(ComboBox<SampleItem> comboBoxCipherAlgorithm) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("RC5", "RC5", null));
        sampleItems.add(new SampleItem("SERPENT", "Serpent", null));
        comboBoxCipherAlgorithm.setItems(sampleItems);
        comboBoxCipherAlgorithm.setItemLabelGenerator(item -> item.label());
    }

    private void setComboBoxEncryptionMode(ComboBox<SampleItem> comboBoxEncryptionMode) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("ECB", "ECB", null));
        sampleItems.add(new SampleItem("CBC", "CBC", null));
        sampleItems.add(new SampleItem("CFB", "CFB", null));
        sampleItems.add(new SampleItem("CTR", "CTR", null));
        sampleItems.add(new SampleItem("OFB", "OFB", null));
        sampleItems.add(new SampleItem("PCBC", "PCBC", null));
        sampleItems.add(new SampleItem("RANDOM_DELTA", "RD", null));
        comboBoxEncryptionMode.setItems(sampleItems);
        comboBoxEncryptionMode.setItemLabelGenerator(item -> item.label());
    }

    private void setComboBoxPadding(ComboBox<SampleItem> comboBoxPadding) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("ANSI_X923", "ANSI X.923", null));
        sampleItems.add(new SampleItem("PKCS7", "PKCS7", null));
        sampleItems.add(new SampleItem("ISO_10126", "ISO 10126", null));
        sampleItems.add(new SampleItem("ZEROS", "ZEROS", null));
        comboBoxPadding.setItems(sampleItems);
        comboBoxPadding.setItemLabelGenerator(item -> item.label());
    }

    // Метод для добавления информации о чате в список чатов
    private void addChatInfoBlock(String chatId, String Algorithm, String Padding, String encryptionMode) {
        HorizontalLayout chatInfoLayout = new HorizontalLayout();
        chatInfoLayout.setWidthFull();
        chatInfoLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // Стиль тонкой прямоугольной рамки синего цвета с закругленными краями
        chatInfoLayout.getStyle()
                .set("border-radius", "10px")
                .set("padding", "10px");

        Button chatInfoButton = getChatInfoButton(chatId, Algorithm, Padding, encryptionMode);
        Button leaveChatButton = getLeaveChatButton(chatId, chatInfoLayout);

        chatInfoLayout.add(chatInfoButton, leaveChatButton);
        chatInfoLayout.expand(chatInfoButton);

        chatListLayout.add(chatInfoLayout); // Добавление в chatListLayout списка чатов
    }

    private Button getChatInfoButton(String chatId, String Algorithm, String Padding, String encryptionMode) {
        String url = "chat/" + clientId + "/" + chatId;

        Button chatInfoButton = new Button("Chat: " + chatId,
                e -> {
                    if (server.isNotOpenWindow(url) && server.connectToChat(clientId, Long.parseLong(chatId), Algorithm, Padding, encryptionMode)) {
                        UI.getCurrent().getPage().executeJs("window.open($0, '_blank')", url);
                    } else {
                        if (!server.isNotOpenWindow(url)) {
                            Notification.show("Connection error: you are already in the chat!");
                        } else {
                            Notification.show("Connection error: the chat is already occupied"); // занята
                        }
                    }
                });

        chatInfoButton.setWidth("100%");
        return chatInfoButton;
    }

    private Button getLeaveChatButton(String chatId, HorizontalLayout chatInfoLayout) {
        // Значок красной корзины с белым фоном
        Icon deleteIcon = new Icon(VaadinIcon.TRASH);
        deleteIcon.getStyle().set("color", "red"); // Красный значок

        Button leaveChatButton = new Button(deleteIcon, e -> {
            server.disconnectFromChat(clientId, Long.parseLong(chatId));
            removeChatInfoBlock(chatInfoLayout);
        });

        leaveChatButton.getStyle().set("background-color", "white"); // Белый фон
        leaveChatButton.getStyle().set("border", "1px solid red"); // Красная рамка для кнопки
        leaveChatButton.getStyle().set("color", "red"); // Красный текст
        leaveChatButton.setWidth("50px");

        return leaveChatButton;
    }

    private void removeChatInfoBlock(HorizontalLayout chatInfoLayout) {
        chatListLayout.remove(chatInfoLayout);
    }
}

