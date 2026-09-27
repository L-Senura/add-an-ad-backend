package com.adagency.addanad.modules.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository mechanism for ProductionStaffDB storage, retrieval, and search behavior.
 */
@Repository
public interface ProductionStaffRepo extends JpaRepository<ProductionStaffDB, Long> {

    /**
     * Find all employees by availability status (e.g. "AVAILABLE", "BUSY", "ON_LEAVE").
     */
    List<ProductionStaffDB> findByStatus(String status);

    /**
     * Find employees by their role / specialization (e.g. "Graphic Designer", "Video Editor").
     */
    List<ProductionStaffDB> findByRoleIgnoreCase(String role);

    /**
     * Find employees who have current workload strictly less than the specified capacity.
     */
    List<ProductionStaffDB> findByCurrentWorkloadLessThan(int maxWorkload);
}
