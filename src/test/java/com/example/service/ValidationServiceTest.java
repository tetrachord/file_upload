package com.example.service;

import com.example.model.Person;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationService validationService;


    @Test
    void shouldReturnAPersonGivenValidLine() {

        // given
        String line = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1 \\n";

        Set<ConstraintViolation<Person>> emptyConstraintViolations = new HashSet<>();
        when(validator.validate(any(Person.class))).thenReturn(emptyConstraintViolations);

        // when
        Person person = validationService.validateLine(line);

        // then
        assertNotNull(person);
    }

    @Test
    void shouldThrowExceptionGivenInvalidLine() {

        // given
        String line = "1afb6f5d-a7c2-4311-a92d|3X3D35|Jenny Walters|Likes Avocados|Rides A Scooter|8.5|15.3";

        Set<ConstraintViolation<Person>> constraintViolations = new HashSet<>();
        ConstraintViolation<Person> mockConstraintViolation = mock(ConstraintViolation.class);
        Path mockPropertyPath = mock(Path.class);
        when(mockConstraintViolation.getPropertyPath()).thenReturn(mockPropertyPath);
        when(mockPropertyPath.toString()).thenReturn("uuid");
        when(mockConstraintViolation.getMessage()).thenReturn("Not a valid UUID");
        constraintViolations.add(mockConstraintViolation);
        when(validator.validate(any(Person.class))).thenReturn(constraintViolations);

        // when
        Exception thrown = assertThrows(
                ConstraintViolationException.class,
                () -> validationService.validateLine(line),
                "validateLine() should have thrown exception"
        );

        // then
        String message = thrown.getMessage();
        assertEquals("uuid: Not a valid UUID", message);
    }
}