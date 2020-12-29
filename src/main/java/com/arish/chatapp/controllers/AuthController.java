package com.arish.chatapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.arish.chatapp.models.User;
import com.arish.chatapp.services.UserService;
import com.arish.chatapp.utils.JwtUtil;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "/register")
    public HashMap<String, Object> register(@RequestBody User user) throws Exception {

        HashMap<String, Object> response = new HashMap<>();

        final String firstName = user.getFirstName();
        final String lastName = user.getLastName();
        final String email = user.getEmail();
        final String password = user.getPassword();

        System.out.println("in server login : " + firstName);
        System.out.println("  :  in server login : " + lastName);
        System.out.println("in server login : " + email);
        System.out.println("  :  in server login : " + password);
        
        final String validation = validate(firstName, lastName, email, password);

        if (validation.equals("validated")) {

            if (!userService.userExists(email)) {

                // hashing password before saving it to database
                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                user.setPassword(hashedPassword);

                // saving user
                userService.saveUser(user);

                response.put("response", "Success");
                response.put("message", "User Registered");

            } else {

                response.put("response", "Error");
                response.put("message", "User with this username already exists.");

            }

        } else {

            response.put("response", "Error");
            response.put("message", validation);

        }

        return response;
    }

    @PostMapping(value = "/login")
    public HashMap<String, Object> login(@RequestBody User user) throws Exception {

        HashMap<String, Object> response = new HashMap<>();

        final String email = user.getEmail();
        final String password = user.getPassword();

        System.out.println("in server login : " + email);
        System.out.println("  :  in server login : " + password);

        User tempUser = userService.findByEmail(email);

        if (tempUser != null) {

            // check password
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), tempUser.getPassword());

            if (result.verified) {

                // Generating JWT
                String jwt = jwtUtil.generateToken(email);

                response.put("response", "Success");
                response.put("jwt", jwt);

            } else {

                response.put("response", "Error");
                response.put("message", "Incorrect username or password");

            }

        } else {

            response.put("response", "Error");
            response.put("message", "Incorrect username or password");

        }

        return response;
    }

    private String validate(String firstName, String lastName, String email, String password) throws Exception {

        if (firstName == null || firstName.isEmpty()) {
            return "First Name missing.";
        }

        if (firstName == null || firstName.isEmpty()) {
            return "First Name missing.";
        }

        if (email == null || email.isEmpty()) {
            return "Email missing.";
        }

        if (password.length() > 15 || password.length() < 8) {
            return "Password must be less than 16 and more than 7 characters in length.";
        }

        String upperCaseChars = "(.*[A-Z].*)";
        if (!password.matches(upperCaseChars)) {
            return "Password must contain atleast one upper case alphabet.";
        }

        String lowerCaseChars = "(.*[a-z].*)";
        if (!password.matches(lowerCaseChars)) {
            return "Password must contain atleast one lower case alphabet.";
        }

        String numbers = "(.*[0-9].*)";
        if (!password.matches(numbers)) {
            return "Password must contain atleast one number.";
        }

        String specialChars = "(.*[,~,!,@,#,$,%,^,&,*,(,),-,_,=,+,[,{,],},|,;,:,<,>,/,?].*$)";
        if (!password.matches(specialChars)) {
            return "Password should contain atleast one special character.";
        }

        return "validated";
    }

}
