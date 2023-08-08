package com.example.springbatch.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "person")
@NoArgsConstructor
@Getter
@Setter
public class Person {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String profession;
    private String birthDate;
}
