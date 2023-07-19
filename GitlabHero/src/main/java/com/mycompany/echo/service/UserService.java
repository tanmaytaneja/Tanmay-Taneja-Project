package com.mycompany.echo.service;

import com.mycompany.echo.models.User;
import com.mycompany.echo.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    private final UserRepository userDetailsRepository;
    public UserService(UserRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }
    public void saveUserDetails(User userDetails) {
        userDetailsRepository.save(userDetails);
    }
    public User getUserDetailsByEmail(String email) {
        return userDetailsRepository.findByEmail(email);
    }
    public boolean checkEmailExists(String email) {
        return userDetailsRepository.existsById(email);
    }

// Other methods as needed
}