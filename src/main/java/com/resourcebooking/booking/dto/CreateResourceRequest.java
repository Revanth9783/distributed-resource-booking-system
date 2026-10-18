
package com.resourcebooking.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateResourceRequest {

    @NotNull
    private Long resourceTypeId;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    @Min(1)
    private Integer capacity;

    private String location;

    public Long getResourceTypeId() { return resourceTypeId; }
    public void setResourceTypeId(Long resourceTypeId) { this.resourceTypeId = resourceTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
