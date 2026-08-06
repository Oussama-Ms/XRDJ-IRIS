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
    @org.springframework.batch.core.configuration.annotation.StepScope
    public FlatFileItemReader<AnomalyRecord> anomalyReader(@org.springframework.beans.factory.annotation.Value("#{jobParameters['filePath']}") String filePath) {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(
                new Range(68, 72), // 01 CRE TYPE
                new Range(1091, 1092), // 02 COD APP
                new Range(745, 768), // 03 BATCHID
                new Range(1093, 1120), // 04 ID CRE
                new Range(167, 173), // 05 CODE ERREUR
                new Range(174, 300), // 06 TEXTE ERREUR
                new Range(424, 424), // 07 NBR RECYCLAGE
                new Range(636, 643), // 08 DAR DATE
                new Range(712, 717), // 09 DAR TIME
                new Range(1230, 1230), // 10 COD LOT
                new Range(945, 947), // 11 COD AGENCE
                new Range(756, 763), // 12 DAT OPERAT
                new Range(937, 939), // 13 DEVISE OP
                new Range(1017, 1034), // 14 MNT OPERAT
                new Range(1, 424), // 15 CODE RECYCLAGE
                new Range(118, 120), // 16 CODE PHASE
                new Range(500, 504), // 17 CODE DOMAINE
                new Range(1230, 1230), // 18 TEXTE COMP ERREUR
                new Range(51, 53), // 19 EMETTEUR
                new Range(43, 43), // 20 NUM CRE ERR
                new Range(118, 122), // 21 CODE CRE
                new Range(43, 43), // 22 VERSION CRE
                new Range(1230, 1230), // 23 CODE INSTANCE
                new Range(43, 43), // 24 NUM ANO
                new Range(43, 43), // 25 TYP ANO
                new Range(43, 43), // 26 NIV GENE
                new Range(49, 49), // 27 ORIGINE ANO
                new Range(43, 43), // 28 IND ENREG CRE
                new Range(118, 120), // 29 CODE ENREG
                new Range(43, 43), // 30 TYPE REGLE
                new Range(552, 556), // 31 CODE REGLE ENREG
                new Range(577, 584), // 32 DEB VERSION REGLE ENREG
                new Range(585, 592), // 33 FIN VERSION REGLE ENREG
                new Range(1230, 1230), // 34 CODE REGLE ME
                new Range(1230, 1230), // 35 DEB VERSION REGLE ME
                new Range(1230, 1230), // 36 FIN VERSION REGLE ME
                new Range(1230, 1230), // 37 CODE
                new Range(9, 24), // 38 IDF VACATION
                new Range(34, 36), // 39 IDF ETAPE
                new Range(37, 44), // 40 DATE VACATION
                new Range(45, 50), // 41 HEURE VACATION
                new Range(1230, 1230), // 42 LOT
                new Range(49, 49), // 43 NIV DETECT
                new Range(38, 38), // 44 CODE PRIO SCHEMA
                new Range(43, 43), // 45 CODE SCHEMA
                new Range(38, 38), // 46 NUM SEQ GARN
                new Range(38, 38), // 47 ADR GARN
                new Range(110, 112), // 48 CODE FORMAT ME
                new Range(669, 671), // 49 MNEMO MODULE
                new Range(672, 674), // 50 CODE ETAT AUTOM
                new Range(676, 678), // 51 LG ENREG
                new Range(629, 1229) // 52 ENREG
        );
        tokenizer.setNames(
                "creType",
                "codApp",
                "batchid",
                "idCre",
                "codeErreur",
                "texteErreur",
                "nbrRecyclage",
                "darDate",
                "darTime",
                "codLot",
                "codAgence",
                "datOperat",
                "deviseOp",
                "mntOperat",
                "codeRecyclage",
                "codePhase",
                "codeDomaine",
                "texteCompErreur",
                "emetteur",
                "numCreErr",
                "codeCre",
                "versionCre",
                "codeInstance",
                "numAno",
                "typAno",
                "nivGene",
                "origineAno",
                "indEnregCre",
                "codeEnreg",
                "typeRegle",
                "codeRegleEnreg",
                "debVersionRegleEnreg",
                "finVersionRegleEnreg",
                "codeRegleMe",
                "debVersionRegleMe",
                "finVersionRegleMe",
                "code",
                "idfVacation",
                "idfEtape",
                "dateVacation",
                "heureVacation",
                "lot",
                "nivDetect",
                "codePrioSchema",
                "codeSchema",
                "numSeqGarn",
                "adrGarn",
                "codeFormatMe",
                "mnemoModule",
                "codeEtatAutom",
                "lgEnreg",
                "enreg"
        );
        tokenizer.setStrict(false); // Set to false to avoid failing on lines that are too short

        BeanWrapperFieldSetMapper<AnomalyRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AnomalyRecord.class);

        DefaultLineMapper<AnomalyRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<AnomalyRecord>()
                .name("anomalyReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1) // Skip the header line (e.g., RWC...)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public ItemProcessor<AnomalyRecord, AnomalyRecord> anomalyProcessor(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['filePath']}") String filePath) {
        
        String fileName = filePath != null ? new java.io.File(filePath).getName() : null;
        
        return item -> {
            // Optional: Filter out empty or invalid records
            if (item.getCreType() == null || item.getCreType().trim().isEmpty()) {
                return null;
            }
            
            // Clean up strings by trimming whitespace
            item.setCreType(item.getCreType() != null ? item.getCreType().trim() : null);
            item.setCodApp(item.getCodApp() != null ? item.getCodApp().trim() : null);
            item.setBatchid(item.getBatchid() != null ? item.getBatchid().trim() : null);
            item.setIdCre(item.getIdCre() != null ? item.getIdCre().trim() : null);
            item.setCodeErreur(item.getCodeErreur() != null ? item.getCodeErreur().trim() : null);
            item.setTexteErreur(item.getTexteErreur() != null ? item.getTexteErreur().trim() : null);
            item.setNbrRecyclage(item.getNbrRecyclage() != null ? item.getNbrRecyclage().trim() : null);
            item.setDarDate(item.getDarDate() != null ? item.getDarDate().trim() : null);
            item.setDarTime(item.getDarTime() != null ? item.getDarTime().trim() : null);
            item.setCodLot(item.getCodLot() != null ? item.getCodLot().trim() : null);
            item.setCodAgence(item.getCodAgence() != null ? item.getCodAgence().trim() : null);
            item.setDatOperat(item.getDatOperat() != null ? item.getDatOperat().trim() : null);
            item.setDeviseOp(item.getDeviseOp() != null ? item.getDeviseOp().trim() : null);
            item.setMntOperat(item.getMntOperat() != null ? item.getMntOperat().trim() : null);
            item.setCodeRecyclage(item.getCodeRecyclage() != null ? item.getCodeRecyclage().trim() : null);
            item.setCodePhase(item.getCodePhase() != null ? item.getCodePhase().trim() : null);
            item.setCodeDomaine(item.getCodeDomaine() != null ? item.getCodeDomaine().trim() : null);
            item.setTexteCompErreur(item.getTexteCompErreur() != null ? item.getTexteCompErreur().trim() : null);
            item.setEmetteur(item.getEmetteur() != null ? item.getEmetteur().trim() : null);
            item.setNumCreErr(item.getNumCreErr() != null ? item.getNumCreErr().trim() : null);
            item.setCodeCre(item.getCodeCre() != null ? item.getCodeCre().trim() : null);
            item.setVersionCre(item.getVersionCre() != null ? item.getVersionCre().trim() : null);
            item.setCodeInstance(item.getCodeInstance() != null ? item.getCodeInstance().trim() : null);
            item.setNumAno(item.getNumAno() != null ? item.getNumAno().trim() : null);
            item.setTypAno(item.getTypAno() != null ? item.getTypAno().trim() : null);
            item.setNivGene(item.getNivGene() != null ? item.getNivGene().trim() : null);
            item.setOrigineAno(item.getOrigineAno() != null ? item.getOrigineAno().trim() : null);
            item.setIndEnregCre(item.getIndEnregCre() != null ? item.getIndEnregCre().trim() : null);
            item.setCodeEnreg(item.getCodeEnreg() != null ? item.getCodeEnreg().trim() : null);
            item.setTypeRegle(item.getTypeRegle() != null ? item.getTypeRegle().trim() : null);
            item.setCodeRegleEnreg(item.getCodeRegleEnreg() != null ? item.getCodeRegleEnreg().trim() : null);
            item.setDebVersionRegleEnreg(item.getDebVersionRegleEnreg() != null ? item.getDebVersionRegleEnreg().trim() : null);
            item.setFinVersionRegleEnreg(item.getFinVersionRegleEnreg() != null ? item.getFinVersionRegleEnreg().trim() : null);
            item.setCodeRegleMe(item.getCodeRegleMe() != null ? item.getCodeRegleMe().trim() : null);
            item.setDebVersionRegleMe(item.getDebVersionRegleMe() != null ? item.getDebVersionRegleMe().trim() : null);
            item.setFinVersionRegleMe(item.getFinVersionRegleMe() != null ? item.getFinVersionRegleMe().trim() : null);
            item.setCode(item.getCode() != null ? item.getCode().trim() : null);
            item.setIdfVacation(item.getIdfVacation() != null ? item.getIdfVacation().trim() : null);
            item.setIdfEtape(item.getIdfEtape() != null ? item.getIdfEtape().trim() : null);
            item.setDateVacation(item.getDateVacation() != null ? item.getDateVacation().trim() : null);
            item.setHeureVacation(item.getHeureVacation() != null ? item.getHeureVacation().trim() : null);
            item.setLot(item.getLot() != null ? item.getLot().trim() : null);
            item.setNivDetect(item.getNivDetect() != null ? item.getNivDetect().trim() : null);
            item.setCodePrioSchema(item.getCodePrioSchema() != null ? item.getCodePrioSchema().trim() : null);
            item.setCodeSchema(item.getCodeSchema() != null ? item.getCodeSchema().trim() : null);
            item.setNumSeqGarn(item.getNumSeqGarn() != null ? item.getNumSeqGarn().trim() : null);
            item.setAdrGarn(item.getAdrGarn() != null ? item.getAdrGarn().trim() : null);
            item.setCodeFormatMe(item.getCodeFormatMe() != null ? item.getCodeFormatMe().trim() : null);
            item.setMnemoModule(item.getMnemoModule() != null ? item.getMnemoModule().trim() : null);
            item.setCodeEtatAutom(item.getCodeEtatAutom() != null ? item.getCodeEtatAutom().trim() : null);
            item.setLgEnreg(item.getLgEnreg() != null ? item.getLgEnreg().trim() : null);
            item.setEnreg(item.getEnreg() != null ? item.getEnreg().trim() : null);
            
            // Explicitly set fileName so it's saved in DB
            if (fileName != null) {
                item.setFileName(fileName);
            }
            
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
            
            // Trim whitespace and map recordType to CRE or EC
            String trimmedRecordType = item.getRecordType() != null ? item.getRecordType().trim() : "";
            item.setRecordType("CRE".equalsIgnoreCase(trimmedRecordType) ? "CRE" : "EC");
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
