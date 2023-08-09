package com.example.springbatch.config;

import com.example.springbatch.entity.Person;
import com.example.springbatch.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonWriter implements ItemWriter<Person> {

    private final PersonRepository personRepository;

    @Override
    public void write(List<? extends Person> list) {
        System.out.println("Thread :-> " + Thread.currentThread().getName());
        personRepository.saveAll(list);
    }
}
