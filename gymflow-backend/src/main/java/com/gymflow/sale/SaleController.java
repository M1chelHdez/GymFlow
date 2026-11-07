package com.gymflow.sale;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gyms/{gymId}/sales")
public class SaleController {
    private final SaleService service;
    public SaleController(SaleService service) { this.service = service; }
    @GetMapping public List<SaleResponse> list(@PathVariable UUID gymId) { return service.list(gymId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@PathVariable UUID gymId, @Valid @RequestBody CreateSaleRequest request) { return service.create(gymId, request); }
}
