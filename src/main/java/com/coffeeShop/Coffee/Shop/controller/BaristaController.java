package com.coffeeShop.Coffee.Shop.controller;

import com.coffeeShop.Coffee.Shop.dto.BaristaResponse;
import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.service.BaristaAssignmentService;
import com.coffeeShop.Coffee.Shop.util.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for barista management and monitoring.
 * 
 * Provides endpoints for:
 * 1. Viewing all baristas and their status
 * 2. Getting available baristas
 * 3. Monitoring barista workload
 * 
 * This controller allows monitoring of the workforce and their current assignments.
 */
@RestController
@RequestMapping("/api/baristas")
@Slf4j
public class BaristaController {
    
    private final BaristaAssignmentService baristaAssignmentService;
    private final OrderMapper orderMapper;
    
    @Autowired
    public BaristaController(BaristaAssignmentService baristaAssignmentService,
                           OrderMapper orderMapper) {
        this.baristaAssignmentService = baristaAssignmentService;
        this.orderMapper = orderMapper;
    }
    
    /**
     * Gets all baristas and their current status.
     * 
     * @return List of all baristas with their current assignments
     */
    @GetMapping
    public ResponseEntity<List<BaristaResponse>> getAllBaristas() {
        try {
            List<Barista> baristas = baristaAssignmentService.getAllBaristas();
            List<BaristaResponse> response = orderMapper.toBaristaResponseList(baristas);
            
            log.debug("Retrieved {} baristas", baristas.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving baristas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets a specific barista by ID.
     * 
     * @param id The barista ID
     * @return The barista details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaristaResponse> getBaristaById(@PathVariable Integer id) {
        try {
            List<Barista> baristas = baristaAssignmentService.getAllBaristas();
            
            for (Barista barista : baristas) {
                if (barista.getId().equals(id)) {
                    BaristaResponse response = orderMapper.toBaristaResponse(barista);
                    log.debug("Retrieved barista {}", id);
                    return ResponseEntity.ok(response);
                }
            }
            
            log.debug("Barista {} not found", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error retrieving barista {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets all currently available baristas.
     * 
     * @return List of available baristas
     */
    @GetMapping("/available")
    public ResponseEntity<List<BaristaResponse>> getAvailableBaristas() {
        try {
            List<Barista> allBaristas = baristaAssignmentService.getAllBaristas();
            
            List<BaristaResponse> availableBaristas = allBaristas.stream()
                    .filter(Barista::isAvailable)
                    .map(orderMapper::toBaristaResponse)
                    .toList();
            
            log.debug("Retrieved {} available baristas out of {}", 
                     availableBaristas.size(), allBaristas.size());
            
            return ResponseEntity.ok(availableBaristas);
            
        } catch (Exception e) {
            log.error("Error retrieving available baristas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets barista workload statistics.
     * 
     * @return Workload statistics for all baristas
     */
    @GetMapping("/workload")
    public ResponseEntity<WorkloadStats> getWorkloadStats() {
        try {
            List<Barista> baristas = baristaAssignmentService.getAllBaristas();
            
            int totalBaristas = baristas.size();
            int availableBaristas = (int) baristas.stream().filter(Barista::isAvailable).count();
            int busyBaristas = totalBaristas - availableBaristas;
            
            // Calculate total and average workload
            int totalWorkedMinutes = baristas.stream()
                    .mapToInt(barista -> barista.getTotalWorkedMinutes() != null ? barista.getTotalWorkedMinutes() : 0)
                    .sum();
            
            double averageWorkload = totalBaristas > 0 ? (double) totalWorkedMinutes / totalBaristas : 0.0;
            
            WorkloadStats stats = new WorkloadStats(
                totalBaristas, availableBaristas, busyBaristas, 
                totalWorkedMinutes, averageWorkload
            );
            
            log.debug("Workload stats: total={}, available={}, busy={}, avg_work={:.2f} min", 
                     totalBaristas, availableBaristas, busyBaristas, averageWorkload);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error retrieving workload stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Data class for workload statistics.
     */
    public static class WorkloadStats {
        public final int totalBaristas;
        public final int availableBaristas;
        public final int busyBaristas;
        public final int totalWorkedMinutes;
        public final double averageWorkloadMinutes;
        
        public WorkloadStats(int totalBaristas, int availableBaristas, int busyBaristas, 
                          int totalWorkedMinutes, double averageWorkloadMinutes) {
            this.totalBaristas = totalBaristas;
            this.availableBaristas = availableBaristas;
            this.busyBaristas = busyBaristas;
            this.totalWorkedMinutes = totalWorkedMinutes;
            this.averageWorkloadMinutes = averageWorkloadMinutes;
        }
    }
}
