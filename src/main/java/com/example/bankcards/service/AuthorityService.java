package com.example.bankcards.service;

import com.example.bankcards.entity.Authority;

public interface AuthorityService {
    Authority findAuthorityByName(String name);
}
