package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean discount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		double inHour = ticket.getInTime().getTime();
		double outHour = ticket.getOutTime().getTime();
		double duration = (outHour - inHour) / 1000 / 60 / 60;

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR:
			if (duration <= 0.5) {
				ticket.setPrice(0);
				break;
			} else if (discount == true) {
				ticket.setPrice(Math.round((duration * Fare.CAR_RATE_PER_HOUR * 0.95) * 1000.0) / 1000.0);
				break;
			} else {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				break;
			}
		case BIKE:
			if (duration <= 0.5) {
				ticket.setPrice(0);
				break;
			} else if (discount == true) {
				ticket.setPrice(Math.round((duration * Fare.BIKE_RATE_PER_HOUR * 0.95) * 1000.0) / 1000.0);
				break;
			} else {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				break;
			}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}
	}

	public void calculateFare(Ticket ticket) {
		FareCalculatorService calculateFare = new FareCalculatorService();
		calculateFare.calculateFare(ticket, false);
	}
}