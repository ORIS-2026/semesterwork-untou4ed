package ru.itis.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.entities.PhoneVerificationCode;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<PhoneVerificationCode, Long> {
    Optional<PhoneVerificationCode> findTopByPhoneOrderByIdDesc(String phoneNumber);
}
