package com.roy.morago.service.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.exception.call.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.service.SetupHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SuppressWarnings("SpringBootApplicationProperties")
@Testcontainers
@Transactional
@SpringBootTest(properties = {
        "app.jwt.secret=vLp7X9mQ2sR8tY3uF5jH6kL1nB4cD8eF0gH2jK3lP5qR7tY9u"
})
public class CallServiceIT {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private CallService callService;
    @Autowired
    private CallHelper callHelper;
    @Autowired
    private SetupHelper setupHelper;

    private User testClient;
    private User testTranslator;
    private Wallet testWalletClient;
    private Wallet testWalletTranslator;
    private Call testCall;
    private CallRequest testCallRequest;
    private CallResponse testCallResponse;

    public CallRequest createTestCallRequest() {
        return new CallRequest(
                testClient.getId(),
                testTranslator.getId(),
                1L
        );
    }

    @BeforeEach
    public void setUp() {
        testClient = setupHelper.createTestClient();
        testWalletClient = setupHelper.createTestWallet(testClient);
        testTranslator = setupHelper.createTestTranslator();
        testWalletTranslator = setupHelper.createTestWallet(testTranslator);
        testCallRequest = createTestCallRequest();
    }

    @Test
    void testRequestCallAsClient() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCall = callHelper.findCallById(testCallResponse.id());
        assertThat(testCall.getClient().getId()).isEqualTo(testClient.getId());
        assertThat(testCall.getTranslator().getId()).isEqualTo(testTranslator.getId());
        assertThat(testCall.getStatus()).isEqualTo(CallStatus.RINGING);
        assertThat(testCall.getIsClientInitiator()).isEqualTo(true);
    }

    @Test
    void testAcceptCallAsTranslator() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCall = callHelper.findCallById(testCallResponse.id());

        assertThat(testCall.getClient().getId()).isEqualTo(testClient.getId());
        assertThat(testCall.getTranslator().getId()).isEqualTo(testTranslator.getId());
        assertThat(testCall.getStatus()).isEqualTo(CallStatus.ACCEPTED);
        assertThat(testCall.getIsClientInitiator()).isEqualTo(true);
        assertNotNull(testCall.getAcceptedAt());
    }

    @Test
    void testStartCall() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());

        assertThat(testCall.getStatus()).isEqualTo(CallStatus.IN_PROGRESS);
        assertNotNull(testCall.getStartedAt());
    }

    @Test
    void testEndCall_clientIsCaller() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());

        testCall.setStartedAt(LocalDateTime.now().minusSeconds(59));

        testCallResponse = callService.endCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());

        assertThat(testCall.getStatus()).isEqualTo(CallStatus.ENDED);
        assertNotNull(testCall.getEndedAt());
        assertThat(testCall.getCost()).isEqualTo(1000L);
        assertThat(testWalletClient.getBalance()).isEqualTo(0L);
        assertThat(testWalletTranslator.getBalance()).isEqualTo(2000L);
        assertThat(testCall.getIsClientInitiator()).isEqualTo(true);
    }

    @Test
    void testEndCall_translatorIsCaller() {
        testCallResponse = callService.requestCall(testCallRequest, testTranslator);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testClient);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());

        testCall.setStartedAt(LocalDateTime.now().minusSeconds(59));

        testCallResponse = callService.endCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());

        assertThat(testCall.getStatus()).isEqualTo(CallStatus.ENDED);
        assertNotNull(testCall.getEndedAt());
        assertThat(testCall.getCost()).isEqualTo(1000L);
        assertThat(testWalletClient.getBalance()).isEqualTo(0L);
        assertThat(testWalletTranslator.getBalance()).isEqualTo(2000L);
        assertThat(testCall.getIsClientInitiator()).isEqualTo(false);
    }

    @Test
    void testGetCall() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCall = callHelper.findCallById(testCallResponse.id());

        CallResponse callResponse = callService.getCall(testCall.getId());
        assertThat(callResponse.clientId()).isEqualTo(testClient.getId());
        assertThat(callResponse.translatorId()).isEqualTo(testTranslator.getId());
        assertThat(callResponse.status()).isEqualTo(CallStatus.RINGING);
    }

    @Test
    void testCancelCall() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCall = callHelper.findCallById(testCallResponse.id());
        testCallResponse = callService.cancelCall(testCall.getId(), testClient);
        testCall = callHelper.findCallById(testCallResponse.id());

        assertThat(testCall.getStatus()).isEqualTo(CallStatus.CANCELED);
        assertNotNull(testCall.getCanceledAt());
    }

    @Test
    void testDeclineCall() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCall = callHelper.findCallById(testCallResponse.id());
        testCallResponse = callService.declineCall(testCall.getId(), testTranslator);

        assertThat(testCall.getStatus()).isEqualTo(CallStatus.DECLINED);
        assertNotNull(testCall.getCanceledAt());
    }

    @Test
    void testRateCall() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());
        testCall.setStartedAt(LocalDateTime.now().minusSeconds(59));
        testCallResponse = callService.endCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());
        callService.rateCall(testCall.getId(), 4);
        assertThat(testCall.getRating()).isEqualTo(4);
    }

    @Test
    void testRequestCall_deficientWalletBalance_throwsException() {
        testWalletClient.setBalance(400L);
        assertThatThrownBy(() -> callService.requestCall(testCallRequest, testClient))
                .isInstanceOf(DeficientFundsException.class);
    }

    @Test
    void testAcceptCall_asCaller_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        assertThatThrownBy(() -> callService.acceptCall(testCallResponse.id(), testClient))
                .isInstanceOf(InvalidCallRecipientException.class);
    }

    @Test
    void testCancelCall_asRecipient_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        assertThatThrownBy(() -> callService.cancelCall(testCallResponse.id(), testTranslator))
                .isInstanceOf(InvalidCallerException.class);
    }

    @Test
    void testDeclineCall_asCaller_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        assertThatThrownBy(() -> callService.declineCall(testCallResponse.id(), testClient))
                .isInstanceOf(InvalidCallRecipientException.class);
    }

    @Test
    void testStartCall_requested_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        assertThatThrownBy(() -> callService.startCall(testCallResponse.id()))
                .isInstanceOf(InvalidCallStateException.class);
    }

    @Test
    void testRateCall_notEnded_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());
        assertThatThrownBy(() -> callService.rateCall(testCallResponse.id(), 4))
                .isInstanceOf(InvalidCallStateException.class);
    }

    @Test
    void testRateCall_ratingNotValid_throwsException() {
        testCallResponse = callService.requestCall(testCallRequest, testClient);
        testCallResponse = callService.acceptCall(testCallResponse.id(), testTranslator);
        testCallResponse = callService.startCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());
        testCall.setStartedAt(LocalDateTime.now().minusSeconds(59));
        testCallResponse = callService.endCall(testCallResponse.id());
        testCall = callHelper.findCallById(testCallResponse.id());
        callService.rateCall(testCall.getId(), 4);
        assertThatThrownBy(() -> callService.rateCall(testCallResponse.id(), 6))
                .isInstanceOf(InvalidCallRatingException.class);
    }

    @Test
    void testGetCall_notFound_throwsException() {
        assertThatThrownBy(() -> callService.getCall(-1L))
                .isInstanceOf(CallNotFoundException.class);
    }
}
