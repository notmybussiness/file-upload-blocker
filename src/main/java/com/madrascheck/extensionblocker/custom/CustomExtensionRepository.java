package com.madrascheck.extensionblocker.custom;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomExtensionRepository extends JpaRepository<CustomExtension, Long> {

    boolean existsByName(String name);
}
