/*
 * Copyright 2006-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo.domain.trade.internal;

import com.example.demo.domain.trade.Trade;
import com.example.demo.domain.trade.TradeDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;

/**
 * Writes a Trade object to a database
 *
 * @author Robert Kasanicky
 * @author Mahmoud Ben Hassine
 */
public class JdbcTradeDao implements TradeDao {

	private final Log log = LogFactory.getLog(JdbcTradeDao.class);

	/**
	 * template for inserting a row
	 */
	private static final String INSERT_TRADE_RECORD = "INSERT INTO TRADE (id, version, isin, quantity, price, customer) VALUES (?, 0, ?, ? ,?, ?)";

	/**
	 * handles the processing of SQL query
	 */
	private JdbcOperations jdbcTemplate;

	/**
	 * database is not expected to be setup for auto increment
	 */
	private DataFieldMaxValueIncrementer incrementer;

	/**
	 * @see TradeDao
	 */
	@Override
	public void writeTrade(Trade trade) {
		Long id = incrementer.nextLongValue();
		if (log.isDebugEnabled()) {
			log.debug("Processing: " + trade);
		}
		jdbcTemplate.update(INSERT_TRADE_RECORD, id, trade.getIsin(), trade.getQuantity(), trade.getPrice(),
				trade.getCustomer());
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}

}
