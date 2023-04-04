package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){
        //Add the train to the trainRepository
        List<Station> listOfStations = trainEntryDto.getStationRoute();
        StringBuilder route = new StringBuilder();
        for(Station station : listOfStations){
            if(route.length()==0){
                route.append(station.toString());
            }
            else {
                route.append(", ").append(station.toString());
            }
        }


        Train train = new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setRoute(route.toString());

        //and route String logic to be taken from the Problem statement.

        //Save the train and return the trainId that is generated from the database.
        Train updatedTrain = trainRepository.save(train);
        return updatedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        Station toStation = seatAvailabilityEntryDto.getToStation();
        Station fromStation = seatAvailabilityEntryDto.getFromStation();
        int trainId = seatAvailabilityEntryDto.getTrainId();
        Train train = trainRepository.findById(trainId).get();
        int noOfSeats = train.getNoOfSeats();
        List<Ticket> ticketList = train.getBookedTickets();
        int countBookedSeatsBWStations = 0;
        for(Ticket ticket : ticketList){
            if(ticket.getFromStation().equals(fromStation) && ticket.getToStation().equals(toStation)){
                countBookedSeatsBWStations += ticket.getPassengersList().size();
            }
        }
        int leftSeats = noOfSeats - countBookedSeatsBWStations;
        return leftSeats-4;
        //Calculate the total seats available
        //Suppose the route is A -> B -> C -> D
        //and 2 tickets are booked, one from A to B and another from C to D
        //Now suppose if there is only 1 seat available in total
        //then we can say, that seat is available between B to C only and not between any other stations
        //In short : a train has totalNo of seats and there are tickets 'from' and 'to' different locations
        //We need to find out the available seats between the given 2 stations.
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        String[]routeArr = route.split(",");
        if(Arrays.stream(routeArr).noneMatch(thisRoute -> thisRoute.equals(station.name()))){
            throw new Exception("Train is not passing from this station");
        }
        List<Ticket> ticketList = train.getBookedTickets();
        int noOfPeopleBoardingAtAStation = 0;
        for(Ticket ticket : ticketList){
            if(ticket.getFromStation().equals(station)){
                int noOfPeopleBoardingOnThisTicket = ticket.getPassengersList().size();
                noOfPeopleBoardingAtAStation += noOfPeopleBoardingOnThisTicket;
            }
        }
        return noOfPeopleBoardingAtAStation;
        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //in a happy case we need to find out the number of such people.
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        Train train = trainRepository.findById(trainId).get();
        List<Ticket> ticketList = train.getBookedTickets();
        if(ticketList.size()==0){
            return 0;
        }
        int oldestAge = 0;
        for(Ticket ticket : ticketList){
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger : passengerList){
                if(passenger.getAge()>oldestAge){
                    oldestAge = passenger.getAge();
                }
            }
        }
        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){
        List<Integer> trainIdList = new ArrayList<>();
        List<Train> trainList = trainRepository.findAll();
        for(Train train : trainList){
            String route = train.getRoute();
            String[]routeArr = route.split(",");
            if(Arrays.stream(routeArr).anyMatch(thisRoute -> thisRoute.equals(station.name()))){
                //if we are inside this "if" block it means train is certainly going to pass
                //from the given station
                //But now we need to check that the train will pass at what TIME...and if that
                //time falls between the time bracket provided to us, we will add the trainId of this
                //train into the list (to be returned)
                int index = Arrays.asList(routeArr).indexOf(station.name());
                LocalTime trainDepartureTime = train.getDepartureTime();
                LocalTime timeOfPassing = trainDepartureTime.plusHours(index);
                if(timeOfPassing.isAfter(startTime) && timeOfPassing.isBefore(endTime)){
                    trainIdList.add(train.getTrainId());
                }
            }
        }
        return trainIdList;
        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame.
        //You can assume that the date change doesn't need to be done i.e. the travel will certainly happen within the same date (More details
        //in problem statement)
        //You can also consider seconds and milliseconds as 0 in a LocalTime format.
    }
}