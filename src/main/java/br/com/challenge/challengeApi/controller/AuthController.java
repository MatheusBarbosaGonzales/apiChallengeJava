package br.com.challenge.challengeApi.controller;


import br.com.challenge.challengeApi.config.security.TokenService;
import br.com.challenge.challengeApi.dto.LoginDto;
import br.com.challenge.challengeApi.dto.TokenDto;
import br.com.challenge.challengeApi.dto.UserCreationDto;
import br.com.challenge.challengeApi.dto.UserExhibitionDto;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    UserService userService;
    @Autowired
    TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid UserCreationDto userCreationDto){

        UserExhibitionDto userExhibitionDto = userService.createUser(userCreationDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(userExhibitionDto);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginDto login){
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken( login.email(), login.password());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new TokenDto(token));
    }
}
