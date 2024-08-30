package com.example.service;

import com.example.model.Person;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class ValidationService {

    private final Validator validator;

    public Person validateLine(String line) {

        String[] tokens = line.split("\\|");

        // remove any "\n" at end of the line to terminate the "top speed" field.
        if ( tokens[6].endsWith("\\n") ) {
            tokens[6] = tokens[6].replace("\\n", "");
        }

        Person person = Person.builder()
                .uuid(tokens[0])
                .id(tokens[1])
                .name(tokens[2])
                .likes(tokens[3])
                .transport(tokens[4])
                .avgSpeed(tokens[5].trim())
                .topSpeed(tokens[6].trim())
                .build();
        
        Set<ConstraintViolation<Person>> constraintViolations = validator.validate(person);

        if ( !constraintViolations.isEmpty() ) {
            throw new ConstraintViolationException(constraintViolations);
        }

        return person;
    }
}
