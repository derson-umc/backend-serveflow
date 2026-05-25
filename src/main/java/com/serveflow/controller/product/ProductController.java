package com.serveflow.controller.product;

import com.serveflow.dto.product.request.OnCreate;
import com.serveflow.dto.product.request.OnUpdate;
import com.serveflow.dto.product.request.ProductInput;
import com.serveflow.dto.product.response.ProductOutput;
import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.product.ProductService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuditService   auditService;

    @PostMapping
    public ResponseEntity<ProductOutput> create(
            @Validated(OnCreate.class) @RequestBody ProductInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        ProductOutput output = productService.create(request);
        auditService.logAction(user.getId(), "PRODUCT_CREATE", "Product",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductOutput>> createBatch(
            @Validated(OnCreate.class) @RequestBody List<ProductInput> requests,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        List<ProductOutput> output = productService.createBatch(requests);
        auditService.logAction(user.getId(), "PRODUCT_BATCH_CREATE", "Product",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductOutput>> findAllActive() {
        return ResponseEntity.ok(productService.findAllActive());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductOutput>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOutput> update(
            @PathVariable UUID id,
            @Validated(OnUpdate.class) @RequestBody ProductInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        ProductOutput output = productService.update(id, request);
        auditService.logAction(user.getId(), "PRODUCT_UPDATE", "Product",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ProductOutput> toggleStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        ProductOutput output = productService.toggleStatus(id);
        auditService.logAction(user.getId(), "PRODUCT_TOGGLE_STATUS", "Product",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        productService.deactivate(id);
        auditService.logAction(user.getId(), "PRODUCT_DEACTIVATE", "Product",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.noContent().build();
    }
}
