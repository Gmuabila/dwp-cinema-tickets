package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Optional;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    public static final int TWENTY = 20;
    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;

    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }


    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateRequest(accountId, ticketTypeRequests);
        final int totalTickets = getTotalNumberOfTickets(ticketTypeRequests);

        final Integer totalPrice = Arrays.stream(ticketTypeRequests)
                .filter(request -> !request.getTicketType().equals(TicketTypeRequest.Type.INFANT))
                .map(request -> request.getTicketType().getPrice() * request.getNoOfTickets())
                .reduce(Integer::sum)
                .orElse(0);

        seatReservationService.reserveSeat(accountId, totalTickets);
        ticketPaymentService.makePayment(accountId, totalPrice);

    }

    private static int getTotalNumberOfTickets(TicketTypeRequest[] ticketTypeRequests){
        return  Arrays.stream(ticketTypeRequests)
                .filter(request -> !request.getTicketType().equals(TicketTypeRequest.Type.INFANT))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);
    }

    private static void validateRequest(Long accountId, TicketTypeRequest[] ticketTypeRequests) {
        if (accountId < 1){
            throw new InvalidPurchaseException("Account ID must be greater than Zero");
        }

        if(Optional.ofNullable(ticketTypeRequests).isEmpty() || Arrays.stream(ticketTypeRequests).anyMatch(request -> Optional.ofNullable(request.getTicketType()).isEmpty())){
            throw new InvalidPurchaseException("Ticket request or Ticket type must not be null");
        }

        final int numberOfTickets = getTotalNumberOfTickets(ticketTypeRequests);
        if(numberOfTickets > TWENTY || numberOfTickets < 1){
            throw new InvalidPurchaseException("Ticket request must be greater than zero and less than or equals to twenty");
        }

        final boolean adultPresent = Arrays.stream(ticketTypeRequests).anyMatch(request -> request.getTicketType().equals(TicketTypeRequest.Type.ADULT));
        if(!adultPresent){
            throw new InvalidPurchaseException("Ticket request must have an adult");
        }
    }
}
