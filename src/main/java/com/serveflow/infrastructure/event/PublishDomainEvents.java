package com.serveflow.infrastructure.event;

import com.serveflow.domain.event.DomainEvent;
import com.serveflow.domain.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PublishDomainEvents implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public PublishDomainEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
