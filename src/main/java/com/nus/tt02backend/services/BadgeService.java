package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.BadgeProgressResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BadgeTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BadgeService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    BadgeRepository badgeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    InternalStaffRepository internalStaffRepository;
    @Autowired
    CategoryItemRepository categoryItemRepository;

    public void awardBadge(User user, UserTypeEnum userType, Long categoryItemId) {
        List<Post> postList = new ArrayList<>();
        Boolean eligibleForBadge = false;
        Boolean eligibleForTopContributor = false;

        Category category = categoryRepository.getCategoryContainingCategoryItem(categoryItemId);
        BadgeTypeEnum badgeType;

        if (category.getName().equals("Attraction")) {
            badgeType = BadgeTypeEnum.ATTRACTION_EXPERT;
        } else if (category.getName().equals("Accommodation")) {
            badgeType = BadgeTypeEnum.ACCOMMODATION_EXPERT;
        } else if (category.getName().equals("Telecom")) {
            badgeType = BadgeTypeEnum.TELECOM_EXPERT;
        } else if (category.getName().equals("Tour")) {
            badgeType = BadgeTypeEnum.TOUR_EXPERT;
        } else {
            badgeType = BadgeTypeEnum.FOODIE;
        }

        if (userType.equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            postList = tourist.getPost_list();

            eligibleForBadge = validateBadgeForTourist(category, tourist);
            if (eligibleForBadge && tourist.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(badgeType))) {
                Badge categoryBadge = new Badge();
                categoryBadge.setBadge_type(badgeType);
                categoryBadge.setBadge_icon(getBadgeIcon(badgeType));
                categoryBadge.setCreation_date(LocalDateTime.now());
                categoryBadge.setIs_primary(false);
                categoryBadge = badgeRepository.save(categoryBadge);

                tourist.getBadge_list().add(categoryBadge);
            }

            eligibleForTopContributor = validateTopContributor(postList);
            if (eligibleForTopContributor && tourist.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(BadgeTypeEnum.TOP_CONTRIBUTOR))) {
                Badge topContributorBadge = new Badge();
                topContributorBadge.setBadge_type(BadgeTypeEnum.TOP_CONTRIBUTOR);
                topContributorBadge.setBadge_icon("https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TOP_CONTRIBUTOR.png");
                topContributorBadge.setCreation_date(LocalDateTime.now());
                topContributorBadge.setIs_primary(false);
                topContributorBadge = badgeRepository.save(topContributorBadge);

                tourist.getBadge_list().add(topContributorBadge);
            }

            if (eligibleForBadge || eligibleForTopContributor) {
                touristRepository.save(tourist);
            }
        } else if (userType.equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            postList = local.getPost_list();

            eligibleForBadge = validateBadgeForLocal(category, local);
            if (eligibleForBadge && local.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(badgeType))) {
                Badge categoryBadge = new Badge();
                categoryBadge.setBadge_type(badgeType);
                categoryBadge.setBadge_icon(getBadgeIcon(badgeType));
                categoryBadge.setCreation_date(LocalDateTime.now());
                categoryBadge.setIs_primary(false);
                categoryBadge = badgeRepository.save(categoryBadge);

                local.getBadge_list().add(categoryBadge);
            }

            eligibleForTopContributor = validateTopContributor(postList);
            if (eligibleForTopContributor && local.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(BadgeTypeEnum.TOP_CONTRIBUTOR))) {
                Badge topContributorBadge = new Badge();
                topContributorBadge.setBadge_type(BadgeTypeEnum.TOP_CONTRIBUTOR);
                topContributorBadge.setBadge_icon("https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TOP_CONTRIBUTOR.png");
                topContributorBadge.setCreation_date(LocalDateTime.now());
                topContributorBadge.setIs_primary(false);
                topContributorBadge = badgeRepository.save(topContributorBadge);

                local.getBadge_list().add(topContributorBadge);
            }

            if (eligibleForBadge || eligibleForTopContributor) {
                localRepository.save(local);
            }
        } else if (userType.equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            postList = vendorStaff.getPost_list();

            eligibleForBadge = validateBadgeForVendorStaff(category, vendorStaff);
            if (eligibleForBadge && vendorStaff.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(badgeType))) {
                Badge categoryBadge = new Badge();
                categoryBadge.setBadge_type(badgeType);
                categoryBadge.setBadge_icon(getBadgeIcon(badgeType));
                categoryBadge.setCreation_date(LocalDateTime.now());
                categoryBadge.setIs_primary(false);
                categoryBadge = badgeRepository.save(categoryBadge);

                vendorStaff.getBadge_list().add(categoryBadge);
            }

            eligibleForTopContributor = validateTopContributor(postList);
            if (eligibleForTopContributor && vendorStaff.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(BadgeTypeEnum.TOP_CONTRIBUTOR))) {
                Badge topContributorBadge = new Badge();
                topContributorBadge.setBadge_type(BadgeTypeEnum.TOP_CONTRIBUTOR);
                topContributorBadge.setBadge_icon("https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TOP_CONTRIBUTOR.png");
                topContributorBadge.setCreation_date(LocalDateTime.now());
                topContributorBadge.setIs_primary(false);
                topContributorBadge = badgeRepository.save(topContributorBadge);

                vendorStaff.getBadge_list().add(topContributorBadge);
            }

            if (eligibleForBadge || eligibleForTopContributor) {
                vendorStaffRepository.save(vendorStaff);
            }
        }
    }

    public Boolean validateTopContributor(List<Post> postList) {
        // For demo purposes
        if (postList.size() >= 4) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean validateBadgeForTourist(Category category, Tourist tourist) {
        List<Post> categoryItemPostList = new ArrayList<>();

        for (CategoryItem categoryItem : category.getCategory_item_list()) {
            categoryItemPostList.addAll(categoryItem.getPost_list());
        }

        Integer numberOfPosts = Math.toIntExact(categoryItemPostList.stream()
                .filter(post -> post.getTourist_user() != null &&
                        post.getTourist_user().getUser_id().equals(tourist.getUser_id())).count());

        // For demo purposes
        if (numberOfPosts >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean validateBadgeForLocal(Category category, Local local) {
        List<Post> categoryItemPostList = new ArrayList<>();

        for (CategoryItem categoryItem : category.getCategory_item_list()) {
            categoryItemPostList.addAll(categoryItem.getPost_list());
        }

        Integer numberOfPosts = Math.toIntExact(categoryItemPostList.stream()
                .filter(post -> post.getLocal_user() != null &&
                        post.getLocal_user().getUser_id().equals(local.getUser_id())).count());

        // For demo purposes
        if (numberOfPosts >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean validateBadgeForVendorStaff(Category category, VendorStaff vendorStaff) {
        List<Post> categoryItemPostList = new ArrayList<>();

        for (CategoryItem categoryItem : category.getCategory_item_list()) {
            categoryItemPostList.addAll(categoryItem.getPost_list());
        }

        Integer numberOfPosts = Math.toIntExact(categoryItemPostList.stream()
                .filter(post -> post.getVendor_staff_user() != null &&
                        post.getVendor_staff_user().getUser_id().equals(vendorStaff.getUser_id())).count());

        // For demo purposes
        if (numberOfPosts >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public String getBadgeIcon(BadgeTypeEnum badgeType) {
        if (badgeType.equals(BadgeTypeEnum.ATTRACTION_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badges/ATTRACTION_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.ACCOMMODATION_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badges/ACCOMMODATION_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.TELECOM_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badges/TELECOM_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.TOUR_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badges/TOUR_EXPERT.png";
        } else {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badges/FOODIE.png";
        }
    }

    public Badge awardedNewBadge(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        List<Badge> badgeList = new ArrayList<>();
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            badgeList = tourist.getBadge_list();
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            badgeList = local.getBadge_list();
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            badgeList = vendorStaff.getBadge_list();
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime twentySecondsBefore = currentDateTime.minus(20, ChronoUnit.SECONDS);
        for (Badge badge : badgeList) {
            if (badge.getCreation_date().isAfter(twentySecondsBefore) &&
                    badge.getCreation_date().isBefore(currentDateTime)) {
                return badge;
            }
        }

        return null;
    }

    public List<Badge> retrieveBadgesByUserId(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        List<Badge> badgeList = new ArrayList<>();
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            badgeList = tourist.getBadge_list();
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            badgeList = local.getBadge_list();
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            badgeList = vendorStaff.getBadge_list();
        }

        return badgeList;
    }

    public Badge markBadgeAsPrimary(Long badgeId, Long userId) throws BadRequestException {
        Optional<Badge> badgeOptional = badgeRepository.findById(badgeId);
        if (badgeOptional.isEmpty()) {
            throw new BadRequestException("Badge does not exist!");
        }

        List<Badge> b_list = retrieveBadgesByUserId(userId);
        for (Badge b : b_list) {
            if (b.getIs_primary() == Boolean.TRUE) {
                b.setIs_primary(false);
                badgeRepository.save(b);
            }
        }

        Badge b = badgeOptional.get();
        b.setIs_primary(true);
        badgeRepository.save(b);

        return b;
    }

    public Badge getPrimaryBadge(Long userId) throws BadRequestException {
        List<Badge> b_list = retrieveBadgesByUserId(userId);
        Badge ans = null;
        for (Badge b : b_list) {
            if (b.getIs_primary() == Boolean.TRUE) {
                ans = b;
                break;
            }
        }
        return ans;
    }

    public List<BadgeTypeEnum> getAllBadgeTypes(Long userId) throws NotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
        User user = userOptional.get();

        List<Badge> badges = new ArrayList<>();
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            badges.addAll(tourist.getBadge_list());
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            badges.addAll(local.getBadge_list());
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            badges.addAll(vendorStaff.getBadge_list());
        } else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            badges.addAll(internalStaff.getBadge_list());
        }

        List<BadgeTypeEnum> badgeTypeEnums = new ArrayList<>(Arrays.asList(BadgeTypeEnum.values()));
        List<String> existingBadges = badges.stream()
                .map(badge -> badge.getBadge_type().name())
                .collect(Collectors.toList());

        badgeTypeEnums.removeIf(badgeType -> existingBadges.contains(badgeType.name()));

        return badgeTypeEnums;
    }

    public BadgeProgressResponse getBadgeProgress(Long userId) throws NotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
        User user = userOptional.get();

        List<Post> posts = new ArrayList<>();
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            posts.addAll(tourist.getPost_list());
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            posts.addAll(local.getPost_list());
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            posts.addAll(vendorStaff.getPost_list());
        } else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            posts.addAll(internalStaff.getPost_list());
        }

        BadgeProgressResponse badgeProgress = new BadgeProgressResponse();
        badgeProgress.setACCOMMODATION_EXPERT(0.0);
        badgeProgress.setATTRACTION_EXPERT(0.0);
        badgeProgress.setTELECOM_EXPERT(0.0);
        badgeProgress.setFOODIE(0.0);
        badgeProgress.setTOP_CONTRIBUTOR(0.0);
        int attractionPostCount = 0;
        int accommodationPostCount = 0;
        int telecomPostCount = 0;
        int foodiePostCount = 0;
        int topContributorPostCount = 0;
        int tourPostCount = 0;

        for (Post post : posts) {
            CategoryItem categoryItem = categoryItemRepository.getCategoryItemContainingPost(post.getPost_id());
            Category category = categoryRepository.getCategoryContainingCategoryItem(categoryItem.getCategory_item_id());

            if (category.getName().equals("Attraction")) {
                attractionPostCount++;
            } else if (category.getName().equals("Accommodation")) {
                accommodationPostCount++;
            } else if (category.getName().equals("Telecom")) {
                telecomPostCount++;
            } else if (category.getName().equals("Tour")) {
                tourPostCount++;
            } else {
                foodiePostCount++;
            }
            topContributorPostCount++;
        }

        if (attractionPostCount >= 2) {
            badgeProgress.setATTRACTION_EXPERT(1.0);
        } else {
            badgeProgress.setATTRACTION_EXPERT(attractionPostCount/2.0);
        }

        if (accommodationPostCount >= 2) {
            badgeProgress.setACCOMMODATION_EXPERT(1.0);
        } else {
            badgeProgress.setACCOMMODATION_EXPERT(accommodationPostCount/2.0);
        }

        if (telecomPostCount >= 2) {
            badgeProgress.setTELECOM_EXPERT(1.0);
        } else {
            badgeProgress.setTELECOM_EXPERT(telecomPostCount/2.0);
        }

        if (foodiePostCount >= 2) {
            badgeProgress.setFOODIE(1.0);
        } else {
            badgeProgress.setFOODIE(foodiePostCount/2.0);
        }

        if (tourPostCount >= 2) {
            badgeProgress.setTOUR_EXPERT(1.0);
        } else {
            badgeProgress.setTOUR_EXPERT(tourPostCount/2.0);
        }

        if (topContributorPostCount >= 4) {
            badgeProgress.setTOP_CONTRIBUTOR(1.0);
        } else {
            badgeProgress.setTOP_CONTRIBUTOR(topContributorPostCount/4.0);
        }

        return badgeProgress;
    }
}