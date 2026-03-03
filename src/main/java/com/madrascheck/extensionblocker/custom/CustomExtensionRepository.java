package com.madrascheck.extensionblocker.custom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomExtensionRepository extends JpaRepository<CustomExtension, Long> {

    boolean existsByName(String name);

    List<CustomExtension> findAllByOrderByCreatedAtDesc();
}
