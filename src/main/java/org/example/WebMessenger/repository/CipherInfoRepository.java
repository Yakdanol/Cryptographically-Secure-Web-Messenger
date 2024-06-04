package org.example.WebMessenger.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.example.WebMessenger.model.client.CipherInfo;

@Repository
public interface CipherInfoRepository extends CrudRepository<CipherInfo, Long> {

}
