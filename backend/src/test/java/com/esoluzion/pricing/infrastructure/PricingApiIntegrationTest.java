package com.esoluzion.pricing.infrastructure;

import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.PriceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pricing API integration tests")
class PricingApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;

    private String url() {
        return "http://localhost:" + port + "/api/prices";
    }

    @Nested
    @DisplayName("5 mandatory price query scenarios")
    class MandatoryScenarios {

        @Test
        @DisplayName("Test 1: 2020-06-14T10:00:00 -> priceList=1, price=35.50")
        void test1() {
            var response = fetchPrice("2020-06-14T10:00:00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            var body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getPriceList()).isEqualTo(1);
            assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("35.50"));
        }

        @Test
        @DisplayName("Test 2: 2020-06-14T16:00:00 -> priceList=2, price=25.45")
        void test2() {
            var response = fetchPrice("2020-06-14T16:00:00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            var body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getPriceList()).isEqualTo(2);
            assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("25.45"));
        }

        @Test
        @DisplayName("Test 3: 2020-06-14T21:00:00 -> priceList=1, price=35.50")
        void test3() {
            var response = fetchPrice("2020-06-14T21:00:00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            var body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getPriceList()).isEqualTo(1);
            assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("35.50"));
        }

        @Test
        @DisplayName("Test 4: 2020-06-15T10:00:00 -> priceList=3, price=30.50")
        void test4() {
            var response = fetchPrice("2020-06-15T10:00:00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            var body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getPriceList()).isEqualTo(3);
            assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("30.50"));
        }

        @Test
        @DisplayName("Test 5: 2020-06-16T21:00:00 -> priceList=4, price=38.95")
        void test5() {
            var response = fetchPrice("2020-06-16T21:00:00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            var body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getPriceList()).isEqualTo(4);
            assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("38.95"));
        }

        private ResponseEntity<PriceResponse> fetchPrice(String applicationDate) {
            var uri = url() + "?applicationDate=" + applicationDate
                    + "&productId=" + PRODUCT_ID
                    + "&brandId=" + BRAND_ID;
            return restTemplate.getForEntity(uri, PriceResponse.class);
        }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandling {

        @Test
        @DisplayName("should return 404 when no applicable price")
        void shouldReturn404WhenNoPrice() {
            var uri = url() + "?applicationDate=2020-06-14T10:00:00&productId=99999&brandId=1";
            var response = restTemplate.getForEntity(uri, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should return 400 when missing required parameter")
        void shouldReturn400WhenMissingParameter() {
            var uri = url() + "?productId=" + PRODUCT_ID + "&brandId=" + BRAND_ID;
            var response = restTemplate.getForEntity(uri, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should return 400 when parameter type is invalid")
        void shouldReturn400WhenInvalidType() {
            var uri = url() + "?applicationDate=2020-06-14T10:00:00&productId=invalid&brandId=1";
            var response = restTemplate.getForEntity(uri, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("E4: Fecha sin tarifa vigente debe devolver 404 PRICE_NOT_FOUND")
    void shouldReturn404WhenNoPriceForDate() {
        var response = restTemplate.getForEntity(
            "/api/prices?applicationDate=2019-01-01T00:00:00&productId=35455&brandId=1",
            ApiErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("PRICE_NOT_FOUND");
    }

    @Test
    @DisplayName("E5: Formato de fecha inválido debe devolver 400 VALIDATION_ERROR")
    void shouldReturn400WhenInvalidDateFormat() {
        var response = restTemplate.getForEntity(
            "/api/prices?applicationDate=invalid-date&productId=35455&brandId=1",
            ApiErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Debe devolver 400 cuando falta parámetro requerido")
    void shouldReturn400WhenMissingRequiredParameter() {
        var response = restTemplate.getForEntity(
            "/api/prices?productId=35455&brandId=1",
            ApiErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("should return 400 when productId is 0 (IllegalArgumentException)")
    void shouldReturn400WhenProductIdIsZero() {
        var response = restTemplate.getForEntity(
            "/api/prices?applicationDate=2020-06-14T10:00:00&productId=0&brandId=1",
            ApiErrorResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
