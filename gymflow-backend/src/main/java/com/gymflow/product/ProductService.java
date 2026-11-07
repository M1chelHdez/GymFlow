package com.gymflow.product;

import com.gymflow.category.Category;
import com.gymflow.category.CategoryRepository;
import com.gymflow.gym.Gym;
import com.gymflow.gym.GymRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final ProductRepository products;
    private final GymRepository gyms;
    private final CategoryRepository categories;
    private final JdbcTemplate jdbc;

    public ProductService(ProductRepository products, GymRepository gyms, CategoryRepository categories, JdbcTemplate jdbc) {
        this.products = products;
        this.gyms = gyms;
        this.categories = categories;
        this.jdbc = jdbc;
    }

    @Transactional
    public ProductResponse create(UUID gymId, ProductRequest request) {
        Gym gym = gyms.findById(gymId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado."));
        String requestedSku = request.sku() == null ? "" : request.sku().trim();
        String sku = requestedSku.isBlank() ? nextSku() : requestedSku;
        if (products.existsByGym_IdAndSkuIgnoreCase(gymId, sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El SKU " + sku + " ya está registrado en este gimnasio. Introduce otro código o consulta el producto existente.");
        }
        Category category = resolveCategory(gymId, request.categoryId());
        Product product = products.save(new Product(gym, category, sku, request.name().trim(), clean(request.description()),
            request.costPrice(), request.salePrice(), request.minimumStock()));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(UUID gymId, UUID productId, ProductRequest request) {
        if (!gyms.existsById(gymId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado.");
        Product product = products.findByIdAndGym_Id(productId, gymId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en este gimnasio."));
        String sku = request.sku() == null || request.sku().isBlank() ? product.getSku() : request.sku().trim();
        if (products.existsByGym_IdAndSkuIgnoreCaseAndIdNot(gymId, sku, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El SKU " + sku + " ya está registrado en este gimnasio. Introduce otro código o consulta el producto existente.");
        }
        Category category = resolveCategory(gymId, request.categoryId());
        recordChange(gymId, product, "SKU", product.getSku(), sku);
        recordChange(gymId, product, "Nombre", product.getName(), request.name().trim());
        recordChange(gymId, product, "Descripción", product.getDescription(), clean(request.description()));
        recordChange(gymId, product, "Costo unitario", product.getCostPrice().toPlainString(), request.costPrice().toPlainString());
        recordChange(gymId, product, "Precio de venta", product.getSalePrice().toPlainString(), request.salePrice().toPlainString());
        recordChange(gymId, product, "Stock mínimo", String.valueOf(product.getMinimumStock()), String.valueOf(request.minimumStock()));
        product.update(category, sku, request.name().trim(), clean(request.description()), request.costPrice(), request.salePrice(), request.minimumStock());
        return ProductResponse.from(products.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(UUID gymId) {
        if (!gyms.existsById(gymId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado.");
        return products.findAllByGym_IdOrderByName(gymId).stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductChangeResponse> history(UUID gymId, UUID productId) {
        if (products.findByIdAndGym_Id(productId, gymId).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en este gimnasio.");
        return jdbc.query("select id, field_name, old_value, new_value, changed_by, changed_at from product_change_history where gym_id=? and product_id=? order by changed_at desc",
            (rs, row) -> new ProductChangeResponse(rs.getObject("id", UUID.class), rs.getString("field_name"), rs.getString("old_value"), rs.getString("new_value"), rs.getString("changed_by"), rs.getTimestamp("changed_at").toInstant()), gymId, productId);
    }

    private String nextSku() {
        Long sequence = jdbc.queryForObject("select nextval('product_sku_seq')", Long.class);
        return "GYM-%06d".formatted(sequence == null ? 1 : sequence);
    }

    private Category resolveCategory(UUID gymId, UUID categoryId) {
        if (categoryId == null) return null;
        return categories.findById(categoryId).filter(c -> c.getGym().getId().equals(gymId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría no pertenece a este gimnasio."));
    }

    private void recordChange(UUID gymId, Product product, String field, String oldValue, String newValue) {
        if (java.util.Objects.equals(oldValue, newValue)) return;
        jdbc.update("insert into product_change_history(gym_id, product_id, field_name, old_value, new_value) values(?,?,?,?,?)",
            gymId, product.getId(), field, oldValue, newValue);
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
