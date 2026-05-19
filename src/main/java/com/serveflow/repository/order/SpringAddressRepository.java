package com.serveflow.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringAddressRepository extends JpaRepository<AddressEntity, UUID> {

    @Query("""
            SELECT a FROM AddressEntity a
            WHERE LOWER(TRIM(a.street)) = LOWER(TRIM(:street))
              AND LOWER(TRIM(a.city))   = LOWER(TRIM(:city))
              AND LOWER(TRIM(a.state))  = LOWER(TRIM(:state))
              AND LOWER(TRIM(a.number)) = LOWER(TRIM(:number))
              AND (
                (:cep IS NULL AND a.cep IS NULL) OR LOWER(TRIM(a.cep)) = LOWER(TRIM(:cep))
              )
              AND (
                (:complement IS NULL AND a.complement IS NULL)
                OR LOWER(TRIM(a.complement)) = LOWER(TRIM(:complement))
              )
            """)
    Optional<AddressEntity> findExisting(
            @Param("cep")        String cep,
            @Param("street")     String street,
            @Param("city")       String city,
            @Param("state")      String state,
            @Param("number")     String number,
            @Param("complement") String complement
    );
}
