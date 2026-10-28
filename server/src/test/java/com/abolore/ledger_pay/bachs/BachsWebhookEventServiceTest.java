package com.abolore.ledger_pay.bachs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abolore.ledger_pay.webhook.bachs.BachsWebhookEventRepository;
import com.abolore.ledger_pay.webhook.bachs.BachsWebhookEventService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BachsWebhookEventServiceTest {

    @Mock
    private BachsWebhookEventRepository eventRepository;

    @InjectMocks
    private BachsWebhookEventService eventService;

    @Test
    void claimEvent_returnsTrue_whenNewEventIsInserted() {
        when(eventRepository.claimEvent(
                any(),
                eq("evt_123"),
                eq("collection.succeeded")))
                .thenReturn(1);

        boolean result = eventService.claimEvent(
                "evt_123",
                "collection.succeeded");

        assertTrue(result);

        verify(eventRepository).claimEvent(
                any(),
                eq("evt_123"),
                eq("collection.succeeded"));
    }

    @Test
    void claimEvent_returnsFalse_whenEventIsDuplicate() {
        when(eventRepository.claimEvent(
                any(),
                eq("evt_123"),
                eq("collection.succeeded")))
                .thenReturn(0);

        boolean result = eventService.claimEvent(
                "evt_123",
                "collection.succeeded");

        assertFalse(result);
    }

    @Test
    void claimEvent_propagatesRepositoryFailure() {
        when(eventRepository.claimEvent(
                any(),
                eq("evt_123"),
                eq("collection.succeeded")))
                .thenThrow(new RuntimeException("Database unavailable"));

        assertThrows(
                RuntimeException.class,
                () -> eventService.claimEvent(
                        "evt_123",
                        "collection.succeeded"));
    }
}