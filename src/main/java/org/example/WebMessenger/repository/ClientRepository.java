package org.example.WebMessenger.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.example.WebMessenger.model.client.ClientInfo;

@Repository
public interface ClientRepository extends CrudRepository<ClientInfo, Long> {
    @Transactional
    default ClientInfo addChat(long clientId, long chatId) {
        ClientInfo client = findById(clientId).orElse(null);

        if (client != null) {
            long[] updatedChats = new long[client.getChats().length + 1];
            System.arraycopy(client.getChats(), 0, updatedChats, 0, client.getChats().length);
            updatedChats[updatedChats.length - 1] = chatId;

            client.setChats(updatedChats);

            return save(client);
        }

        return null;
    }

    @Transactional
    default ClientInfo removeChat(long clientId, long chatId) {
        ClientInfo client = findById(clientId).orElse(null);

        if (client != null) {
            long[] chats = client.getChats();
            long[] updatedChats = new long[chats.length - 1];

            int index = 0;

            for (long chat : chats) {
                if (chat != chatId) {
                    updatedChats[index++] = chat;
                }
            }

            client.setChats(updatedChats);

            return save(client);
        }

        return null;
    }
}

