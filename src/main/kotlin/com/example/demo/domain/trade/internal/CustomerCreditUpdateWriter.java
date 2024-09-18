/*
 * Copyright 2006-2022 the original author or authors.
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

import com.example.demo.domain.trade.CustomerCredit;
import com.example.demo.domain.trade.CustomerCreditDao;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;


public class CustomerCreditUpdateWriter implements ItemWriter<CustomerCredit> {

	private double creditFilter = 800;

	private CustomerCreditDao dao;

	@Override
	public void write(Chunk<? extends CustomerCredit> customerCredits) throws Exception {
		for (CustomerCredit customerCredit : customerCredits) {
			if (customerCredit.getCredit().doubleValue() > creditFilter) {
				dao.writeCredit(customerCredit);
			}
		}
	}

	public void setCreditFilter(double creditFilter) {
		this.creditFilter = creditFilter;
	}

	public void setDao(CustomerCreditDao dao) {
		this.dao = dao;
	}

}
