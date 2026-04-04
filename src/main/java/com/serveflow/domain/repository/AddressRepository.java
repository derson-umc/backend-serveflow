package com.serveflow.domain.repository;

import com.serveflow.domain.model.address.Address;

import java.util.Optional;

public interface AddressRepository {

    Optional<Address> findByCep(String cep);
}
