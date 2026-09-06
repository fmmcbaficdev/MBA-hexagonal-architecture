package br.com.fullcycle.infrastructure.repositories;

import br.com.fullcycle.IntegrationTest;
import br.com.fullcycle.domain.customer.Customer;
import br.com.fullcycle.domain.customer.CustomerRepository;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.EventRepository;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketRepository;
import br.com.fullcycle.domain.partner.Partner;
import br.com.fullcycle.domain.partner.PartnerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TicketDatabaseRepositoryIT extends IntegrationTest {

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
    @DisplayName("Deve buscar ingressos por evento na persistência real")
    public void testTicketsByEventId() {
        // given
        final var partner = partnerRepository.create(Partner.newPartner("Disney", "45.123.123/0001-12", "disney@gmail.com"));
        final var event = eventRepository.create(Event.newEvent("Disney on Ice", "2021-01-01", 10, partner));
        final var otherEvent = eventRepository.create(Event.newEvent("Another Show", "2021-02-01", 10, partner));
        final var john = customerRepository.create(Customer.newCustomer("John Doe", "123.456.789-00", "john@gmail.com"));
        final var jane = customerRepository.create(Customer.newCustomer("Jane Doe", "987.654.321-00", "jane@gmail.com"));

        ticketRepository.create(Ticket.newTicket(john.customerId(), event.eventId()));
        ticketRepository.create(Ticket.newTicket(jane.customerId(), event.eventId()));
        ticketRepository.create(Ticket.newTicket(john.customerId(), otherEvent.eventId()));

        // when
        final var actualTickets = ticketRepository.ticketsByEventId(event.eventId());

        // then
        Assertions.assertEquals(2, actualTickets.size());
        Assertions.assertTrue(actualTickets.stream().allMatch(ticket -> ticket.eventId().equals(event.eventId())));
    }
}
