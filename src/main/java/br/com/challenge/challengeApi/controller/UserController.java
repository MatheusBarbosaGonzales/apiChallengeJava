package br.com.challenge.challengeApi.controller;

import br.com.challenge.challengeApi.dto.UserExhibitionDto;
import br.com.challenge.challengeApi.dto.UserPatchDto;
import br.com.challenge.challengeApi.exception.UserNotFoundException;
import br.com.challenge.challengeApi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<UserExhibitionDto> listAll(@PageableDefault(size = 10, page =0) Pageable pageable){
        return userService.listAll(pageable);
    }
    @GetMapping("/signers")
    @ResponseStatus(HttpStatus.OK)
    public Page<UserExhibitionDto> listAllSigners(@PageableDefault(size = 10, page =0) Pageable pageable){
        return userService.listAllSigners(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserExhibitionDto> searchById(@PathVariable Long id){
        try {
            return ResponseEntity.ok(userService.searchById(id));
        }catch (UserNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserExhibitionDto> searchMe(Authentication authentication
    ) {
        try {
            String email = authentication.getName();

            return ResponseEntity.ok(
                    userService.searchByEmail(email)
            );

        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteById(@PathVariable Long id){
        try {
            userService.exclude(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserExhibitionDto> patch(@RequestBody @Valid UserPatchDto user, @PathVariable Long id ) {
        try{
            UserExhibitionDto userExhibitionDto = userService.patch(user, id);
            return ResponseEntity.ok(userExhibitionDto);
        }catch(UserNotFoundException e ){
            return ResponseEntity.notFound().build();
        }
    }


}
