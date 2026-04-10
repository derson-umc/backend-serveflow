package com.serveflow.domain.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
