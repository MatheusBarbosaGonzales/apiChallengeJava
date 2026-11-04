package br.com.challenge.challengeApi.config.security;

import br.com.challenge.challengeApi.repository.UserRepository;
import br.com.challenge.challengeApi.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class VerifyToken extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;

    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String authorizationHeader = request.getHeader("Authorization");
        String token = "";

        if(authorizationHeader == null){
            token = null;
        } else{
            token = authorizationHeader.replace("Bearer", "").trim();
            String login = tokenService.validateToken(token);

            UserDetails user = userRepository.findByEmail(login);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
