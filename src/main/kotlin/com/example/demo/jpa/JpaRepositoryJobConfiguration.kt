package com.example.demo.jpa


import com.example.demo.common.DataSourceConfiguration
import com.example.demo.domain.trade.CustomerCredit
import com.example.demo.domain.trade.internal.CustomerCreditIncreaseProcessor
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
//import org.springframework.batch.samples.common.DataSourceConfiguration
//import org.springframework.batch.samples.domain.trade.CustomerCredit
//import org.springframework.batch.samples.domain.trade.internal.CustomerCreditIncreaseProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.math.BigDecimal
import java.util.Map
import javax.sql.DataSource


/**
 * Hibernate JPA dialect does not support custom tx isolation levels => overwrite with
 * ISOLATION_DEFAULT.
 *
 * @author Mahmoud Ben Hassine
 */
@Configuration
@Import(
    DataSourceConfiguration::class
)
@EnableBatchProcessing(isolationLevelForCreate = "ISOLATION_DEFAULT", transactionManagerRef = "jpaTransactionManager")
@EnableJpaRepositories(basePackages = ["com.example.demo.jpa"])
class JpaRepositoryJobConfiguration {
    @Bean
    @StepScope
    fun itemReader(
        @Value("#{jobParameters['credit']}") credit: Double,
        repository: CustomerCreditPagingAndSortingRepository
    ): RepositoryItemReader<CustomerCredit> {
        return RepositoryItemReaderBuilder<CustomerCredit>().name("itemReader")
            .pageSize(2)
            .methodName("findByCreditGreaterThan")
            .repository(repository)
            .arguments(BigDecimal.valueOf(credit!!))
            .sorts(Map.of<String, Sort.Direction>("id", Sort.Direction.ASC))
            .build()
    }

    @Bean
    fun itemWriter(repository: CustomerCreditCrudRepository): RepositoryItemWriter<CustomerCredit> {
        return RepositoryItemWriterBuilder<CustomerCredit>().repository(repository).methodName("save").build()
    }

    @Bean
    fun job(
        jobRepository: JobRepository, jpaTransactionManager: JpaTransactionManager,
        itemReader: RepositoryItemReader<CustomerCredit>, itemWriter: RepositoryItemWriter<CustomerCredit>
    ): Job {
        return JobBuilder("ioSampleJob", jobRepository)
            .start(
                StepBuilder("step1", jobRepository)
                    .chunk<CustomerCredit, CustomerCredit>(2, jpaTransactionManager)
                    .reader(itemReader)
                    .processor(CustomerCreditIncreaseProcessor())
                    .writer(itemWriter)
                    .build()
            )
            .build()
    }

    // Infrastructure beans
    @Bean
    fun jpaTransactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    @Bean
    fun entityManagerFactory(
        persistenceUnitManager: PersistenceUnitManager,
        dataSource: DataSource
    ): EntityManagerFactory {
        val factoryBean = LocalContainerEntityManagerFactoryBean()
        factoryBean.dataSource = dataSource
        factoryBean.setPersistenceUnitManager(persistenceUnitManager)
        factoryBean.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factoryBean.afterPropertiesSet()
        return factoryBean.getObject()!!
    }

    @Bean
    fun persistenceUnitManager(dataSource: DataSource?): PersistenceUnitManager {
        val persistenceUnitManager = DefaultPersistenceUnitManager()
        persistenceUnitManager.defaultDataSource = dataSource
        persistenceUnitManager.afterPropertiesSet()
        return persistenceUnitManager
    }
}
