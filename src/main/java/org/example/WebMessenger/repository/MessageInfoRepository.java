package org.example.WebMessenger.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.example.WebMessenger.model.client.MessageInfo;

@Repository
public interface MessageInfoRepository extends CrudRepository<MessageInfo, String> {

}
