package cl.metspherical.calbucofelizbackend.features.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwilioServiceTest {

    @InjectMocks
    private TwilioService twilioService;

    @Mock
    private com.twilio.rest.api.v2010.account.MessageCreator creator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twilioService, "accountSid", "ACxxxxxxxxxxxxxxxxx");
        ReflectionTestUtils.setField(twilioService, "authToken", "authxxxxxxxxxxxxxxx");
        ReflectionTestUtils.setField(twilioService, "twilioPhoneNumber", "+1234567890");
    }

    @Test
    void testSendSmsSuccess() {
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class);
             MockedStatic<Message> messageMockedStatic = Mockito.mockStatic(Message.class)) {

            twilioMockedStatic.when(() -> Twilio.init(any(String.class), any(String.class))).thenAnswer(invocation -> null);
            twilioService.init(); // Call init to ensure Twilio is initialized

            Message messageMock = mock(Message.class);
            when(messageMock.getSid()).thenReturn("SMxxxxxxxxxxxxxxxxx");

            messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class)))
                    .thenReturn(creator);
            when(creator.create()).thenReturn(messageMock);

            String messageSid = twilioService.sendSms("+56912345678", "Test message");
            assertEquals("SMxxxxxxxxxxxxxxxxx", messageSid);
        }
    }

    @Test
    void testSendSmsFailure() {
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class);
             MockedStatic<Message> messageMockedStatic = Mockito.mockStatic(Message.class)) {

            twilioMockedStatic.when(() -> Twilio.init(any(String.class), any(String.class))).thenAnswer(invocation -> null);
            twilioService.init();

            messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class)))
                    .thenReturn(creator);
            when(creator.create()).thenThrow(new RuntimeException("Twilio error"));

            assertThrows(ResponseStatusException.class, () -> {
                twilioService.sendSms("+56912345678", "Test message");
            });
        }
    }

    @Test
    void testSendPasswordRecoverySms() {
        try (MockedStatic<Twilio> twilioMockedStatic = Mockito.mockStatic(Twilio.class);
             MockedStatic<Message> messageMockedStatic = Mockito.mockStatic(Message.class)) {

            twilioMockedStatic.when(() -> Twilio.init(any(String.class), any(String.class))).thenAnswer(invocation -> null);
            twilioService.init(); // Call init to ensure Twilio is initialized

            Message messageMock = mock(Message.class);
            when(messageMock.getSid()).thenReturn("SMxxxxxxxxxxxxxxxxx");

            messageMockedStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(String.class)))
                    .thenReturn(creator);
            when(creator.create()).thenReturn(messageMock);

            String temporaryPassword = "testPassword";
            String expectedMessageBody = String.format("Codigo para CalbucoFeliz es: %s", temporaryPassword);

            twilioService.sendPasswordRecoverySms("+56912345678", temporaryPassword);

            // Verify that Message.creator was called with the correct message body
            // This is a bit tricky due to static mocking, direct verification of arguments is complex.
            // Instead, we rely on the structure and the fact that if sendSms is called,
            // it must have used the formatted message.
            // A more robust way would be to capture arguments if possible with the mocking framework.
            // For now, this test ensures the method runs and calls the underlying sendSms.
             messageMockedStatic.verify(() -> Message.creator(
                new PhoneNumber("+56912345678"),
                new PhoneNumber("+1234567890"),
                expectedMessageBody
            ));
        }
    }
}
