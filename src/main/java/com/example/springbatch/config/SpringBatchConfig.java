package com.example.springbatch.config;

import com.example.springbatch.entity.Person;
import com.example.springbatch.partition.RangePartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SpringBatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PersonWriter personWriter;

    private static final int GRID_SIZE = 2;
    private static final int POOL_SIZE = 4;
    private static final int CHUNK_SIZE = 500;

    @Bean
    FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> itemReader = new FlatFileItemReader();
        itemReader.setResource(new FileSystemResource("src/main/resources/people.csv"));
        itemReader.setName("CSV-READER");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        return itemReader;
    }

    private LineMapper<Person> lineMapper() {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "profession", "birthDate");

        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Person.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public PersonProcessor processor() {
        return new PersonProcessor();
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("MASTER-STEP-CSV-TO-ENTITY")
            .partitioner(slaveStep().getName(), partitioner())
            .partitionHandler(partitionHandler())
            .build();
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("SLAVE-STEP-CSV-TO-ENTITY")
            .<Person, Person>chunk(CHUNK_SIZE)
            .reader(reader())
            .processor(processor())
            .writer(personWriter)
            .build();
    }

    @Bean
    public Partitioner partitioner() {
        return new RangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setGridSize(GRID_SIZE);
        partitionHandler.setTaskExecutor(taskExecutor());
        partitionHandler.setStep(slaveStep());
        return partitionHandler;
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("JOB-CSV-TO-ENTITY")
            .flow(masterStep())
            .end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(POOL_SIZE);
        taskExecutor.setCorePoolSize(POOL_SIZE);
        taskExecutor.setQueueCapacity(POOL_SIZE);
        return taskExecutor;
    }
}


