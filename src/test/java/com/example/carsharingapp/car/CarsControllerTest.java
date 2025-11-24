package com.example.carsharingapp.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarResponseDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.model.CarType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CarsControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    static void setup(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext context) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/add-cars.sql"));
        }
    }

    @AfterAll
    static void cleanup(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/remove-all-cars.sql"));
        }
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should create a new car and return status 201")
    void createCar_ShouldReturnCreatedCar() throws Exception {
        CarRequestDto request = new CarRequestDto();
        request.setModel("Tesla Model X");
        request.setBrand("Tesla");
        request.setType(CarType.SEDAN);
        request.setInventory(5);
        request.setDailyFee(new BigDecimal("150.00"));

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto.class);

        assertNotNull(actual);
        assertEquals("Tesla Model X", actual.getModel());
        assertEquals("Tesla", actual.getBrand());
        assertEquals(CarType.SEDAN, actual.getType());
        assertEquals(5, actual.getInventory());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Should return paginated list of all cars for customer")
    void getAllCars_ShouldReturnList() throws Exception {
        MvcResult result = mockMvc.perform(get("/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        CarResponseDto[] cars = objectMapper.readValue(
                root.get("content").traverse(), CarResponseDto[].class);

        assertTrue(cars.length > 0);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("Should return car by ID for customer")
    void getCarById_ShouldReturnCar() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(listResult.getResponse().getContentAsByteArray());
        CarResponseDto[] cars = objectMapper
                .readValue(root.get("content").traverse(), CarResponseDto[].class);

        assertTrue(cars.length > 0);

        Long carId = cars[0].getId();

        MvcResult result = mockMvc.perform(get("/cars/{id}", carId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto.class);

        assertNotNull(actual);
        assertEquals(carId, actual.getId());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @Sql(scripts = "classpath:database/add-one-car-for-update-test.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Should update existing car and return updated response")
    void updateCar_ShouldReturnUpdatedCar() throws Exception {
        Long carId = 1L;

        UpdateCarRequestDto request = new UpdateCarRequestDto();
        request.setModel("Updated Tesla");
        request.setBrand("Tesla");
        request.setType("SEDAN");
        request.setInventory(10);
        request.setPricePerDay(180.00);

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(put("/cars/{id}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarResponseDto.class);

        assertNotNull(actual);
        assertEquals("Updated Tesla", actual.getModel());
        assertEquals("Tesla", actual.getBrand());
        assertEquals(CarType.valueOf(request.getType()), actual.getType());
        assertEquals(10, actual.getInventory());

    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should delete car and return 204, then 404 when fetching deleted car")
    void deleteCar_ShouldReturnNoContent() throws Exception {
        Long carId = 1L;

        mockMvc.perform(delete("/cars/{id}", carId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cars/{id}", carId)
                        .with(user("user").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
