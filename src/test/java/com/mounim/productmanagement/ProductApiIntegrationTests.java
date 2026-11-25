package com.mounim.productmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mounim.productmanagement.entities.Product;
import com.mounim.productmanagement.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:products-api-tests",
        "app.seed-data=false",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
@Transactional
class ProductApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void insertProducts() {
        productRepository.saveAllAndFlush(List.of(
                new Product(null, "Ordinateur", 8500, 10),
                new Product(null, "Souris", 150, 50),
                new Product(null, "Clavier", 350, 30)));
    }

    @Test
    void createsReadsUpdatesAndDeletesAProduct() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  Écran  ","price":1250.50,"quantity":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Écran"))
                .andExpect(jsonPath("$.price").value(1250.50))
                .andExpect(jsonPath("$.quantity").value(5))
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        long id = created.get("id").asLong();
        assertThat(id).isPositive();
        assertThat(productRepository.findById(id)).isPresent();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Écran"));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Écran offert","price":0,"quantity":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Écran offert"))
                .andExpect(jsonPath("$.price").value(0))
                .andExpect(jsonPath("$.quantity").value(0));

        productRepository.flush();
        Product updated = productRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Écran offert");
        assertThat(updated.getPrice()).isZero();
        assertThat(updated.getQuantity()).isZero();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertThat(productRepository.findById(id)).isEmpty();
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsTheLocationOfTheCreatedProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Casque","price":200,"quantity":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.matchesPattern("http://localhost/api/products/[0-9]+")));
    }

    @Test
    void listsProductsInIdOrder() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Ordinateur"))
                .andExpect(jsonPath("$[1].name").value("Souris"))
                .andExpect(jsonPath("$[2].name").value("Clavier"));
    }

    @Test
    void searchesNamesWithoutCaseSensitivity() throws Exception {
        mockMvc.perform(get("/api/products").param("name", "  O  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ordinateur"))
                .andExpect(jsonPath("$[1].name").value("Souris"));
    }

    @Test
    void filtersByInclusiveMinimumPrice() throws Exception {
        mockMvc.perform(get("/api/products").param("minPrice", "350"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Clavier"));
    }

    @Test
    void combinesNameAndPriceFilters() throws Exception {
        mockMvc.perform(get("/api/products").param("name", "o").param("minPrice", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ordinateur"));
    }

    @Test
    void returnsAnEmptyListWhenNoProductMatches() throws Exception {
        mockMvc.perform(get("/api/products").param("name", "introuvable"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void returnsNotFoundForAnUnknownProduct(String method) throws Exception {
        MockHttpServletRequestBuilder builder = request(
                HttpMethod.valueOf(method), "/api/products/{id}", Long.MAX_VALUE);
        if (method.equals("PUT")) {
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"name":"Inconnu","price":10,"quantity":1}
                            """);
        }

        mockMvc.perform(builder)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Produit introuvable"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource("invalidProducts")
    void rejectsInvalidProducts(String body, String field) throws Exception {
        long count = productRepository.count();
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).exists());
        assertThat(productRepository.count()).isEqualTo(count);
    }

    static Stream<Arguments> invalidProducts() {
        return Stream.of(
                Arguments.of("{\"price\":10,\"quantity\":1}", "name"),
                Arguments.of("{\"name\":\"  \",\"price\":10,\"quantity\":1}", "name"),
                Arguments.of("{\"name\":\"" + "a".repeat(121) + "\",\"price\":10,\"quantity\":1}", "name"),
                Arguments.of("{\"name\":\"Test\",\"quantity\":1}", "price"),
                Arguments.of("{\"name\":\"Test\",\"price\":-1,\"quantity\":1}", "price"),
                Arguments.of("{\"name\":\"Test\",\"price\":1000000000,\"quantity\":1}", "price"),
                Arguments.of("{\"name\":\"Test\",\"price\":10}", "quantity"),
                Arguments.of("{\"name\":\"Test\",\"price\":10,\"quantity\":-1}", "quantity"));
    }

    @Test
    void rejectsAnInvalidUpdateWithoutChangingTheProduct() throws Exception {
        Product product = productRepository.findAll().getFirst();
        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Modifié","price":-1,"quantity":1}
                                """))
                .andExpect(status().isBadRequest());
        assertThat(productRepository.findById(product.getId()).orElseThrow().getName())
                .isEqualTo("Ordinateur");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "NaN", "Infinity", "abc", "1000000000"})
    void rejectsInvalidMinimumPrices(String minPrice) throws Exception {
        mockMvc.perform(get("/api/products").param("minPrice", minPrice))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void rejectsInvalidIdentifiers(String id) throws Exception {
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"name\":\"Test\",\"price\":10,\"quantity\":1.5}"})
    void rejectsMalformedJsonAndFractionalQuantities(String body) throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
