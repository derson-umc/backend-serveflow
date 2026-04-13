package com.serveflow.web.facade;

import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderStatus;
import com.serveflow.domain.service.OrderService;
import com.serveflow.web.dto.order.request.CreateOrderInput;
import com.serveflow.web.dto.order.response.OrderOutput;
import com.serveflow.web.mapper.OrderWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderWebFacade {

    private final OrderService orderService;
    private final OrderWebMapper mapper;

    public OrderWebFacade(OrderService orderService, OrderWebMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    public OrderOutput create(CreateOrderInput request) {
        var addressDto = request.address();
        var manualAddress = mapper.toAddressDomain(addressDto);
        var resolvedAddress = orderService.resolveAddress(
                addressDto != null ? addressDto.cep() : null,
                addressDto != null ? addressDto.number() : null,
                addressDto != null ? addressDto.complement() : null,
                manualAddress
        );
        Order order = mapper.toDomain(request, resolvedAddress);
        Order created = orderService.create(order);
        return mapper.toResponse(created);
    }

    public OrderOutput findById(UUID id) {
        return mapper.toResponse(orderService.findById(id));
    }

    public List<OrderOutput> findAll() {
        return mapper.toResponseList(orderService.findAll());
    }

    public List<OrderOutput> findByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return mapper.toResponseList(orderService.findByStatus(orderStatus));
    }

    public OrderOutput confirm(UUID id) {
        return mapper.toResponse(orderService.confirm(id));
    }

    public OrderOutput startPreparation(UUID id) {
        return mapper.toResponse(orderService.startPreparation(id));
    }

    public OrderOutput markReady(UUID id) {
        return mapper.toResponse(orderService.markReady(id));
    }

    public OrderOutput sendForDelivery(UUID id) {
        return mapper.toResponse(orderService.sendForDelivery(id));
    }

    public OrderOutput complete(UUID id) {
        return mapper.toResponse(orderService.complete(id));
    }

    public OrderOutput cancel(UUID id) {
        return mapper.toResponse(orderService.cancel(id));
    }
}
