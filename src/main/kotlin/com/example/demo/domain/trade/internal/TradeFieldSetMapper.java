/*
 * Copyright 2006-2007 the original author or authors.
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
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class TradeFieldSetMapper implements FieldSetMapper<Trade> {

	public static final int ISIN_COLUMN = 0;

	public static final int QUANTITY_COLUMN = 1;

	public static final int PRICE_COLUMN = 2;

	public static final int CUSTOMER_COLUMN = 3;

	@Override
	public Trade mapFieldSet(FieldSet fieldSet) {

		Trade trade = new Trade();
		trade.setIsin(fieldSet.readString(ISIN_COLUMN));
		trade.setQuantity(fieldSet.readLong(QUANTITY_COLUMN));
		trade.setPrice(fieldSet.readBigDecimal(PRICE_COLUMN));
		trade.setCustomer(fieldSet.readString(CUSTOMER_COLUMN));

		return trade;
	}

}
