package br.com.fullcycle.application.ticket;

import br.com.fullcycle.application.repository.InMemoryTicketRepository;
import br.com.fullcycle.domain.customer.Customer;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.ticket.Ticket;
import br.com.fullcycle.domain.event.ticket.TicketStatus;
import br.com.fullcycle.domain.partner.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelEventTicketsUseCaseTest {

    @Test
    @DisplayName("Deve cancelar todos os ingressos de um evento")
    public void testCancelEventTickets() {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);
        final var aCustomer = Customer.newCustomer("John Doe", "123.456.789-01", "john.doe@gmail.com");
        final var anotherCustomer = Customer.newCustomer("Jane Doe", "987.654.321-00", "jane.doe@gmail.com");

        final var ticketRepository = new InMemoryTicketRepository();
        final var firstTicket = ticketRepository.create(Ticket.newTicket(aCustomer.customerId(), anEvent.eventId()));
        final var secondTicket = ticketRepository.create(Ticket.newTicket(anotherCustomer.customerId(), anEvent.eventId()));

        final var input = new CancelEventTicketsUseCase.Input(anEvent.eventId().value());

        // when
        final var output = new CancelEventTicketsUseCase(ticketRepository).execute(input);

        // then
        Assertions.assertEquals(2, output.ticketIds().size());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(firstTicket.ticketId()).get().status());
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(secondTicket.ticketId()).get().status());
    }

    @Test
    @DisplayName("Reprocessar o cancelamento dos ingressos é idempotente")
    public void testCancelEventTicketsIsIdempotent() {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);
        final var aCustomer = Customer.newCustomer("John Doe", "123.456.789-01", "john.doe@gmail.com");
        final var ticketRepository = new InMemoryTicketRepository();
        final var aTicket = ticketRepository.create(Ticket.newTicket(aCustomer.customerId(), anEvent.eventId()));
        final var input = new CancelEventTicketsUseCase.Input(anEvent.eventId().value());
        final var useCase = new CancelEventTicketsUseCase(ticketRepository);

        // when
        useCase.execute(input);
        useCase.execute(input);

        // then
        Assertions.assertEquals(TicketStatus.CANCELLED, ticketRepository.ticketOfId(aTicket.ticketId()).get().status());
    }
}
