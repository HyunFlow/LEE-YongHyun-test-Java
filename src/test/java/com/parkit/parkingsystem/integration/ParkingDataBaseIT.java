package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	@Order(1)
	public void testParkingACar() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket).isNotNull();
		assertThat(ticket.getInTime()).isNotNull();

		int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		assertThat(nextAvailableSlot).isEqualTo(2);
	}

	@Test
	@Order(2)
	public void testParkingLotExit() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		parkingService.processExitingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket).isNotNull();
		assertThat(ticket.getOutTime()).isNotNull();
		assertThat(ticket.getPrice()).isEqualTo(0);

		ParkingSpot parkingSpot = ticket.getParkingSpot();
		assertThat(parkingSpot.getId()).isEqualTo(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	@Test
	@Order(3)
	public void testParkingLotExitRecurringUser() throws Exception {
		dataBasePrepareService.clearDataBaseEntries();

		// GIVEN
		Ticket oldTicket = new Ticket();
		oldTicket.setVehicleRegNumber("ABCDEF");
		oldTicket.setPrice(1.5);
		oldTicket.setInTime(new Date(System.currentTimeMillis() - (3 * 60 * 60 * 1000)));
		oldTicket.setOutTime(new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000)));
		oldTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

		ticketDAO.saveTicket(oldTicket);

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		ticketDAO.updateTicketInTime("ABCDEF", inTime);
		parkingService.processExitingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket).isNotNull();
		assertThat(ticket.getOutTime()).isNotNull();
		double expectedPrice = Math.round(((1.0 * Fare.CAR_RATE_PER_HOUR) * 0.95) * 1000.0) / 1000.0;
		assertThat(ticket.getPrice()).isEqualTo(expectedPrice);

	}

}
