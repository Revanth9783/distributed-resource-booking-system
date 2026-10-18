
package com.resourcebooking.booking.repository;

import com.resourcebooking.booking.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByStatus(
            com.resourcebooking.booking.entity.ResourceStatus status);
}
