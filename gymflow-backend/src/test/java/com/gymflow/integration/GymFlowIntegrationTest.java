package com.gymflow.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration and regression tests against a disposable real PostgreSQL instance.
 * Each test uses its own generated gym data; Testcontainers removes the temporary database container.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.security.user.name=integration-admin",
        "spring.security.user.password=integration-test-password"
})
class GymFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("gymflow_test")
            .withUsername("gymflow_test")
            .withPassword("gymflow_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/gyms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mainModulesPersistAndWorkTogether() throws Exception {
        UUID gymId = createGym();

        MvcResult memberResult = mockMvc.perform(post("/api/v1/gyms/{gymId}/members", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberNumber\":\"SOC-001\",\"fullName\":\"Socio de Integracion\",\"email\":\"socio@example.com\",\"phone\":\"2281234567\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberNumber").value("SOC-001"))
                .andReturn();
        UUID memberId = UUID.fromString(JsonPath.read(memberResult.getResponse().getContentAsString(), "$.id"));

        MvcResult categoryResult = mockMvc.perform(post("/api/v1/gyms/{gymId}/categories", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Suplementos\",\"description\":\"Productos deportivos\"}"))
                .andExpect(status().isCreated()).andReturn();
        UUID categoryId = UUID.fromString(JsonPath.read(categoryResult.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(post("/api/v1/gyms/{gymId}/suppliers", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Proveedor Demo\",\"email\":\"proveedor@example.com\",\"phone\":\"2281112233\",\"notes\":\"Proveedor de prueba\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Proveedor Demo"));

        MvcResult productResult = createProduct(gymId, categoryId, "SUP-IT-001", "Proteina Demo", "100.00", "250.00");
        UUID productId = UUID.fromString(JsonPath.read(productResult.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(post("/api/v1/gyms/{gymId}/inventory/movements", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"type\":\"INITIAL\",\"quantity\":10,\"reason\":\"Carga inicial de prueba\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/gyms/{gymId}/products/{productId}", gymId, productId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SUP-IT-001\",\"name\":\"Proteina Demo Actualizada\",\"description\":\"Edicion de prueba\",\"categoryId\":\"" + categoryId + "\",\"costPrice\":110.00,\"salePrice\":275.00,\"minimumStock\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Proteina Demo Actualizada"));

        mockMvc.perform(get("/api/v1/gyms/{gymId}/products/{productId}/history", gymId, productId).with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").exists());

        MvcResult planResult = mockMvc.perform(post("/api/v1/gyms/{gymId}/membership-plans", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Plan Mensual IT\",\"description\":\"Plan de prueba\",\"durationValue\":1,\"durationUnit\":\"MONTH\",\"price\":500.00}"))
                .andExpect(status().isCreated()).andReturn();
        UUID planId = UUID.fromString(JsonPath.read(planResult.getResponse().getContentAsString(), "$.id"));

        mockMvc.perform(post("/api/v1/gyms/{gymId}/memberships", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":\"" + memberId + "\",\"planId\":\"" + planId + "\",\"startDate\":\"2026-10-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endDate").value("2026-11-01"));

        MvcResult saleResult = mockMvc.perform(post("/api/v1/gyms/{gymId}/sales", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":\"" + memberId + "\",\"paymentMethod\":\"CASH\",\"items\":[{\"productId\":\"" + productId + "\",\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.folio").value(org.hamcrest.Matchers.startsWith("VTA-")))
                .andExpect(jsonPath("$.total").value(550.0))
                .andReturn();

        String saleId = JsonPath.read(saleResult.getResponse().getContentAsString(), "$.id");
        assertThat(jdbc.queryForObject("select quantity from stock_balances where gym_id=? and product_id=?", Integer.class, gymId, productId)).isEqualTo(8);
        assertThat(jdbc.queryForObject("select count(*) from sales where id=?", Integer.class, UUID.fromString(saleId))).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from sale_items where sale_id=?", Integer.class, UUID.fromString(saleId))).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from stock_movements where gym_id=? and product_id=?", Integer.class, gymId, productId)).isEqualTo(2);
        mockMvc.perform(get("/api/v1/gyms/{gymId}/sales", gymId).with(auth()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(saleId));
    }

    @Test
    void failedSaleRollsBackEarlierStockUpdatesAndDoesNotCreateSale() throws Exception {
        UUID gymId = createGym();
        UUID firstProduct = productId(createProduct(gymId, null, "ROLL-001", "Producto Uno", "10.00", "20.00"));
        UUID secondProduct = productId(createProduct(gymId, null, "ROLL-002", "Producto Dos", "10.00", "30.00"));
        addInitialStock(gymId, firstProduct, 5);
        addInitialStock(gymId, secondProduct, 1);

        mockMvc.perform(post("/api/v1/gyms/{gymId}/sales", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":null,\"paymentMethod\":\"CASH\",\"items\":[{\"productId\":\"" + firstProduct + "\",\"quantity\":2},{\"productId\":\"" + secondProduct + "\",\"quantity\":2}]}"))
                .andExpect(status().isConflict());

        assertThat(stock(gymId, firstProduct)).isEqualTo(5);
        assertThat(stock(gymId, secondProduct)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from sales where gym_id=?", Integer.class, gymId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from stock_movements where gym_id=? and type='OUT'", Integer.class, gymId)).isZero();
    }

    @Test
    void inventoryRejectsNegativeStockWithoutPersistingMovement() throws Exception {
        UUID gymId = createGym();
        UUID productId = productId(createProduct(gymId, null, "INV-001", "Producto Inventario", "5.00", "12.00"));
        addInitialStock(gymId, productId, 3);

        mockMvc.perform(post("/api/v1/gyms/{gymId}/inventory/movements", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"type\":\"OUT\",\"quantity\":4,\"reason\":\"No debe aplicarse\"}"))
                .andExpect(status().isConflict());

        assertThat(stock(gymId, productId)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from stock_movements where gym_id=? and product_id=?", Integer.class, gymId, productId)).isEqualTo(1);
    }

    @Test
    void duplicateProductSkuIsRejectedAndDoesNotCreateSecondProduct() throws Exception {
        UUID gymId = createGym();
        createProduct(gymId, null, "UNIQUE-001", "Producto Original", "10.00", "20.00");

        mockMvc.perform(post("/api/v1/gyms/{gymId}/products", gymId)
                        .with(auth()).contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("UNIQUE-001", "Producto Duplicado", null, "10.00", "20.00")))
                .andExpect(status().isConflict());

        assertThat(jdbc.queryForObject("select count(*) from products where gym_id=? and sku='UNIQUE-001'", Integer.class, gymId)).isEqualTo(1);
    }

    private UUID createGym() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/gyms").with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"GymFlow Integration\",\"timezone\":\"America/Mexico_City\",\"currency\":\"MXN\"}"))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private MvcResult createProduct(UUID gymId, UUID categoryId, String sku, String name, String cost, String price) throws Exception {
        return mockMvc.perform(post("/api/v1/gyms/{gymId}/products", gymId).with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(sku, name, categoryId, cost, price)))
                .andExpect(status().isCreated()).andReturn();
    }

    private String productJson(String sku, String name, UUID categoryId, String cost, String price) {
        return "{\"sku\":\"" + sku + "\",\"name\":\"" + name + "\",\"description\":\"Producto para pruebas\",\"categoryId\":" + (categoryId == null ? "null" : "\"" + categoryId + "\"") + ",\"costPrice\":" + cost + ",\"salePrice\":" + price + ",\"minimumStock\":1}";
    }

    private UUID productId(MvcResult result) throws Exception {
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private void addInitialStock(UUID gymId, UUID productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/gyms/{gymId}/inventory/movements", gymId).with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"type\":\"INITIAL\",\"quantity\":" + quantity + ",\"reason\":\"Stock inicial de prueba\"}"))
                .andExpect(status().isCreated());
    }

    private int stock(UUID gymId, UUID productId) {
        return jdbc.queryForObject("select quantity from stock_balances where gym_id=? and product_id=?", Integer.class, gymId, productId);
    }

    private RequestPostProcessor auth() {
        return SecurityMockMvcRequestPostProcessors.httpBasic("integration-admin", "integration-test-password");
    }
}
