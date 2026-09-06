package br.com.fullcycle.infrastructure.gateways;

import br.com.fullcycle.IntegrationTest;
import br.com.fullcycle.application.event.CancelEventUseCase;
import br.com.fullcycle.domain.customer.Customer;
import br.com.fullcycle.domain.customer.CustomerRepository;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.EventCancelled;
import br.com.fullcycle.domain.event.EventRepository;
import br.com.fullcycle.domain.event.EventStatus;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketRepository;
import br.com.fullcycle.domain.event.ticket.TicketStatus;
import br.com.fullcycle.domain.partner.Partner;
import br.com.fullcycle.domain.partner.PartnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventCancelledCascadeIT extends IntegrationTest {

    @Autowired
    private ConsumerQueueGateway consumerQueueGateway;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CancelEventUseCase cancelEventUseCase;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
        customerRepository.deleteAll();
        partnerRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve cancelar ingressos ao consumir EventCancelled pela fila")
    public void testCascadeCancelTicketsThroughQueue() throws Exception {
        // given
        final var partner = partnerRepository.create(Partner.newPartner("Disney", "45.123.123/0001-12", "disney@gmail.com"));
        final var event = eventRepository.create(Event.newEvent("Disney on Ice", "2021-01-01", 10, partner));
        final var john = customerRepository.create(Customer.newCustomer("John Doe", "123.456.789-00", "john@gmail.com"));
        final var jane = customerRepository.create(Customer.newCustomer("Jane Doe", "987.654.321-00", "jane@gmail.com"));
        ticketRepository.create(Ticket.newTicket(john.customerId(), event.eventId()));
        ticketRepository.create(Ticket.newTicket(jane.customerId(), event.eventId()));

        cancelEventUseCase.execute(new CancelEventUseCase.Input(event.eventId().value()));
        Assertions.assertEquals(EventStatus.CANCELLED, eventRepository.eventOfId(event.eventId()).get().status());

        final var payload = mapper.writeValueAsString(new EventCancelled(event.eventId()));

        // when
        consumerQueueGateway.publish(payload);
        consumerQueueGateway.publish(payload);

        // then
        final var actualTickets = ticketRepository.ticketsByEventId(event.eventId());
        Assertions.assertEquals(2, actualTickets.size());
        Assertions.assertTrue(actualTickets.stream().allMatch(ticket -> ticket.status() == TicketStatus.CANCELLED));
    }
}
