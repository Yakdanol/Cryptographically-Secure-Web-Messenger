package org.example.WebMessenger.repository;

import org.springframework.data.repository.CrudRepository;import org.springframework.stereotype.Repository;
import org.example.WebMessenger.model.client.ChatInfo;
import java.util.Optional;

@Repository
public interface ChatRepository extends CrudRepository<ChatInfo, Long> {
    Optional<ChatInfo> getChatInfoByChatId(long chatId);

    boolean existsChatInfoByChatId(long chatId);
}