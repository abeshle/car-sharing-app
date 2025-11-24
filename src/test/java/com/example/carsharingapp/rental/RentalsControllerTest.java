package com.example.carsharingapp.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.rental.RentalResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalsControllerTest {
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
                    new ClassPathResource("database/add-rentals.sql"));
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
                    new ClassPathResource("database/remove-all-rentals.sql"));
        }
    }

    @Test
    @WithUserDetails("customer@example.com")
    @Sql(
            scripts = {
                    "classpath:database/remove-all-rentals.sql",
                    "classpath:database/add-rentals.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Should create a rental and return status 201 with correct RentalResponseDto")
    void createRental_ShouldReturnCreatedRental() throws Exception {
        LocalDate returnDate = LocalDate.now().plusDays(3);

        String jsonRequest =
                """
        {
            "carId": 1,
            "returnDate": "%s"
        }
                """.formatted(returnDate);

        MvcResult result = mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn();

        RentalResponseDto rental = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalResponseDto.class);

        assertNotNull(rental);
        assertEquals(1L, rental.getCarId());
    }

    @Test
    @WithUserDetails("manager@example.com")
    @Sql(
            scripts = {
                    "classpath:database/remove-all-rentals.sql",
                    "classpath:database/add-rentals.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Manager should be able to retrieve list of active rentals")
    void getAllRentals_asManager_ShouldReturnList() throws Exception {
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("active", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        RentalResponseDto[] rentals = objectMapper.readValue(
                root.get("content").traverse(),
                RentalResponseDto[].class
        );

        assertTrue(rentals.length > 0);
    }

    @Test
    @WithUserDetails("customer@example.com")
    @DisplayName("Should return rental by ID for customer")
    void getRentalById_ShouldReturnRental() throws Exception {
        Long rentalId = 1L;

        MvcResult result = mockMvc.perform(get("/rentals/{id}", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        RentalResponseDto rental = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalResponseDto.class
        );

        assertNotNull(rental);
        assertEquals(rentalId, rental.getId());
    }

    @Test
    @WithUserDetails("customer@example.com")
    @DisplayName("Should return updated rental after car return")
    void returnCar_ShouldReturnUpdatedRental() throws Exception {
        Long rentalId = 1L;

        MvcResult result = mockMvc.perform(post("/rentals/{id}/return", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        RentalResponseDto rental = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalResponseDto.class
        );

        assertNotNull(rental);
        assertEquals(rentalId, rental.getId());
    }
}
