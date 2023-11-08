package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ItemService {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;

    public Item createItem(Long vendorId, Item itemToCreate) throws BadRequestException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new BadRequestException("Vendor does not exist!");
        }
        Vendor vendor = vendorOptional.get();

        Item itemCreated = itemRepository.save(itemToCreate);

        if (vendor.getItem_list() == null) {
            vendor.setItem_list(new ArrayList<>());
        }
        vendor.getItem_list().add(itemCreated);
        vendorRepository.save(vendor);

        return itemCreated;
    }

    public Item updateItem(Item itemToUpdate) throws BadRequestException {
        Optional<Item> itemOptional = itemRepository.findById(itemToUpdate.getItem_id());
        if (itemOptional.isEmpty()) {
            throw new BadRequestException("Item does not exist!");
        }
        Item item = itemOptional.get();

        item.setName(itemToUpdate.getName());
        item.setImage(itemToUpdate.getImage());
        item.setPrice(itemToUpdate.getPrice());
        item.setQuantity(itemToUpdate.getQuantity());
        item.setDescription(itemToUpdate.getDescription());
        item.setIs_published(itemToUpdate.getIs_published());
        item.setIs_limited_edition(itemToUpdate.getIs_limited_edition());

        Item updatedItem = itemRepository.save(item);
        return updatedItem;
    }

    public String deleteItem(Long vendorId, Long itemIdToDelete) throws BadRequestException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new BadRequestException("Vendor does not exist!");
        }
        Vendor vendor = vendorOptional.get();

        Optional<Item> itemOptional = itemRepository.findById(itemIdToDelete);
        if (itemOptional.isEmpty()) {
            throw new BadRequestException("Item does not exist!");
        }
        Item item = itemOptional.get();

        vendor.getItem_list().remove(item);
        vendorRepository.save(vendor);

        itemRepository.delete(item);

        return "Item successfully deleted";
    }

    public List<Item> retrieveAllItemsByVendor(Long vendorId) throws BadRequestException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new BadRequestException("Vendor does not exist!");
        }
        Vendor vendor = vendorOptional.get();

        if (vendor.getItem_list() == null) {
            vendor.setItem_list(new ArrayList<>());
        }

        return vendor.getItem_list();
    }

    public List<Item> retrieveAllPublishedItemsByVendor(Long vendorId) throws BadRequestException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new BadRequestException("Vendor does not exist!");
        }
        Vendor vendor = vendorOptional.get();

        if (vendor.getItem_list() == null) {
            vendor.setItem_list(new ArrayList<>());
        }

        return vendor.getItem_list().stream()
                .filter(Item::getIs_published)
                .toList();
    }

    public List<Item> retrieveAllPublishedItems() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .filter(Item::getIs_published)
                .toList();
    }

    public Item retrieveItemById(Long itemId) throws BadRequestException {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new BadRequestException("Item does not exist!");
        }

        return itemOptional.get();
    }

    public Long getLastItemId() {
        Long lastItemId = itemRepository.findMaxItemId();
        return (lastItemId != null) ? lastItemId : 0L;
    }

    public List<Item> toggleSaveItem(Long userId, Long itemId) throws NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (userOptional.isPresent() && itemOptional.isPresent()) {
            User user = userOptional.get();
            Item item = itemOptional.get();

            if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                if (tourist.getItem_list() == null) tourist.setItem_list(new ArrayList<>());

                if (tourist.getItem_list().contains(item)) { // remove from saved listing
                    tourist.getItem_list().remove(item);
                } else {
                    tourist.getItem_list().add(item);
                }
                touristRepository.save(tourist);
                return tourist.getItem_list();
            } else if (user instanceof Local) {
                Local local = (Local) user;
                if (local.getItem_list() == null) local.setItem_list(new ArrayList<>());

                if (local.getItem_list().contains(item)) { // remove from saved listing
                    local.getItem_list().remove(item);
                } else {
                    local.getItem_list().add(item);
                }
                localRepository.save(local);
                return local.getItem_list();
            } else {
                throw new NotFoundException("User is not tourist or local!");
            }
        } else {
            throw new NotFoundException("User or item is not found!");
        }
    }
}