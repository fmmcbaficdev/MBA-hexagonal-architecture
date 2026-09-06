package br.com.fullcycle.application.event;

import br.com.fullcycle.application.repository.InMemoryEventRepository;
import br.com.fullcycle.domain.event.Event;
import br.com.fullcycle.domain.event.EventId;
import br.com.fullcycle.domain.event.EventStatus;
import br.com.fullcycle.domain.partner.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetEventByIdUseCaseTest {

    @Test
    @DisplayName("Deve obter um evento por id")
    public void testGetById() {
        // given
        final var expectedDate = "2021-01-01";
        final var expectedName = "Disney on Ice";
        final var expectedTotalSpots = 10;
        final var aPartner = Partner.newPartner("John Doe", "41.536.538/0001-00", "john.doe@gmail.com");
        final var anEvent = Event.newEvent(expectedName, expectedDate, expectedTotalSpots, aPartner);
        final var eventRepository = new InMemoryEventRepository();
        eventRepository.create(anEvent);

        final var input = new GetEventByIdUseCase.Input(anEvent.eventId().value());

        // when
        final var output = new GetEventByIdUseCase(eventRepository).execute(input).get();

        // then
        Assertions.assertEquals(anEvent.eventId().value(), output.id());
        Assertions.assertEquals(expectedName, output.name());
        Assertions.assertEquals(expectedDate, output.date());
        Assertions.assertEquals(expectedTotalSpots, output.totalSpots());
        Assertions.assertEquals(EventStatus.ACTIVE.name(), output.status());
    }

    @Test
    @DisplayName("Deve obter vazio ao tentar recuperar um evento inexistente")
    public void testGetByIdWithInvalidId() {
        // given
        final var input = new GetEventByIdUseCase.Input(EventId.unique().value());

        // when
        final var output = new GetEventByIdUseCase(new InMemoryEventRepository()).execute(input);

        // then
        Assertions.assertTrue(output.isEmpty());
    }
}
