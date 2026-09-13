package com.tsynytsyna.animations.production_components.ticket

import androidx.compose.ui.graphics.Color

data class FlightTicket(
    val id: String,
    val airline: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val originTerminal: String,
    val destinationCode: String,
    val destinationCity: String,
    val destinationTerminal: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val passengerName: String,
    val seat: String,
    val cabinClass: String,
    val gate: String,
    val boardingGroup: String,
    val barcodeNumber: String,
    val accentColor: Color,
    val paperColor: Color = Color(0xFFFBF9F5)
)

object SampleFlightTickets {
    val tokyoFlight = FlightTicket(
        id = "JL001",
        airline = "JAPAN AIRLINES",
        flightNumber = "JL 001",
        originCode = "SFO",
        originCity = "SAN FRANCISCO",
        originTerminal = "T-INTL",
        destinationCode = "HND",
        destinationCity = "TOKYO HANEDA",
        destinationTerminal = "T-3",
        departureTime = "11:45 AM",
        arrivalTime = "03:15 PM +1",
        duration = "10h 30m",
        passengerName = "DR. ALEXANDER CHEN",
        seat = "02A",
        cabinClass = "FIRST CLASS",
        gate = "B14",
        boardingGroup = "GROUP 1",
        barcodeNumber = "0819 4482 9912 001",
        accentColor = Color(0xFFE11D48)
    )

    val londonFlight = FlightTicket(
        id = "BA178",
        airline = "BRITISH AIRWAYS",
        flightNumber = "BA 178",
        originCode = "JFK",
        originCity = "NEW YORK",
        originTerminal = "T-7",
        destinationCode = "LHR",
        destinationCity = "LONDON HEATHROW",
        destinationTerminal = "T-5",
        departureTime = "08:10 PM",
        arrivalTime = "08:25 AM +1",
        duration = "7h 15m",
        passengerName = "SOPHIA ELEANOR VANE",
        seat = "04K",
        cabinClass = "CLUB WORLD",
        gate = "G22",
        boardingGroup = "GROUP 2",
        barcodeNumber = "1254 9901 8831 178",
        accentColor = Color(0xFF2563EB)
    )

    val parisFlight = FlightTicket(
        id = "AF066",
        airline = "AIR FRANCE",
        flightNumber = "AF 066",
        originCode = "CDG",
        originCity = "PARIS ROISSY",
        originTerminal = "2E",
        destinationCode = "DXB",
        destinationCity = "DUBAI INTL",
        destinationTerminal = "T-1",
        departureTime = "01:30 PM",
        arrivalTime = "11:10 PM",
        duration = "6h 40m",
        passengerName = "MARCUS AURELIUS",
        seat = "01F",
        cabinClass = "LA PREMIÈRE",
        gate = "K44",
        boardingGroup = "PREMIER",
        barcodeNumber = "4401 2293 8810 066",
        accentColor = Color(0xFFD97706)
    )

    val all = listOf(tokyoFlight, londonFlight, parisFlight)
}
