package com.madrascheck.extensionblocker.fixed;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedExtensionPolicyRepository extends JpaRepository<FixedExtensionPolicy, String> {

    List<FixedExtensionPolicy> findByCheckedTrue();
}
