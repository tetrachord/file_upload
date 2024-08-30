package com.example.repository;

import com.example.domain.IpRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IpRequestRepository extends CrudRepository<IpRequest, UUID> {

    @Query("select i from IpRequest i where i.requestId = :requestId")
    Optional<IpRequest> getByRequestId(UUID requestId);
}
