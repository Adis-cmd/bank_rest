package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Authority;
import com.example.bankcards.exception.AuthorityNotFoundException;
import com.example.bankcards.repository.AuthorityRepository;
import com.example.bankcards.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {
    private final AuthorityRepository repository;

    @Override
    public Authority findAuthorityByName(String name) {
        return repository.findAuthorityByName(name)
                .orElseThrow(() -> new AuthorityNotFoundException("Authority Not Found!!"));
    }

}