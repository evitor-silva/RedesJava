package org.evitorsilva.controllers;

import org.evitorsilva.entities.Item;
import org.evitorsilva.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    
    @Autowired
    private ItemRepository itemRepository;
    

    @PostMapping("")
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }
    

    @GetMapping("")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    

    @GetMapping("/{id}")
    public Item getItem(@PathVariable Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
    }
}
