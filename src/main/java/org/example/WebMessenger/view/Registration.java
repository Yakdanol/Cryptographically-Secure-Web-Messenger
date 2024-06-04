package org.example.WebMessenger.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.example.WebMessenger.model.client.ClientInfo;
import org.example.WebMessenger.service.Server;

@Slf4j
@Route("")
public class Registration extends VerticalLayout {
    public Registration(Server server) {
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn = new VerticalLayout();
        LoginForm loginForm = new LoginForm();
        setWidth("100%");
        getStyle().set("flex-grow", "1");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn.setWidth("100%");
        layoutColumn.getStyle().set("flex-grow", "1");
        layoutColumn.setJustifyContentMode(JustifyContentMode.CENTER);
        layoutColumn.setAlignItems(Alignment.CENTER);
        layoutColumn.setAlignSelf(FlexComponent.Alignment.CENTER, loginForm);
        add(layoutRow);
        layoutRow.add(layoutColumn);
        layoutColumn.add(loginForm);

        // Убрал кнопку "Forgot Password"
        loginForm.setForgotPasswordButtonVisible(false);

        // Валидация для полей Username и Password
        loginForm.addLoginListener(event -> {
            String username = event.getUsername();
            String password = event.getPassword();
            String Algorithm = "RC5";
            if (username != null && username.matches("[a-zA-Z0-9]+") && password != null && password.matches("[a-zA-Z0-9]+")) {
                try {
                    ClientInfo clientInfo = server.authorization(username, password.hashCode(), Algorithm);
                    if (clientInfo != null) {
                        Notification.show("User has been successfully registered!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        UI.getCurrent().navigate(String.valueOf(clientInfo.getId()));
                    } else {
                        // Ошибка при сохранении клиента
                        Notification.show("Error during user registration!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                } catch (Exception ex) {
                    // Ошибка регистрации клиента
                    Notification.show("Error during user registration!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    throw new RuntimeException(ex.getMessage());
                }
            } else {
                Notification.show("Invalid username or password!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                loginForm.setEnabled(true);
            }
        });
    }
}

