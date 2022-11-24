package com.cos.batch.job.validatorParam;

import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cos.batch.job.validatorParam.Validator.FileParamValidator;

import lombok.RequiredArgsConstructor;

/*
 * desc : 파일 이름 파라미터 전달 그리고 검증
 * run : --spring.batch.job.names=ValidatorParamJob 
 *       --fileName=melon.csv
 */
@Configuration
@RequiredArgsConstructor
public class ValidatorParamJobConfig {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Job ValidatorParamJob(Step ValidatorParamStep) {
		return jobBuilderFactory.get("ValidatorParamJob")
				.incrementer(new RunIdIncrementer())
//				.validator(new FileParamValidator())
				.validator(multipleValidator())
				.start(ValidatorParamStep)
				.build();
	}
	
	private CompositeJobParametersValidator multipleValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new FileParamValidator()));

        return validator;
    }
	
	@JobScope
	@Bean
	public Step ValidatorParamStep(Tasklet ValidatorParamTasklet) {
		return stepBuilderFactory.get("ValidatorParamStep")
				.tasklet(ValidatorParamTasklet)
				.build();
	}
	
	@StepScope
	@Bean
	public Tasklet ValidatorParamTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
		return new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println(fileName);
				System.out.println("Validator Param Tasklet");
				return RepeatStatus.FINISHED;
			}
		};
	}
	
	

}
