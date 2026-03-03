package com.madrascheck.extensionblocker.fixed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_extension_policy")
public class FixedExtensionPolicy {

    @Id
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "checked", nullable = false)
    private boolean checked;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FixedExtensionPolicy() {
    }

    public FixedExtensionPolicy(String name, boolean checked) {
        this.name = name;
        this.checked = checked;
    }

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
