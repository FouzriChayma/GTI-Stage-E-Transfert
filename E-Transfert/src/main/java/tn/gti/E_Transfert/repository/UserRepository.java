package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Add this import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.gti.E_Transfert.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

}