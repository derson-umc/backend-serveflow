package com.serveflow.integration;

import com.serveflow.dto.order.request.AddressInput;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressResolver {

    private final FindPostalCode findPostalCode;

    public AddressResolver(FindPostalCode findPostalCode) {
        this.findPostalCode = findPostalCode;
    }

    public Address resolve(AddressInput dto) {
        if (dto == null) return null;

        if (hasValidCep(dto)) {
            Optional<Address> resolved = findPostalCode.findByCep(dto.cep());
            if (resolved.isPresent()) {
                return buildFromPostalCode(resolved.get(), dto);
            }
        }

        return hasManualFields(dto) ? buildManually(dto) : null;
    }

    private boolean hasValidCep(AddressInput dto) {
        return dto.cep() != null && !dto.cep().isBlank();
    }

    private boolean hasManualFields(AddressInput dto) {
        return dto.street() != null && !dto.street().isBlank()
                && dto.city() != null && !dto.city().isBlank()
                && dto.state() != null && !dto.state().isBlank()
                && dto.number() != null && !dto.number().isBlank();
    }

    private Address buildFromPostalCode(Address base, AddressInput dto) {
        String num = dto.number() != null && !dto.number().isBlank() ? dto.number() : "S/N";
        Complement comp = dto.complement() != null && !dto.complement().isBlank()
                ? new Complement(dto.complement()) : null;
        return Address.create(base.getCep(), base.getStreet(), base.getCity(),
                base.getState(), new Number(num), comp);
    }

    private Address buildManually(AddressInput dto) {
        Cep cep = hasValidCep(dto) ? new Cep(dto.cep()) : null;
        Complement comp = dto.complement() != null && !dto.complement().isBlank()
                ? new Complement(dto.complement()) : null;
        return Address.create(cep, new Street(dto.street()), new City(dto.city()),
                new State(dto.state()), new Number(dto.number()), comp);
    }
}
