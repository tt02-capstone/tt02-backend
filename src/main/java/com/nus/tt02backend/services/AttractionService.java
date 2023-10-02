package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.TicketEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

@Service
public class AttractionService {
    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    TicketPerDayRepository ticketPerDayRepository;

    @Autowired
    SeasonalActivityRepository seasonalActivityRepository;

    public User findUser(Long userId) throws NotFoundException {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                throw new NotFoundException("User not found!");
            }
        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Tourist findTourist(Long touristId) throws NotFoundException {
        try {
            Tourist tourist = touristRepository.getTouristByUserId(touristId);
            if (tourist != null) {
                return tourist;
            } else {
                throw new NotFoundException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Local findLocal(Long localId) throws NotFoundException {
        try {
            Local local = localRepository.getLocalByUserId(localId);
            if (local != null) {
                return local;
            } else {
                throw new NotFoundException("Local not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public VendorStaff retrieveVendor(Long vendorStaffId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorStaffId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public List<Attraction> retrieveAllAttraction() {
        return attractionRepository.findAll();
    }

    public List<Attraction> retrieveAllPublishedAttraction() { // for mobile view
        List<Attraction> attractionList = attractionRepository.findAll();
        List<Attraction> publishedList = new ArrayList<>();
        for (Attraction a : attractionList) {
            if (a.getIs_published() == Boolean.TRUE) {
                publishedList.add(a);
            }
        }

        return publishedList;
    }

    public Attraction retrieveAttraction(Long attractionId) throws NotFoundException {
        try {
            Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);
            if (attractionOptional.isPresent()) {
                return attractionOptional.get();
            } else {
                throw new NotFoundException("Attraction not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<Attraction> retrieveAllAttractionByVendor(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        if (!vendor.getAttraction_list().isEmpty()) {
            return vendor.getAttraction_list();
        } else {
            throw new NotFoundException("Attractions not found!");
        }
    }

    public Attraction retrieveAttractionByVendor(Long vendorStaffId, Long attractionId) throws NotFoundException {
        List<Attraction> attractionList = retrieveAllAttractionByVendor(vendorStaffId);
        for (Attraction a : attractionList) {
            if (a.getAttraction_id().equals(attractionId)) {
                return a;
            }
        }
        throw new NotFoundException("Attraction not found!"); // if the attraction is not part of vendor's listing
    }

    public PriceTierEnum priceTierEstimation(List<Price> price_list) {
        // do an avg pricing by using 2 adult + 1 child + 1 senior for attractions
        // based on tourist pricing for now + logic can check in the future
        // tier 1 = 0 - 50 , tier 2 = 51 - 100 , tier 3 = 101 - 200, tier 4 > 200

        BigDecimal total = new BigDecimal("0");
        TicketEnum child = TicketEnum.CHILD;
        TicketEnum adult = TicketEnum.ADULT;
        TicketEnum senior = TicketEnum.SENIOR;

        for (Price price : price_list) {
            if (price.getTicket_type().equals(child)) {
                BigDecimal amt = price.getTourist_amount();
                total = total.add(amt);
            } else if (price.getTicket_type().equals(senior)) {
                BigDecimal amt = price.getTourist_amount();
                total = total.add(amt);
            } else if (price.getTicket_type().equals(adult)) {
                BigDecimal amt = price.getTourist_amount();
                BigDecimal two = new BigDecimal("2");
                BigDecimal amt2 = amt.multiply(two);
                total = total.add(amt2);
            }
        }

        BigDecimal tier1Min = new BigDecimal("0");
        BigDecimal tier1 = new BigDecimal("50");
        BigDecimal tier2 = new BigDecimal("100");
        BigDecimal tier3 = new BigDecimal("150");
        BigDecimal tier4 = new BigDecimal("200");

        if (total.compareTo(tier1Min) >= 0 && total.compareTo(tier1) <= 0) {
            return PriceTierEnum.TIER_1;
        } else if (total.compareTo(tier1) >= 0 && total.compareTo(tier2) <= 0 ) {
            return PriceTierEnum.TIER_2;
        } else if (total.compareTo(tier2) >= 0 && total.compareTo(tier3) <= 0) {
            return PriceTierEnum.TIER_3;
        } else if (total.compareTo(tier3) >= 0 && total.compareTo(tier4) <= 0) {
            return PriceTierEnum.TIER_4;
        } else {
            return PriceTierEnum.TIER_5;
        }
    }

    public List<Price> createPriceList(List<Price> price_list) {
        List<Price> create_price_list = new ArrayList<Price>();

        for (Price input : price_list) {
            Price price = new Price();
            price.setLocal_amount(input.getLocal_amount());
            price.setTourist_amount(input.getTourist_amount());
            price.setTicket_type(input.getTicket_type());
            priceRepository.save(price);

            create_price_list.add(price);
        }

        return create_price_list;
    }

    public List<Price> updatePriceList(List<Price> price_list) throws NotFoundException {
        List<Price> update_price_list = new ArrayList<Price>();

        if (price_list != null) {
            for (Price input : price_list) {

                if (input.getPrice_id() == null) {
                    Price price = new Price();
                    price.setLocal_amount(input.getLocal_amount());
                    price.setTourist_amount(input.getTourist_amount());
                    price.setTicket_type(input.getTicket_type());
                    priceRepository.save(price);

                    update_price_list.add(price);
                } else {
                    Price price = priceRepository.findById(input.getPrice_id()).orElseThrow(() -> new NotFoundException("Pricing Not Found!"));

                    price.setLocal_amount(input.getLocal_amount());
                    price.setTourist_amount(input.getTourist_amount());
                    price.setTicket_type(input.getTicket_type());
                    priceRepository.save(price);

                    update_price_list.add(price);
                }
            }
        }
        return update_price_list;
    }

    public Attraction createAttraction(VendorStaff vendorStaff, Attraction attractionToCreate) throws BadRequestException {
        Attraction attraction = attractionRepository.getAttractionByName((attractionToCreate.getName()));

        if (attraction != null) {
            throw new BadRequestException("There is an attraction listing with the same name, please choose another name!");
        }

        List<Price> price_list = attractionToCreate.getPrice_list(); // get the price list and process them as price obj
        if (price_list != null) {
            List<Price> persisted_price_list = createPriceList(price_list);
            PriceTierEnum priceTier = priceTierEstimation(persisted_price_list);

            attractionToCreate.setPrice_list(persisted_price_list); // set the price list w the newly created price objs
            attractionToCreate.setEstimated_price_tier(priceTier);
        }

        Attraction newAttraction = attractionRepository.save(attractionToCreate);

        Vendor vendor = vendorStaff.getVendor();
        List<Attraction> currentList = vendor.getAttraction_list();
        currentList.add(newAttraction);
        vendor.setAttraction_list(currentList); // set new attraction for the vendor
        System.out.println(vendor.getAttraction_list());

        vendorStaffRepository.save(vendorStaff); // update the vendor staff db

        return newAttraction;
    }

    public void updateAttraction(VendorStaff vendorStaff, Attraction attractionToUpdate) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attractionToUpdate.getAttraction_id())
                .orElseThrow(() -> new NotFoundException("Attraction Not Found!"));
        if (attractionToUpdate.getName() != null && attractionToUpdate.getOpening_hours() != null && attractionToUpdate.getContact_num() != null &&
                attractionToUpdate.getIs_published() != null && !attractionToUpdate.getPrice_list().isEmpty()) {

            attraction.setName(attractionToUpdate.getName());
            attraction.setDescription(attractionToUpdate.getDescription());
            attraction.setAddress(attractionToUpdate.getAddress());
            attraction.setOpening_hours(attractionToUpdate.getOpening_hours());
            attraction.setAge_group(attractionToUpdate.getAge_group());
            attraction.setContact_num(attractionToUpdate.getContact_num());
            attraction.setIs_published(attractionToUpdate.getIs_published());
            attraction.setSuggested_duration(attractionToUpdate.getSuggested_duration());
            attraction.setAttraction_category(attractionToUpdate.getAttraction_category());
            attraction.setGeneric_location(attractionToUpdate.getGeneric_location());
            attraction.setAttraction_image_list(attractionToUpdate.getAttraction_image_list());

            List<Price> updatedPriceList = updatePriceList(attractionToUpdate.getPrice_list());
            PriceTierEnum updatedTier = priceTierEstimation(updatedPriceList);

            attraction.setPrice_list(updatedPriceList);
            attraction.setEstimated_price_tier(updatedTier);
        }

        attractionRepository.save(attraction);
    }

    public List<Attraction> retrieveAllSavedAttractionsForTouristAndLocal(Long userId) throws NotFoundException, BadRequestException {
        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);

        if (currentUser.getUser_type().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            return tourist.getAttraction_list();
        } else if (currentUser.getUser_type().equals(localType)) {
            Local local = findLocal(userId);
            return local.getAttraction_list();
        } else {
            throw new BadRequestException("Current user type not tourist or local");
        }
    }

    public User saveAttractionForTouristAndLocal (Long userId, Long currentAttractionId) throws BadRequestException, NotFoundException {
        Attraction attractionToSave = retrieveAttraction(currentAttractionId);
        User currentUser = findUser(userId);
        List<Attraction> currentSavedAttractions = new ArrayList<Attraction>();

        if (!attractionToSave.getIs_published()) {
            throw new BadRequestException("Can't save a hidden attraction!"); // shouldn't trigger if thr is a frontend
        }

        if (currentUser.getUser_type().equals(UserTypeEnum.TOURIST)) {
            currentSavedAttractions = ((Tourist) currentUser).getAttraction_list();
        } else if (currentUser.getUser_type().equals(UserTypeEnum.LOCAL)) {
            currentSavedAttractions = ((Local) currentUser).getAttraction_list();
        } else {
            throw new BadRequestException("Invalid User Type! Only Local or Tourist can save an attraction!");
        }

        for (Attraction a : currentSavedAttractions) {
            if (a.getAttraction_id().equals(currentAttractionId)) {
                throw new BadRequestException("You have already saved this attraction!");
            }
        }

        currentSavedAttractions.add(attractionToSave);
        userRepository.save(currentUser);

        if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
            Local local = (Local) currentUser;
            local.setComment_list(null);
            local.setPost_list(null);
            local.setSupport_ticket_list(null);
            local.setBooking_list(null);
        } else {
            Tourist tourist = (Tourist) currentUser;
            tourist.setComment_list(null);
            tourist.setPost_list(null);
            tourist.setSupport_ticket_list(null);
            tourist.setBooking_list(null);
        }

        return currentUser;
    }

    public User removeSavedAttractionForTouristAndLocal (Long userId, Long currentAttractionId) throws NotFoundException {
        User currentUser = findUser(userId);
        List<Attraction> currentSavedAttractions = new ArrayList<Attraction>();

        if (currentUser.getUser_type() == UserTypeEnum.TOURIST) {
            Tourist tourist = findTourist(userId);
            currentSavedAttractions = tourist.getAttraction_list();
        } else if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
            Local local = findLocal(userId);
            currentSavedAttractions = local.getAttraction_list();
        }

        for (Attraction a : currentSavedAttractions) {
            if (a.getAttraction_id().equals(currentAttractionId)) {
                currentSavedAttractions.remove(a);
                if (currentUser.getUser_type() == UserTypeEnum.TOURIST) {
                    touristRepository.save((Tourist) currentUser);
                } else if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
                    localRepository.save((Local) currentUser);
                }

                if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
                    Local local = (Local) currentUser;
                    local.setComment_list(null);
                    local.setPost_list(null);
                    local.setSupport_ticket_list(null);
                    local.setBooking_list(null);
                } else {
                    Tourist tourist = (Tourist) currentUser;
                    tourist.setComment_list(null);
                    tourist.setPost_list(null);
                    tourist.setSupport_ticket_list(null);
                    tourist.setBooking_list(null);
                }
                
                return currentUser;
            }
        }

        throw new NotFoundException("Attraction not found in the saved list!");
    }

