
package com.resourcebooking.booking.dto;

import com.resourcebooking.booking.entity.Resource;
import com.resourcebooking.booking.entity.ResourceStatus;

public record ResourceResponse(
        Long id,
        Long resourceTypeId,
        String name,
        String description,
        Integer capacity,
        ResourceStatus status,
        String location
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getResourceTypeId(),
                resource.getName(),
                resource.getDescription(),
                resource.getCapacity(),
                resource.getStatus(),
                resource.getLocation()
        );
    }
}
