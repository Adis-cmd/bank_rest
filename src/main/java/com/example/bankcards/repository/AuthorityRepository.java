package com.example.bankcards.repository;

import com.example.bankcards.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    @Query(nativeQuery = true, value = "select * from authorities a where a.name = :name")
    Optional<Authority> findAuthorityByName(@Param("name") String name);

}
