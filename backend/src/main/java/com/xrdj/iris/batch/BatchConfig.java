package com.xrdj.iris.batch;

import com.xrdj.iris.model.AnomalyRecord;
import com.xrdj.iris.model.RuleCounterRecord;
import com.xrdj.iris.repository.AnomalyRecordRepository;
import com.xrdj.iris.repository.RuleCounterRecordRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    // --- Anomaly Record Batch Components ---

    @Bean
    public FlatFileItemReader<AnomalyRecord> anomalyReader() {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        // You can adjust these ranges based on your exact file format requirements
        tokenizer.setColumns(
                new Range(1, 3),    // recordType (e.g., CRE)
                new Range(60, 68),  // idEbs
                new Range(126, 129),// errorCode (e.g., 0144)
                new Range(131, 250),// errorMessage
                new Range(251, 600) // remainingData
        );
        tokenizer.setNames(
                "recordType",
                "idEbs",
                "errorCode",
                "errorMessage",
                "remainingData"
        );
        tokenizer.setStrict(false); // Set to false to avoid failing on lines that are too short

        BeanWrapperFieldSetMapper<AnomalyRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AnomalyRecord.class);

        DefaultLineMapper<AnomalyRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<AnomalyRecord>()
                .name("anomalyReader")
                .resource(new FileSystemResource("../data/Details_Anomaly_Rejected_CRE_CRE_CRE_GL_AB_FLEXI_2026051820260520.seq"))
                .linesToSkip(1) // Skip the header line (e.g., RWC...)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public ItemProcessor<AnomalyRecord, AnomalyRecord> anomalyProcessor() {
        return item -> {
            // Optional: Filter out empty or invalid records
            if (item.getRecordType() == null || item.getRecordType().trim().isEmpty()) {
                return null;
            }
            
            // Clean up strings by trimming whitespace
            item.setRecordType(item.getRecordType() != null ? item.getRecordType().trim() : null);
            item.setIdEbs(item.getIdEbs() != null ? item.getIdEbs().trim() : null);
            item.setErrorCode(item.getErrorCode() != null ? item.getErrorCode().trim() : null);
            item.setErrorMessage(item.getErrorMessage() != null ? item.getErrorMessage().trim() : null);
            item.setRemainingData(item.getRemainingData() != null ? item.getRemainingData().trim() : null);
            
            return item;
        };
    }

    @Bean
    public RepositoryItemWriter<AnomalyRecord> anomalyWriter(AnomalyRecordRepository repository) {
        return new RepositoryItemWriterBuilder<AnomalyRecord>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step anomalyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                            ItemReader<AnomalyRecord> anomalyReader,
                            ItemProcessor<AnomalyRecord, AnomalyRecord> anomalyProcessor,
                            ItemWriter<AnomalyRecord> anomalyWriter) {
        return new StepBuilder("anomalyStep", jobRepository)
                .<AnomalyRecord, AnomalyRecord>chunk(10, transactionManager)
                .reader(anomalyReader)
                .processor(anomalyProcessor)
                .writer(anomalyWriter)
                .build();
    }

    @Bean
    public Job importAnomalyJob(JobRepository jobRepository, Step anomalyStep) {
        return new JobBuilder("importAnomalyJob", jobRepository)
                .start(anomalyStep)
                .build();
    }

    // --- Rule Counter Record Batch Components ---

    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public FlatFileItemReader<RuleCounterRecord> ruleCounterReader(@org.springframework.beans.factory.annotation.Value("#{jobParameters['filePath']}") String filePath) {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        
        // Using the tokenizer ranges provided by you
        tokenizer.setColumns(
                new Range(1, 25),
                new Range(26, 50),
                new Range(51, 75),
                new Range(76, 80),
                new Range(81, 88),
                new Range(89, 96),
                new Range(97, 121),
                new Range(122, 146),
                new Range(147, 171),
                new Range(172, 196)
        );

        tokenizer.setNames(
                "recordType",
                "appCode1",
                "appCode2",
                "typeFlux",
                "dateStart",
                "dateEnd",
                "creRecus",
                "creTraites",
                "creRejetes",
                "meGeneres"
        );
        tokenizer.setStrict(false);

        BeanWrapperFieldSetMapper<RuleCounterRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(RuleCounterRecord.class);

        DefaultLineMapper<RuleCounterRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<RuleCounterRecord>()
                .name("ruleCounterReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1) // Skip the header line (CPTREGLE...)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public ItemProcessor<RuleCounterRecord, RuleCounterRecord> ruleCounterProcessor(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['filePath']}") String filePath) {
        
        String fileName = filePath != null ? new java.io.File(filePath).getName() : null;
        
        return item -> {
            if (item.getRecordType() == null || item.getRecordType().trim().isEmpty()) {
                return null;
            }
            
            // Trim whitespace
            item.setRecordType(item.getRecordType() != null ? item.getRecordType().trim() : null);
            item.setAppCode1(item.getAppCode1() != null ? item.getAppCode1().trim() : null);
            item.setAppCode2(item.getAppCode2() != null ? item.getAppCode2().trim() : null);
            item.setTypeFlux(item.getTypeFlux() != null ? item.getTypeFlux().trim() : null);
            item.setDateStart(item.getDateStart() != null ? item.getDateStart().trim() : null);
            item.setDateEnd(item.getDateEnd() != null ? item.getDateEnd().trim() : null);
            item.setCreRecus(item.getCreRecus() != null ? item.getCreRecus().trim() : null);
            item.setCreTraites(item.getCreTraites() != null ? item.getCreTraites().trim() : null);
            item.setCreRejetes(item.getCreRejetes() != null ? item.getCreRejetes().trim() : null);
            item.setMeGeneres(item.getMeGeneres() != null ? item.getMeGeneres().trim() : null);
            
            // Explicitly set fileName so it's saved in DB
            if (fileName != null) {
                item.setFileName(fileName);
            }
            
            return item;
        };
    }

    @Bean
    public RepositoryItemWriter<RuleCounterRecord> ruleCounterWriter(RuleCounterRecordRepository repository) {
        return new RepositoryItemWriterBuilder<RuleCounterRecord>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step ruleCounterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                ItemReader<RuleCounterRecord> ruleCounterReader,
                                ItemProcessor<RuleCounterRecord, RuleCounterRecord> ruleCounterProcessor,
                                ItemWriter<RuleCounterRecord> ruleCounterWriter) {
        return new StepBuilder("ruleCounterStep", jobRepository)
                .<RuleCounterRecord, RuleCounterRecord>chunk(10, transactionManager)
                .reader(ruleCounterReader)
                .processor(ruleCounterProcessor)
                .writer(ruleCounterWriter)
                .build();
    }

    @Bean
    public Job importRuleCounterJob(JobRepository jobRepository, Step ruleCounterStep) {
        return new JobBuilder("importRuleCounterJob", jobRepository)
                .start(ruleCounterStep)
                .build();
    }

}
