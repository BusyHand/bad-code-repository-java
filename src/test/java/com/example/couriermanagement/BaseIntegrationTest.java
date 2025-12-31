package com.example.couriermanagement;

import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.*;
import com.example.couriermanagement.security.JwtUtil;
import com.example.couriermanagement.service.OpenStreetMapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.liquibase.enabled=true",
                "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml"
        }
)
@AutoConfigureMockMvc(addFilters = true)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected VehicleRepository vehicleRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected DeliveryRepository deliveryRepository;

    @Autowired
    protected DeliveryPointRepository deliveryPointRepository;

    @Autowired
    protected DeliveryPointProductRepository deliveryPointProductRepository;

    @MockBean
    protected OpenStreetMapService openStreetMapService;

    protected User adminUser;
    protected User managerUser;
    protected User courierUser;
    protected String adminToken;
    protected String managerToken;
    protected String courierToken;

    @BeforeEach
    public void setUp() {
        setupUsers();
        setupTokens();
        setupOpenStreetMapMocks();
    }

    private void setupUsers() {
        // Get admin user from Liquibase setup
        adminUser = userRepository.findByLogin("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Create test manager and courier users
        managerUser = userRepository.save(
                User.builder()
                        .login("manager")
                        .passwordHash(passwordEncoder.encode("password"))
                        .name("Менеджер")
                        .role(UserRole.MANAGER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        courierUser = userRepository.save(
                User.builder()
                        .login("courier")
                        .passwordHash(passwordEncoder.encode("password"))
                        .name("Курьер")
                        .role(UserRole.COURIER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private void setupTokens() {
        adminToken = jwtUtil.generateToken(adminUser.getLogin(), adminUser.getRole().name());
        managerToken = jwtUtil.generateToken(managerUser.getLogin(), managerUser.getRole().name());
        courierToken = jwtUtil.generateToken(courierUser.getLogin(), courierUser.getRole().name());
    }

    private void setupOpenStreetMapMocks() {
        // Mock long distance route (Moscow to St. Petersburg - ~635 km)
        when(openStreetMapService.calculateDistance(
                new BigDecimal("55.7558"),
                new BigDecimal("37.6176"),
                new BigDecimal("59.9311"),
                new BigDecimal("30.3609")
        )).thenReturn(new BigDecimal("635.0"));

        // Mock short distance route (within Moscow - ~2.5 km)
        when(openStreetMapService.calculateDistance(
                new BigDecimal("55.7558"),
                new BigDecimal("37.6176"),
                new BigDecimal("55.7600"),
                new BigDecimal("37.6200")
        )).thenReturn(new BigDecimal("2.5"));

        // Mock default distance for createDelivery() method coordinates
        when(openStreetMapService.calculateDistance(
                new BigDecimal("55.7558"),
                new BigDecimal("37.6176"),
                new BigDecimal("55.7558"),
                new BigDecimal("37.6176")
        )).thenReturn(new BigDecimal("0.1")); // Very short distance for same coordinates

        when(openStreetMapService.calculateDistance(
                new BigDecimal("55.7600"),
                new BigDecimal("37.6200"),
                new BigDecimal("55.7700"),
                new BigDecimal("37.6300")
        )).thenReturn(new BigDecimal("0.1")); // Very short distance for same coordinates
    }

    protected Vehicle createVehicle() {
        return vehicleRepository.save(
                Vehicle.builder()
                        .brand("Ford Transit")
                        .licensePlate("А123БВ")
                        .maxWeight(new BigDecimal("1000.0"))
                        .maxVolume(new BigDecimal("15.0"))
                        .build()
        );
    }

    protected Product createProduct() {
        return productRepository.save(
                Product.builder()
                        .name("Тестовый товар")
                        .weight(new BigDecimal("1.5"))
                        .length(new BigDecimal("10.0"))
                        .width(new BigDecimal("10.0"))
                        .height(new BigDecimal("10.0"))
                        .build()
        );
    }

    protected Delivery createDelivery() {
        return createDelivery(courierUser, createVehicle());
    }

    protected Delivery createDelivery(User courier, Vehicle vehicle) {
        Delivery delivery = deliveryRepository.save(
                Delivery.builder()
                        .courier(courier)
                        .vehicle(vehicle)
                        .createdBy(managerUser)
                        .deliveryDate(LocalDate.now().plusDays(5))
                        .timeStart(LocalTime.of(9, 0))
                        .timeEnd(LocalTime.of(18, 0))
                        .status(DeliveryStatus.PLANNED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        // Create delivery point with product
        Product product = createProduct();
        DeliveryPoint deliveryPoint = deliveryPointRepository.save(
                DeliveryPoint.builder()
                        .delivery(delivery)
                        .sequence(1)
                        .latitude(new BigDecimal("55.7558"))
                        .longitude(new BigDecimal("37.6176"))
                        .build()
        );

        deliveryPointProductRepository.save(
                DeliveryPointProduct.builder()
                        .deliveryPoint(deliveryPoint)
                        .product(product)
                        .quantity(2)
                        .build()
        );

        return delivery;
    }

    protected ResultActions expectSuccess(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(result -> {
            int status = result.getResponse().getStatus();
            if (status < 200 || status >= 300) {
                throw new AssertionError(String.format(
                        "Expected success status, but got %d: %s",
                        status,
                        result.getResponse().getContentAsString()
                ));
            }
        });
    }

    protected ResultActions expectBadRequest(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(result -> {
            int status = result.getResponse().getStatus();
            if (status != 400) {
                throw new AssertionError(String.format(
                        "Expected 400, but got %d: %s",
                        status,
                        result.getResponse().getContentAsString()
                ));
            }
        });
    }

    protected ResultActions expectUnauthorized(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(result -> {
            int status = result.getResponse().getStatus();
            if (status != 401) {
                throw new AssertionError(String.format(
                        "Expected 401, but got %d: %s",
                        status,
                        result.getResponse().getContentAsString()
                ));
            }
        });
    }

    protected ResultActions expectForbidden(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(result -> {
            int status = result.getResponse().getStatus();
            if (status != 403) {
                throw new AssertionError(String.format(
                        "Expected 403, but got %d: %s",
                        status,
                        result.getResponse().getContentAsString()
                ));
            }
        });
    }

    protected ResultActions expectNotFound(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(result -> {
            int status = result.getResponse().getStatus();
            if (status != 404) {
                throw new AssertionError(String.format(
                        "Expected 404, but got %d: %s",
                        status,
                        result.getResponse().getContentAsString()
                ));
            }
        });
    }

    protected ResultActions postJson(String url, Object body, String token) throws Exception {
        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return mockMvc.perform(request);
    }

    protected ResultActions postJson(String url, Object body) throws Exception {
        return postJson(url, body, null);
    }

    protected ResultActions putJson(String url, Object body, String token) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions getWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token));
    }

    protected ResultActions deleteWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token));
    }
}