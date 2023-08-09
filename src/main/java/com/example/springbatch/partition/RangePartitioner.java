package com.example.springbatch.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {
    private final int MIN_VALUE = 1;
    private final int MAX_VALUE = 1000;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int targetSize = (MAX_VALUE - MIN_VALUE + gridSize) / gridSize;
        int start = MIN_VALUE;
        int end = start + targetSize - 1;
        int number = 1;

        Map<String, ExecutionContext> contextMap = new HashMap<>();
        while (start <= MAX_VALUE) {
            ExecutionContext context = new ExecutionContext();
            contextMap.put("partition:" + number, context);

            context.putInt("minValue", start);
            context.putInt("maxValue", end);

            start += targetSize;
            end += targetSize;
            number += 1;
        }
        System.out.println("Context Map: " + contextMap);
        return contextMap;
    }
}
