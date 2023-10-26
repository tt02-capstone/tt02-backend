package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BadgeTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
                topContributorBadge = badgeRepository.save(topContributorBadge);

                vendorStaff.getBadge_list().add(topContributorBadge);
            }

            if (eligibleForBadge || eligibleForTopContributor) {
                vendorStaffRepository.save(vendorStaff);
            }
        } /* else {
            InternalStaff internalStaff = (InternalStaff) user;
            postList = internalStaff.getPost_list();

            eligibleForBadge = validateBadgeForInternalStaff(category, internalStaff);
            if (eligibleForBadge && internalStaff.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(badgeType))) {
                Badge categoryBadge = new Badge();
                categoryBadge.setBadge_type(badgeType);
                categoryBadge.setBadge_icon(getBadgeIcon(badgeType));
                categoryBadge.setCreation_date(LocalDateTime.now());
                categoryBadge = badgeRepository.save(categoryBadge);

                internalStaff.getBadge_list().add(categoryBadge);
            }

            eligibleForTopContributor = validateTopContributor(postList);
            if (eligibleForTopContributor && internalStaff.getBadge_list().stream().noneMatch(badge ->
                    badge.getBadge_type().equals(BadgeTypeEnum.TOP_CONTRIBUTOR))) {
                Badge topContributorBadge = new Badge();
                topContributorBadge.setBadge_type(BadgeTypeEnum.TOP_CONTRIBUTOR);
                topContributorBadge.setBadge_icon("https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TOP_CONTRIBUTOR.png");
                topContributorBadge.setCreation_date(LocalDateTime.now());
                topContributorBadge = badgeRepository.save(topContributorBadge);

                internalStaff.getBadge_list().add(topContributorBadge);
            }

            if (eligibleForBadge || eligibleForTopContributor) {
                internalStaffRepository.save(internalStaff);
            }
        } */
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

    public Boolean validateBadgeForInternalStaff(Category category, InternalStaff internalStaff) {
        List<Post> categoryItemPostList = new ArrayList<>();

        for (CategoryItem categoryItem : category.getCategory_item_list()) {
            categoryItemPostList.addAll(categoryItem.getPost_list());
        }

        Integer numberOfPosts = Math.toIntExact(categoryItemPostList.stream()
                .filter(post -> post.getInternal_staff_user() != null &&
                        post.getInternal_staff_user().getUser_id().equals(internalStaff.getUser_id())).count());

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
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/ATTRACTION_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.ACCOMMODATION_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/ACCOMMODATION_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.TELECOM_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TELECOM_EXPERT.png";
        } else if (badgeType.equals(BadgeTypeEnum.TOUR_EXPERT)) {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/TOUR_EXPERT.png";
        } else {
            return "https://tt02.s3.ap-southeast-1.amazonaws.com/static/badge/FOODIE_EXPERT.png";
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
        } /* else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            badgeList = internalStaff.getBadge_list();
        } */

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
        } /* else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            badgeList = internalStaff.getBadge_list();
        } */

        return badgeList;
    }
}