    private TicketPerDay attractionContainsTicketForDate(List<TicketPerDay> list, LocalDate date, TicketEnum type) {
        for (TicketPerDay t : list) {
            if (t.getTicket_date().isEqual(date) && t.getTicket_type() == type) return t;
        }
        return null;
    }

    public List<TicketPerDay> createTicketsPerDayList(LocalDate startDate, LocalDate endDate, TicketEnum ticketType, int ticketCount, Long attraction_id) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attraction_id)
                .orElseThrow(() -> new NotFoundException("Attraction not found when saving ticket per day!"));

        long duration = ChronoUnit.DAYS.between(startDate, endDate);

        if (attraction.getTicket_per_day_list() == null) attraction.setTicket_per_day_list(new ArrayList<>());
        List<TicketPerDay> ticketList = attraction.getTicket_per_day_list();

        for (int i = 0; i <= duration; i++) {
            LocalDate ticketDate = startDate.plusDays(i);
            // check if there is an entity created for that day, if so update instead of creating a new one
            TicketPerDay ticket = attractionContainsTicketForDate(ticketList, ticketDate, ticketType);
            if (ticket != null) {
                ticket.setTicket_count(ticketCount);
                ticketPerDayRepository.save(ticket);
            } else { // create new entity
                ticket = new TicketPerDay();
                ticket.setTicket_count(ticketCount);
                ticket.setTicket_date(ticketDate);
                ticket.setTicket_type(ticketType);
                ticketPerDayRepository.save(ticket);
                ticketList.add(ticket);
            }
        }
        attractionRepository.save(attraction);

        return ticketList;

