package br.com.fullcycle.application.event;

import br.com.fullcycle.application.repository.InMemoryEventRepository;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.event.EventStatus;
import br.com.fullcycle.domain.exceptions.ValidationException;
import br.com.fullcycle.domain.partner.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelEventUseCaseTest {

    @Test
    @DisplayName("Deve cancelar um evento ativo")
    public void testCancelEvent() {
        // given
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);
        final var eventRepository = new InMemoryEventRepository();
        eventRepository.create(anEvent);

        final var input = new CancelEventUseCase.Input(anEvent.eventId().value());

        // when
        final var output = new CancelEventUseCase(eventRepository).execute(input);

        // then
        Assertions.assertEquals(anEvent.eventId().value(), output.id());
        Assertions.assertEquals(EventStatus.CANCELLED.name(), output.status());
        Assertions.assertEquals(EventStatus.CANCELLED, eventRepository.eventOfId(anEvent.eventId()).get().status());
        Assertions.assertEquals(1, eventRepository.eventOfId(anEvent.eventId()).get().allDomainEvents().size());
    }

    @Test
    @DisplayName("Não deve cancelar um evento inexistente")
    public void testCancelEventNotFound() {
        // given
        final var expectedError = "Event not found";
        final var input = new CancelEventUseCase.Input(EventId.unique().value());

        // when
        final var actualException = Assertions.assertThrows(
                ValidationException.class,
                () -> new CancelEventUseCase(new InMemoryEventRepository()).execute(input)
        );

        // then
        Assertions.assertEquals(expectedError, actualException.getMessage());
    }

    @Test
    @DisplayName("Não deve cancelar um evento já cancelado")
    public void testCancelEventAlreadyCancelled() {
        // given
        final var expectedError = "Event already cancelled";
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent("Disney on Ice", "2021-01-01", 10, aPartner);
        anEvent.cancel();
        final var eventRepository = new InMemoryEventRepository();
        eventRepository.create(anEvent);

        final var input = new CancelEventUseCase.Input(anEvent.eventId().value());

        // when
        final var actualException = Assertions.assertThrows(
                ValidationException.class,
                () -> new CancelEventUseCase(eventRepository).execute(input)
        );

        // then
        Assertions.assertEquals(expectedError, actualException.getMessage());
    }
}
