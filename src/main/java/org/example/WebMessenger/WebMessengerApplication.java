package org.example.WebMessenger;

import lombok.extern.slf4j.Slf4j;
import org.example.CipherAlgorithms.Implementation.algorithms.RC5.RC5;
import org.example.CipherAlgorithms.SymmetricEncryption;
//import org.example.WebMessenger.test.Client;
//import org.example.WebMessenger.test.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class WebMessengerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebMessengerApplication.class, args);
    }
}
