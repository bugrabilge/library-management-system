package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.dto.event.BookAvailabilityEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BookAvailabilityPublisher {

    private final Sinks.Many<BookAvailabilityEvent> sink;
    private final Flux<BookAvailabilityEvent> flux;

    public BookAvailabilityPublisher() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.flux = sink.asFlux();
    }

    public void publish(BookAvailabilityEvent event) {
        sink.tryEmitNext(event);
    }

    public Flux<BookAvailabilityEvent> getStream() {
        return flux;
    }
}
