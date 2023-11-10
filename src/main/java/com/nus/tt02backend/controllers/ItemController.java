package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.Item;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.services.CategoryService;
import com.nus.tt02backend.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/item")
public class ItemController {
    @Autowired
    ItemService itemService;

    @PostMapping("/createItem/{vendorId}")
    public ResponseEntity<Item> createItem(@PathVariable Long vendorId, @RequestBody Item itemToCreate) throws BadRequestException {
        Item createdItem = itemService.createItem(vendorId, itemToCreate);
        return ResponseEntity.ok(createdItem);
    }

    @PutMapping("/updateItem")
    public ResponseEntity<Item> updateItem(@RequestBody Item itemToUpdate) throws BadRequestException {
        Item updatedItem = itemService.updateItem(itemToUpdate);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/deleteItem/{vendorId}/{itemIdToDelete}")
    public ResponseEntity<String> deleteItem(@PathVariable Long vendorId, @PathVariable Long itemIdToDelete) throws BadRequestException {
        String responseMessage = itemService.deleteItem(vendorId, itemIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/retrieveAllItemsByVendor/{vendorId}")
    public ResponseEntity<List<Item>> retrieveAllItemsByVendor(@PathVariable Long vendorId) throws BadRequestException {
        List<Item> items = itemService.retrieveAllItemsByVendor(vendorId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/retrieveAllPublishedItemsByVendor/{vendorId}")
    public ResponseEntity<List<Item>> retrieveAllPublishedItemsByVendor(@PathVariable Long vendorId) throws BadRequestException {
        List<Item> items = itemService.retrieveAllPublishedItemsByVendor(vendorId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/retrieveAllPublishedItems")
    public ResponseEntity<List<Item>> retrieveAllPublishedItems() {
        List<Item> items = itemService.retrieveAllPublishedItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/retrieveItemById/{itemId}")
    public ResponseEntity<Item> retrieveItemById(@PathVariable Long itemId) throws BadRequestException {
        Item item = itemService.retrieveItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/getLastItemId")
    public ResponseEntity<?> getLastItemId() {
        try {
            Long lastItemId = itemService.getLastItemId();
            return ResponseEntity.ok(lastItemId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/getItemVendor/{itemId}")
    public ResponseEntity<Vendor> getItemVendor(@PathVariable Long itemId) {
        Vendor v = itemService.getItemVendor(itemId);
        return ResponseEntity.ok(v);
    }

    @PutMapping("/toggleSaveItem/{userId}/{itemId}")
    public ResponseEntity<List<Item>> toggleSaveItem(@PathVariable Long userId, @PathVariable Long itemId) throws NotFoundException {
        List<Item> itemList = itemService.toggleSaveItem(userId, itemId);
        return ResponseEntity.ok(itemList);
    }

    @GetMapping("/getUserSavedItems/{userId}")
    public ResponseEntity<List<Item>> getUserSavedItems(@PathVariable Long userId) throws NotFoundException {
        List<Item> item_list = itemService.getUserSavedItems(userId);
        return ResponseEntity.ok(item_list);
    }
}
