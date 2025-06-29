package cl.metspherical.calbucofelizbackend.features.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for sending SMS messages using Twilio
 */
@Service
public class TwilioService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Sends an SMS message to the specified phone number
     *
     * @param toPhoneNumber The recipient's phone number (must include country code, e.g., +56912345678)
     * @param messageBody The message content
     * @return Message SID if successful
     * @throws RuntimeException if message sending fails
     */
    public String sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),

                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();


            return message.getSid();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Failed to send SMS: " + e.getMessage());
        }
    }

    /**
     * Sends a password recovery SMS with the temporary password
     *
     * @param phoneNumber The recipient's phone number
     * @param temporaryPassword The temporary password to send
     * @return Message SID if successful
     */
    public String sendPasswordRecoverySms(String phoneNumber, String temporaryPassword) {
        String messageBody = String.format(
                "Codigo para CalbucoFeliz es: %s", temporaryPassword
        );
        return sendSms(phoneNumber, messageBody);
    }
}
