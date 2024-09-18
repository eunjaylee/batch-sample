package com.example.demo.jpa


import com.example.demo.domain.trade.CustomerCredit
import org.springframework.data.repository.CrudRepository


interface CustomerCreditCrudRepository : CrudRepository<CustomerCredit, Long>
