package br.com.challenge.challengeApi.service;

import br.com.challenge.challengeApi.dto.UserCreationDto;
import br.com.challenge.challengeApi.dto.UserExhibitionDto;
import br.com.challenge.challengeApi.dto.UserPatchDto;
import br.com.challenge.challengeApi.exception.UserNotFoundException;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.model.UserRole;
import br.com.challenge.challengeApi.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public UserExhibitionDto createUser(UserCreationDto userCreation)
    {
        String encodedPassword = new BCryptPasswordEncoder().encode(userCreation.getPassword());
        User user = new User();
        BeanUtils.copyProperties(userCreation, user);

        user.setPassword(encodedPassword);
        user.setRole(UserRole.SIGNER);
        User savedUser = userRepository.save(user);
        return new UserExhibitionDto(savedUser);
    }

    public UserExhibitionDto searchById(Long id){
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()){
            return new UserExhibitionDto(userOptional.get());
        } else {
            throw new UserNotFoundException("User does not exist.");
        }
    }

    public UserExhibitionDto searchByEmail(String email) {

        UserDetails userDetails = userRepository.findByEmail(email);
        if (userDetails == null) {
            throw new UserNotFoundException("User does not exist.");
        }
        User user = (User) userDetails;

        return new UserExhibitionDto(user);
    }

    public Page<UserExhibitionDto> listAll(Pageable pageable){
        return userRepository.findByRole(UserRole.SIGNER, pageable).map(UserExhibitionDto::new);
    }


    public Page<UserExhibitionDto> listAllSigners(Pageable pageable){
        return userRepository.findByRole(UserRole.SIGNER,pageable).map(UserExhibitionDto::new);
    }


    public void exclude(Long id){
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isPresent()){
            userRepository.delete(userOptional.get());
        }else{
            throw new UserNotFoundException("User does not exist");
        }
    }

    public UserExhibitionDto patch(UserPatchDto userDto, Long id){
        Optional<User> userOptional = userRepository.findById(id);


        if(userOptional.isPresent()){
            User user = userOptional.get();


            BeanUtils.copyProperties(userDto, user, "password");

            if(userDto.password() != null){
                user.setPassword(new BCryptPasswordEncoder().encode(userDto.password()));
            }

            return new UserExhibitionDto(userRepository.save(user));
        }
        else {
            throw new UserNotFoundException("User does not exist");
        }
    }


}
