/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egovframe.rte.bat.mapper;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

/**
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see
 *  <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */
public class EmpProcessor2 implements ItemProcessor<EmpVO2, EmpVO2> {

	// 증가할 수
	public static final BigDecimal FIXED_AMOUNT = new BigDecimal("5");

	/**
	 * FIXED_AMOUNT만큼 증가 시킨 후 return
	 */
	@Override
	public EmpVO2 process(EmpVO2 item) throws Exception {
		item.setMgr( item.getMgr()+"AAA" );
		
		return item;
	}
}
