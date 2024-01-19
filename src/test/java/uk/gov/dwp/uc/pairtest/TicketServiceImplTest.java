package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private TicketPaymentService ticketPaymentService;
    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;
    @Captor
    ArgumentCaptor<Integer> intArgumentCaptor;

    private TicketServiceImpl ticketService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketServiceImpl(seatReservationService, ticketPaymentService);
    }
    @Test
    public void ticketRequestMustNotBull() {
        final InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, null));

        assertTrue(invalidPurchaseException.getMessage().contains("Ticket request or Ticket type must not be null"));
    }

    @Test
    public void ticketTypeMustNotBeNull(){
        TicketTypeRequest nullTicketTypeRequest = new TicketTypeRequest(null, 2);
        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        final InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(47L, nullTicketTypeRequest, adultTicketTypeRequest));

        assertTrue(invalidPurchaseException.getMessage().contains("Ticket request or Ticket type must not be null"));
    }

    @Test
    public void ticketRequestMustNotBeMoreThanTwenty(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        try{
            ticketService.purchaseTickets(1L, ticketTypeRequest);
            fail();
        }catch (InvalidPurchaseException e){
            assertEquals("Ticket request must be greater than zero and less than or equals to twenty", e.getMessage());
        }
    }

    @Test
    public void ticketRequestMustNotBeZero(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);
        try{
            ticketService.purchaseTickets(1L, ticketTypeRequest);
            fail();
        }catch (InvalidPurchaseException e){
            assertEquals("Ticket request must be greater than zero and less than or equals to twenty", e.getMessage());
        }
    }

    @Test
    public void ticketRequestMustHaveAnAdult(){
        TicketTypeRequest childTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        try{
            ticketService.purchaseTickets(1L, childTicketTypeRequest, infantTicketTypeRequest);
            fail();
        }catch (InvalidPurchaseException e){
            assertEquals("Ticket request must have an adult", e.getMessage());
        }
    }

    @Test
    public void accountIdMustBeGreaterThanZero(){
        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infantTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        try{
            ticketService.purchaseTickets(0L, adultTicketTypeRequest, childTicketTypeRequest, infantTicketTypeRequest);
            fail();
        }catch (InvalidPurchaseException e){
            assertEquals("Account ID must be greater than Zero", e.getMessage());
        }
    }

    @Test
    public void mustBeAbleToBookAdultOnlyTickets(){
        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);

        doNothing().when(seatReservationService).reserveSeat(24L, 4);
        doNothing().when(ticketPaymentService).makePayment(24L, 80);

        ticketService.purchaseTickets(24L, adultTicketTypeRequest);

        verify(seatReservationService).reserveSeat(longArgumentCaptor.capture(), intArgumentCaptor.capture());
        assertEquals(24, longArgumentCaptor.getValue());
        assertEquals(4, intArgumentCaptor.getValue());
        verify(ticketPaymentService).makePayment(longArgumentCaptor.capture(), intArgumentCaptor.capture());
        assertEquals(24, longArgumentCaptor.getValue());
        assertEquals(80, intArgumentCaptor.getValue());
    }

    @Test
    public void mustBeAbleToRequestMultipleTickets(){
        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest childTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6);
        TicketTypeRequest infantTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4);

        doNothing().when(seatReservationService).reserveSeat(451L, 16);
        doNothing().when(ticketPaymentService).makePayment(451L, 260);

        ticketService.purchaseTickets(451L, adultTicketTypeRequest, childTicketTypeRequest, infantTicketTypeRequest);

        verify(seatReservationService).reserveSeat(451L, 16);
        verify(ticketPaymentService).makePayment(451L, 260);
    }

    @Test
    public void mustBookOneAdultOneChildWithInfant(){

        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        doNothing().when(seatReservationService).reserveSeat(367L, 2);
        doNothing().when(ticketPaymentService).makePayment(367L, 30);

        ticketService.purchaseTickets(367L, adultTicketTypeRequest, childTicketTypeRequest, infantTicketTypeRequest);

        verify(seatReservationService).reserveSeat(367L, 2);
        verify(ticketPaymentService).makePayment(367L, 30);
    }

    @Test
    public void mustBeAbleToBookTwentyTickets(){
        TicketTypeRequest adultTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 12);
        TicketTypeRequest childTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 8);
        TicketTypeRequest infantTicketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        doNothing().when(seatReservationService).reserveSeat(501L, 20);
        doNothing().when(ticketPaymentService).makePayment(501L, 320);

        ticketService.purchaseTickets(501L, adultTicketTypeRequest, childTicketTypeRequest, infantTicketTypeRequest);

        verify(seatReservationService).reserveSeat(501L, 20);
        verify(ticketPaymentService).makePayment(501L, 320);
    }
}
