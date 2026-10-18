
package com.resourcebooking.booking.controller;

import com.resourcebooking.booking.dto.CreateResourceRequest;
import com.resourcebooking.booking.dto.ResourceResponse;
import com.resourcebooking.booking.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createResource(
            @Valid @RequestBody CreateResourceRequest request) {
        return resourceService.createResource(request);
    }

    @GetMapping
    public List<ResourceResponse> getResources() {
        return resourceService.getResources();
    }
}
