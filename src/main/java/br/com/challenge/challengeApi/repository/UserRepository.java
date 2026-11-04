package br.com.challenge.challengeApi.repository;

import br.com.challenge.challengeApi.dto.UserExhibitionDto;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByEmail(String email);
    Page<User> findByRole(UserRole role, Pageable pageable);
}