//        Old Code by Ci En
//        List<TicketPerDay> createdTickets = new ArrayList<>();
//        List<TicketPerDay> previouslyCreated = new ArrayList<>();
//
//        if (!attraction.getTicket_per_day_list().isEmpty()) {
//            previouslyCreated = attraction.getTicket_per_day_list();
//        }
//
//        long duration = ChronoUnit.DAYS.between(startDate, endDate);
//
//        for (int i = 0; i <= duration; i++) {
//            LocalDate ticketDate = startDate.plusDays(i);
//            TicketPerDay ticketPerDay = new TicketPerDay();
//            ticketPerDay.setTicket_count(ticketCount);
//            ticketPerDay.setTicket_date(ticketDate);
//            ticketPerDay.setTicket_type(ticketType);
//
//            ticketPerDayRepository.save(ticketPerDay);
//
//            createdTickets.add(ticketPerDay);
//        }
//
//        if (!previouslyCreated.isEmpty()) {
//            previouslyCreated.addAll(createdTickets);
//            attraction.setTicket_per_day_list(previouslyCreated);
//        } else {
//            attraction.setTicket_per_day_list(createdTickets);
//        }
//
//        attractionRepository.save(attraction);
//        return createdTickets;
    }

    public List<TicketPerDay> updateTicketsPerDay(Long attraction_id, TicketPerDay toUpdateTicket) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attraction_id)
                .orElseThrow(() -> new NotFoundException("Attraction not found when saving ticket per day!"));

        List<TicketPerDay> exisitingTicketList = attraction.getTicket_per_day_list();
        List<TicketPerDay> updatedList = new ArrayList<TicketPerDay>();
        boolean checker = false;

        for (TicketPerDay t : exisitingTicketList) {
            if (t.getTicket_date().equals(toUpdateTicket.getTicket_date()) && t.getTicket_type().equals(toUpdateTicket.getTicket_type())) { // use date instead of id
                t.setTicket_count(toUpdateTicket.getTicket_count());
                ticketPerDayRepository.save(t);
                updatedList.add(t);
                checker = true;
            } else {
                updatedList.add(t);
            }
        }

        if (checker) {
            attraction.setTicket_per_day_list(updatedList);
            return updatedList;
        } else {
            throw new NotFoundException("Selected Date or Ticket Type not found!");
        }
    }

    public List<TicketPerDay> getAllTickets() {
        return ticketPerDayRepository.findAll();
    }

    public List<TicketPerDay> getAllTicketListedByAttraction(Long attraction_id) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attraction_id)
                .orElseThrow(() -> new NotFoundException("Attraction not found when getting list of tickets per day!"));

        if (attraction.getTicket_per_day_list().isEmpty()) {
            throw new NotFoundException("No tickets created for this attraction listing!");
        }

        return attraction.getTicket_per_day_list();
    }

    public List<TicketPerDay> getAllTicketListedByAttractionInTimeRange(Long id, LocalDate startDate, LocalDate endDate) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(id).orElseThrow(() -> new NotFoundException("Attraction not found!"));
        List<TicketPerDay> wantedList = new ArrayList<>();
        for (TicketPerDay t : attraction.getTicket_per_day_list()) {
            if (!t.getTicket_date().isBefore(startDate) && !t.getTicket_date().isAfter(endDate)) {
                wantedList.add(t);
            }
        }
        return wantedList;
    }

    public List<TicketPerDay> getAllTicketListedByAttractionAndDate(Long attraction_id,LocalDate inputDate) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attraction_id)
                .orElseThrow(() -> new NotFoundException("Attraction not found when getting list of tickets per day!"));

        List<TicketPerDay> ticketList = attraction.getTicket_per_day_list();
        List<TicketPerDay> selectedTicketList = new ArrayList<TicketPerDay>();

        if (ticketList.isEmpty()) {
            throw new NotFoundException("No tickets created for this attraction listing!");
        } else {
            for (TicketPerDay t : ticketList) {
                if (t.getTicket_date().equals(inputDate)) {
                    selectedTicketList.add(t);
                }
            }
        }

        return selectedTicketList;
    }

    public List<TicketEnum> getTicketEnumByAttraction(Long attraction_id) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attraction_id)
                .orElseThrow(() -> new NotFoundException("Attraction to find price list not found!"));

        List<TicketEnum> ticketTypes = new ArrayList<TicketEnum>();
        if (!attraction.getPrice_list().isEmpty()) {
            for (Price p : attraction.getPrice_list()) {
                ticketTypes.add(p.getTicket_type());
            }
        }

        return ticketTypes;
    }

    public void checkTicketInventory(Long attraction_id, LocalDate ticketDate, List<TicketPerDay> tickets_to_check) throws NotFoundException, BadRequestException {
        List<TicketPerDay> currentList = getAllTicketListedByAttractionAndDate(attraction_id,ticketDate); // tickets listed based on the date selected
        if (currentList.isEmpty()) {
            throw new NotFoundException("No tickets found for this date!");
        } else {
            for (TicketPerDay ticketToCheck : tickets_to_check) {
                TicketPerDay findTicketType = currentList.stream()
                        .filter(t -> t.getTicket_type().equals(ticketToCheck.getTicket_type()) && t.getTicket_per_day_id().equals(ticketToCheck.getTicket_per_day_id()))
                        .findFirst()
                        .orElse(null);

                if (findTicketType != null && ticketToCheck.getTicket_count() > findTicketType.getTicket_count()) {
                    throw new BadRequestException("Insufficient Inventory for Ticket Type: " + ticketToCheck.getTicket_type());
                }

                if (findTicketType == null) {
                    throw new BadRequestException("Ticket Type: " + ticketToCheck.getTicket_type() + " has been sold out!"); // when tickets r not listed for the particular day
                }
            }
        }
    }

    public Long getLastAttractionId() {
        Long lastAttractionId = attractionRepository.findMaxAttractionId();
        return (lastAttractionId != null) ? lastAttractionId : 0L; // Default to 0 if no attractions exist
    }

    public Attraction createSeasonalActivity(VendorStaff vendorStaff, Long attractionId, SeasonalActivity activityToCreate) throws BadRequestException,  NotFoundException{
        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);

        if (attractionOptional.isEmpty()) {
            throw new NotFoundException("Attraction not found!");
        }

        Attraction attraction = attractionOptional.get();
        List<SeasonalActivity> currentList = attraction.getSeasonal_activity_list();
        for (SeasonalActivity sa : currentList) { // ensure that there isnt overlap in the activities
            if ((activityToCreate.getStart_date().isAfter(sa.getStart_date()) || activityToCreate.getStart_date().isEqual(sa.getStart_date())) &&
                    (activityToCreate.getStart_date().isBefore(sa.getEnd_date()) ||  activityToCreate.getStart_date().isEqual(sa.getEnd_date()))) {
                throw new BadRequestException("There is an exisiting activity which collide with the new activity!");
            }
        }

        SeasonalActivity activity = seasonalActivityRepository.save(activityToCreate);
        currentList.add(activity);

        Vendor vendor = vendorStaff.getVendor();
        List<Attraction> attrList = vendor.getAttraction_list();

        for (Attraction a : attrList) {
            if (a.getAttraction_id().equals(attractionId)) {
                a.setSeasonal_activity_list(currentList); // update vendor's attraction w the new seasonal activity list
                break;
            }
        }

        vendorStaffRepository.save(vendorStaff); // update the vendor staff db
        attractionRepository.save(attraction);

        return attraction;
    }

    public SeasonalActivity getSeasonalActivity(Long attractionId) throws NotFoundException {
        Attraction current = retrieveAttraction(attractionId);
        List<SeasonalActivity> sList = current.getSeasonal_activity_list();

        LocalDate currentDate = LocalDate.now();

        List<SeasonalActivity> filteredList = sList.stream() // to get the activities that fall within the current date
                .filter(activity -> {
                    LocalDate startDate = activity.getStart_date();
                    LocalDate endDate = activity.getEnd_date();
                    return ((currentDate.isAfter(startDate) || currentDate.isEqual(startDate)) && (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)));
                })
                .toList();

        if (filteredList.isEmpty()) {
            throw new NotFoundException("No Seasonal Activity now!");
        } else {
            return filteredList.get(0);
        }

    }

    public List<Attraction> nearbyAttrRecommendation (GenericLocationEnum locationNow) throws NotFoundException {
        List<Attraction> aList = retrieveAllPublishedAttraction();
        List<Attraction> filterList = new ArrayList<>();

        if (aList.isEmpty()) {
            throw new NotFoundException("No attractions are created!");
        } else {
            for (Attraction a : aList) {
                if (a.getGeneric_location() == locationNow) {
                    filterList.add(a);
                }
            }
        }

        if (filterList.isEmpty()) {
            return new ArrayList<>(); // no attraction nearby within the same location
        } else {
            return filterList;
        }
    }

    public List<Attraction> nearbyAttrRecommendation (GenericLocationEnum locationNow, Long attrId) throws NotFoundException {
        List<Attraction> aList = retrieveAllPublishedAttraction();
        List<Attraction> filterList = new ArrayList<>();

        if (aList.isEmpty()) {
            throw new NotFoundException("No attractions are created!");
        } else {
            for (Attraction a : aList) {
                if (a.getGeneric_location() == locationNow && !a.getAttraction_id().equals(attrId)) {
                    filterList.add(a);
                }
            }
        }

        if (filterList.isEmpty()) {
            return new ArrayList<>();
        } else {
            return filterList;
        }
    }

}
