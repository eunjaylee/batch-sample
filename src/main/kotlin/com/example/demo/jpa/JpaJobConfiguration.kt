package com.example.demo.jpa


import com.example.demo.common.DataSourceConfiguration
import com.example.demo.domain.trade.CustomerCredit
import com.example.demo.domain.trade.internal.CustomerCreditIncreaseProcessor
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
//import org.springframework.batch.samples.common.DataSourceConfiguration
//import org.springframework.batch.samples.domain.trade.CustomerCredit
//import org.springframework.batch.samples.domain.trade.internal.CustomerCreditIncreaseProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
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
class JpaJobConfiguration {
    @Bean
    fun itemReader(entityManagerFactory: EntityManagerFactory): JpaPagingItemReader<CustomerCredit> {
        return JpaPagingItemReaderBuilder<CustomerCredit>().name("itemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("select c from CustomerCredit c")
            .build()
    }

    @Bean
    fun itemWriter(entityManagerFactory: EntityManagerFactory): JpaItemWriter<CustomerCredit> {
        return JpaItemWriterBuilder<CustomerCredit>().entityManagerFactory(entityManagerFactory).build()
    }

    @Bean
    fun job(
        jobRepository: JobRepository, jpaTransactionManager: JpaTransactionManager,
        itemReader: JpaPagingItemReader<CustomerCredit>, itemWriter: JpaItemWriter<CustomerCredit>
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
        factoryBean.dataSource=dataSource
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
