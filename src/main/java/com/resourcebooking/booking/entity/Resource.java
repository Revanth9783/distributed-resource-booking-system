
package com.resourcebooking.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_type_id", nullable = false)
    private Long resourceTypeId;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status;

    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Resource() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getResourceTypeId() { return resourceTypeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getCapacity() { return capacity; }
    public ResourceStatus getStatus() { return status; }
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setResourceTypeId(Long resourceTypeId) { this.resourceTypeId = resourceTypeId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setStatus(ResourceStatus status) { this.status = status; }
    public void setLocation(String location) { this.location = location; }
}
