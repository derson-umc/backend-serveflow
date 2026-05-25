package com.serveflow.integration;

import com.serveflow.dto.order.request.AddressInput;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressResolver {

    private final FindPostalCode findPostalCode;
    private final String defaultCity;
    private final String defaultState;

    public AddressResolver(FindPostalCode findPostalCode,
                           @Value("${app.delivery.default-city:Cidade não informada}") String defaultCity,
                           @Value("${app.delivery.default-state:SP}") String defaultState) {
        this.findPostalCode = findPostalCode;
        this.defaultCity = defaultCity;
        this.defaultState = defaultState;
    }

    public Address resolve(AddressInput dto) {
        if (dto == null) return null;

        if (hasValidCep(dto)) {
            Optional<Address> resolved = findPostalCode.findByCep(dto.cep());
            if (resolved.isPresent()) {
                return buildFromPostalCode(resolved.get(), dto);
            }
        }

        return hasMinimumManualFields(dto) ? buildManually(dto) : null;
    }

    private boolean hasValidCep(AddressInput dto) {
        return dto.cep() != null && !dto.cep().isBlank();
    }

    private boolean hasMinimumManualFields(AddressInput dto) {
        return dto.street() != null && !dto.street().isBlank()
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
        String city  = dto.city()  != null && !dto.city().isBlank()  ? dto.city()  : defaultCity;
        String state = dto.state() != null && !dto.state().isBlank() ? dto.state() : defaultState;
        return Address.create(cep, new Street(dto.street()), new City(city),
                new State(state), new Number(dto.number()), comp);
    }
}
