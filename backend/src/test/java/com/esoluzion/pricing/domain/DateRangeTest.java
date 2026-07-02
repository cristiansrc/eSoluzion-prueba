package com.esoluzion.pricing.domain;

import com.esoluzion.pricing.domain.model.DateRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateRange value object")
class DateRangeTest {

    private final LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create DateRange with valid dates")
        void shouldCreateWithValidDates() {
            var range = new DateRange(startDate, endDate);

            assertAll(
                    () -> assertEquals(startDate, range.getStartDate()),
                    () -> assertEquals(endDate, range.getEndDate())
            );
        }

        @Test
        @DisplayName("should accept equal start and end dates")
        void shouldAcceptEqualStartAndEnd() {
            var sameDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
            assertDoesNotThrow(() -> new DateRange(sameDate, sameDate));
        }

        @Test
        @DisplayName("should reject startDate after endDate")
        void shouldRejectStartAfterEnd() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DateRange(endDate, startDate)
            );
        }

        @Test
        @DisplayName("should reject null startDate")
        void shouldRejectNullStartDate() {
            assertThrows(NullPointerException.class, () ->
                    new DateRange(null, endDate)
            );
        }

        @Test
        @DisplayName("should reject null endDate")
        void shouldRejectNullEndDate() {
            assertThrows(NullPointerException.class, () ->
                    new DateRange(startDate, null)
            );
        }

        @Test
        @DisplayName("should reject both null dates")
        void shouldRejectBothNull() {
            assertThrows(NullPointerException.class, () ->
                    new DateRange(null, null)
            );
        }
    }

    @Nested
    @DisplayName("contains method")
    class Contains {

        private final DateRange range = new DateRange(startDate, endDate);

        @Test
        @DisplayName("should return true for date within range")
        void shouldReturnTrueForDateInsideRange() {
            var inside = LocalDateTime.of(2020, 7, 15, 12, 0, 0);
            assertTrue(range.contains(inside));
        }

        @Test
        @DisplayName("should return true at start boundary")
        void shouldReturnTrueAtStartBoundary() {
            assertTrue(range.contains(startDate));
        }

        @Test
        @DisplayName("should return true at end boundary")
        void shouldReturnTrueAtEndBoundary() {
            assertTrue(range.contains(endDate));
        }

        @Test
        @DisplayName("should return false for date before start")
        void shouldReturnFalseForDateBeforeStart() {
            var before = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
            assertFalse(range.contains(before));
        }

        @Test
        @DisplayName("should return false for date after end")
        void shouldReturnFalseForDateAfterEnd() {
            var after = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
            assertFalse(range.contains(after));
        }

        @Test
        @DisplayName("should reject null dateTime")
        void shouldRejectNullDateTime() {
            assertThrows(NullPointerException.class, () ->
                    range.contains(null)
            );
        }
    }

    @Nested
    @DisplayName("equals, hashCode and toString")
    class EqualsHashCodeToString {

        private final DateRange range = new DateRange(startDate, endDate);
        private final DateRange sameRange = new DateRange(startDate, endDate);
        private final DateRange differentStart = new DateRange(
                LocalDateTime.of(2020, 1, 1, 0, 0), endDate);
        private final DateRange differentEnd = new DateRange(startDate,
                LocalDateTime.of(2020, 6, 30, 23, 59, 59));

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(range, range);
        }

        @Test
        @DisplayName("should be equal to another DateRange with same values")
        void shouldBeEqualToSameValues() {
            assertEquals(range, sameRange);
            assertEquals(range.hashCode(), sameRange.hashCode());
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, range);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            assertNotEquals("some string", range);
        }

        @Test
        @DisplayName("should not be equal when startDate differs")
        void shouldNotBeEqualWhenStartDateDiffers() {
            assertNotEquals(range, differentStart);
        }

        @Test
        @DisplayName("should not be equal when endDate differs")
        void shouldNotBeEqualWhenEndDateDiffers() {
            assertNotEquals(range, differentEnd);
        }

        @Test
        @DisplayName("toString should contain both dates")
        void toStringShouldContainDates() {
            var str = range.toString();
            assertAll(
                    () -> assertTrue(str.contains(startDate.toString())),
                    () -> assertTrue(str.contains(endDate.toString()))
            );
        }
    }
}
