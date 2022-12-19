package com.cos.batch.job.FileDataReadWrite;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.cos.batch.job.FileDataReadWrite.dto.Player;
import com.cos.batch.job.FileDataReadWrite.dto.PlayerYears;

import lombok.RequiredArgsConstructor;
/*
 *  run: --spring.batch.job.names=fileReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
    @Bean
    public Job fileReadWriteJob(Step fileReadWriteStep) {
        return jobBuilderFactory.get("fileReadWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(fileReadWriteStep)
                .build();
    }
    
    @JobScope
    @Bean
    public Step fileReadWriteStep(ItemReader<? extends Player> playerItemReader,
    		                      ItemProcessor<? super Player, ? extends PlayerYears> playerItemProcessor,
    		                      ItemWriter<? super PlayerYears> playerItemWriter
    		) {
    	return stepBuilderFactory.get("fileReadWriteStep")
                .<Player, PlayerYears>chunk(5)
                .reader(playerItemReader)
                .processor(playerItemProcessor)
                .writer(playerItemWriter)
    			.build();
    }
    
    @StepScope
    @Bean
    public ItemProcessor<Player, PlayerYears> playerItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }

    @StepScope
    @Bean
    public FlatFileItemReader<Player> playerItemReader() {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader")
                .resource(new FileSystemResource("Players.csv"))
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PlayerFieldSetMapper())
                .linesToSkip(1)                                    // 첫 번째 줄은 스킵
                .build();
    }
    
    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerYears> playerItemWriter() {
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter("=");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("players_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerItemWriter")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();
    }
}
