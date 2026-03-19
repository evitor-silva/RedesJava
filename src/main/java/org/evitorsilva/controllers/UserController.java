package org.evitorsilva.controllers;

import org.evitorsilva.entities.User;
import org.evitorsilva.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public List<User> getUser(){
        return userService.findAll();
    }

    @PostMapping("")
    public void createUser(@RequestBody User user){
        userService.save(user);
    }
}
