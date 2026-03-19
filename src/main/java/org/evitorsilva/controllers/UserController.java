package org.evitorsilva.controllers;

import org.evitorsilva.entities.User;
import org.evitorsilva.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String getUser(){
        return "user";
    }

    @PostMapping("")
    public void createUser(@RequestBody User user){
        userService.save(user);
    }
}
