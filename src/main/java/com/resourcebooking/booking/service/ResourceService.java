
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.dto.CreateResourceRequest;
import com.resourcebooking.booking.dto.ResourceResponse;
import com.resourcebooking.booking.entity.Resource;
import com.resourcebooking.booking.entity.ResourceStatus;
import com.resourcebooking.booking.repository.ResourceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    @CacheEvict(value = "resources", allEntries = true)
    public ResourceResponse createResource(CreateResourceRequest request) {
        Resource resource = new Resource();
        resource.setResourceTypeId(request.getResourceTypeId());
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setCapacity(request.getCapacity());
        resource.setLocation(request.getLocation());
        resource.setStatus(ResourceStatus.ACTIVE);

        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "resources", key = "'all'")
    public List<ResourceResponse> getResources() {
        return resourceRepository.findAll()
                .stream()
                .map(ResourceResponse::from)
                .toList();
    }
}
