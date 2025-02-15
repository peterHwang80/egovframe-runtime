package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 *  == 개정이력(Modification Information) ==
 *  
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 *   2014.01.22 권윤정  SLF4J로 로깅방식 변경
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class CallableStatementMapperTest extends TestBase {

	@Resource(name = "empMapper")
	EmpMapper empMapper;

	@Before
	public void onSetUp() throws Exception {

		if (isOracle) {

			ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

			// init data
			ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

			StringBuffer procedureStmt = new StringBuffer();
			procedureStmt.append(" create or replace procedure PROC_GETTIME_BEFORE      \n");
			procedureStmt.append("      (IN_CONDITION  in INTEGER,        \n");
			procedureStmt.append("       OUT_RESULT    out VARCHAR2)   \n");
			procedureStmt.append(" as       \n");
			procedureStmt.append(" begin    \n");
			procedureStmt.append("   -- Add your code       \n");
			procedureStmt.append("   select to_char(sysdate - IN_CONDITION,'YYYYMMDDHH24MISS') as days_before       \n");
			procedureStmt.append("   into   OUT_RESULT      \n");
			procedureStmt.append("   from   dual;   \n");
			procedureStmt.append(" -- Add code for exceptions       \n");
			procedureStmt.append(" exception        \n");
			procedureStmt.append("   when others then       \n");
			procedureStmt.append("     DBMS_OUTPUT.put_line(to_char(sqlcode) ||' : '||sqlerrm);     \n");
			procedureStmt.append(" end;     \n");

			jdbcTemplate.execute(procedureStmt.toString());

			StringBuffer refcursorStmt = new StringBuffer();
			refcursorStmt.append(" create or replace procedure PROC_GET_EMPLIST         \n");
			refcursorStmt.append("      (IN_COND     in VARCHAR2,   \n");
			refcursorStmt.append("       OUT_RESULT  out SYS_REFCURSOR)     \n");
			refcursorStmt.append(" as       \n");
			refcursorStmt.append(" begin    \n");
			refcursorStmt.append("   open OUT_RESULT for    \n");
			refcursorStmt.append("     select EMP_NO,       \n");
			refcursorStmt.append("            EMP_NAME,     \n");
			refcursorStmt.append("            JOB,  \n");
			refcursorStmt.append("            MGR,  \n");
			refcursorStmt.append("            HIRE_DATE,    \n");
			refcursorStmt.append("            SAL,  \n");
			refcursorStmt.append("            COMM, \n");
			refcursorStmt.append("            DEPT_NO       \n");
			refcursorStmt.append("     from   EMP   \n");
			refcursorStmt.append("     where  EMP_NAME like '%' || IN_COND || '%'        \n");
			refcursorStmt.append("     order by EMP_NO ;        \n");
			refcursorStmt.append(" exception        \n");
			refcursorStmt.append("   when others then       \n");
			refcursorStmt.append("     DBMS_OUTPUT.put_line('Value : '||IN_COND);   \n");
			refcursorStmt.append("     DBMS_OUTPUT.put_line(to_char(sqlcode) ||' : '||sqlerrm);     \n");
			refcursorStmt.append(" end;     \n");

			jdbcTemplate.execute(refcursorStmt.toString());

			StringBuffer pkgStmt = new StringBuffer();
			pkgStmt.append(" CREATE OR REPLACE PACKAGE PKG_EMP_REF_CURSOR IS        \n");
			pkgStmt.append("        TYPE EMP_CURSOR IS REF CURSOR;  \n");
			pkgStmt.append("        PROCEDURE PROC_EMP_REF_CURSOR (IN_CONDITION IN VARCHAR2, OUT_RESULT OUT EMP_CURSOR);    \n");
			pkgStmt.append(" END PKG_EMP_REF_CURSOR;        \n");

			jdbcTemplate.execute(pkgStmt.toString());

			StringBuffer pkgBodyStmt = new StringBuffer();
			pkgBodyStmt.append(" CREATE OR REPLACE PACKAGE BODY PKG_EMP_REF_CURSOR IS   \n");
			pkgBodyStmt.append("        PROCEDURE PROC_EMP_REF_CURSOR ( \n");
			pkgBodyStmt.append("                        IN_CONDITION IN VARCHAR2,       \n");
			pkgBodyStmt.append("                        OUT_RESULT OUT EMP_CURSOR       \n");
			pkgBodyStmt.append("        ) AS    \n");
			pkgBodyStmt.append("        BEGIN   \n");
			pkgBodyStmt.append("        OPEN OUT_RESULT FOR     \n");
			pkgBodyStmt.append("                        SELECT  \n");
			pkgBodyStmt.append("                                EMP_NO, \n");
			pkgBodyStmt.append("                                EMP_NAME,       \n");
			pkgBodyStmt.append("                                JOB,    \n");
			pkgBodyStmt.append("                                MGR,    \n");
			pkgBodyStmt.append("                                HIRE_DATE,      \n");
			pkgBodyStmt.append("                                SAL,    \n");
			pkgBodyStmt.append("                                COMM,   \n");
			pkgBodyStmt.append("                                DEPT_NO \n");
			pkgBodyStmt.append("                        FROM EMP        \n");
			pkgBodyStmt.append("                        where  EMP_NAME like '%' || IN_CONDITION || '%'      \n");
			pkgBodyStmt.append("                        order by EMP_NO;        \n");
			pkgBodyStmt.append("        END PROC_EMP_REF_CURSOR;        \n");
			pkgBodyStmt.append(" END PKG_EMP_REF_CURSOR;        \n");

			jdbcTemplate.execute(pkgBodyStmt.toString());
		}
	}

	public void checkResult(Map<String, Object> map) {
		assertNotNull(map.get("outResult"));

		// DB 시간이 local 시간과 크게 차이없다는 가정하에 날짜까지만 비교
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+09:00"), Locale.KOREA);
		calendar.setTime(new Date());
		calendar.roll(Calendar.DATE, -1 * (Integer) map.get("inCondition"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());

		assertEquals(sdf.format(calendar.getTime()), ((String) map.get("outResult")).substring(0, 8));
		LoggerFactory.getLogger(this.getClass()).debug("== outResult : {}", map.get("outResult"));

	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCall() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// 현재 시간에서 inCondition 에 해당하는 일자(days)를 뺀 시간을 되돌려 줄 것임.
			map.put("inCondition", new Integer(1));
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call
			empMapper.selectList("procGetTimeBefore", map);

			// check
			checkResult(map);
		}
	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCallUsingInlineParameter() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// 현재 시간에서 inCondition 에 해당하는 일자(days)를 뺀 시간을 되돌려 줄 것임.
			map.put("inCondition", new Integer(1));
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call
			empMapper.selectList("procGetTimeBeforeUsingInlineParameter", map);

			// check
			checkResult(map);

		}
	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCallUsingOracleRefCursor() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// empName 의 like 비교용 문자열 조건 - WA'RD', FO'RD'
			map.put("inCondition", "RD");
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call
			empMapper.selectList("procGetEmpListUsingOracleRefCursor", map);

			// check
			assertNotNull(map.get("outResult"));

			// MyBatis의 경우 CURSOR output의 경우 ResultMap을 통해 직접 mapping 되기 때문에
			// iBatis와 같이 별도로 ResultSet을 처리할 필요 없음
			assertTrue(map.get("outResult") instanceof List);
			@SuppressWarnings("unchecked")
			List<EmpVO> empList = (List<EmpVO>) map.get("outResult");
			/*
			 * assertTrue(map.get("outResult") instanceof ResultSet); ResultSet
			 * rs = (ResultSet) map.get("outResult"); int i = 0; List<EmpVO>
			 * empList = new ArrayList<EmpVO>(); while (rs.next()) { EmpVO vo =
			 * new EmpVO(); vo.setEmpNo(rs.getBigDecimal("EMP_NO"));
			 * vo.setEmpName(rs.getString("EMP_NAME"));
			 * vo.setJob(rs.getString("JOB"));
			 * vo.setMgr(rs.getBigDecimal("MGR"));
			 * vo.setHireDate(rs.getDate("HIRE_DATE"));
			 * vo.setSal(rs.getBigDecimal("SAL"));
			 * vo.setDeptNo(rs.getBigDecimal("DEPT_NO")); empList.add(vo);
			 *
			 * LoggerFactory.getLogger(this.getClass()).debug("== EmpVO " + (i++) +
			 * " : " + ToStringBuilder.reflectionToString(vo)); }
			 */
			assertEquals(2, empList.size());

		}
	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCallUsingOracleRefCursorInline() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// empName 의 like 비교용 문자열 조건 - WA'RD', FO'RD'
			map.put("inCondition", "RD");
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call
			empMapper.selectList("procGetEmpListUsingOracleRefCursorInline", map);

			// check
			assertNotNull(map.get("outResult"));

			// MyBatis의 경우 CURSOR output의 경우 ResultMap을 통해 직접 mapping 되기 때문에
			// iBatis와 같이 별도로 ResultSet을 처리할 필요 없음
			assertTrue(map.get("outResult") instanceof List);
			@SuppressWarnings("unchecked")
			List<EmpVO> empList = (List<EmpVO>) map.get("outResult");
			/*
			 * assertTrue(map.get("outResult") instanceof ResultSet); ResultSet
			 * rs = (ResultSet) map.get("outResult"); int i = 0; List<EmpVO>
			 * empList = new ArrayList<EmpVO>(); while (rs.next()) { EmpVO vo =
			 * new EmpVO(); vo.setEmpNo(rs.getBigDecimal("EMP_NO"));
			 * vo.setEmpName(rs.getString("EMP_NAME"));
			 * vo.setJob(rs.getString("JOB"));
			 * vo.setMgr(rs.getBigDecimal("MGR"));
			 * vo.setHireDate(rs.getDate("HIRE_DATE"));
			 * vo.setSal(rs.getBigDecimal("SAL"));
			 * vo.setDeptNo(rs.getBigDecimal("DEPT_NO")); empList.add(vo);
			 *
			 * LogFactory.getLog(this.getClass()).debug("== EmpVO " + (i++) +
			 * " : " + ToStringBuilder.reflectionToString(vo)); }
			 */
			assertEquals(2, empList.size());

		}
	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCallCursorWithResultMap() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// empName 의 like 비교용 문자열 조건 - WA'RD', FO'RD'
			map.put("inCondition", "RD");
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call - queryForList 사용 -
			// parameterMap 의 cursor 변수에 대한 javaType 을 java.sql.ResultSet 으로
			// 명시해줌.
			List<EmpVO> resultList = empMapper.selectList("procGetEmpListUsingOracleRefCursorWithResultMap", map);

			// check
			assertNotNull(resultList);

			// MyBatis의 경우 위 list 호출 방식을 지원하지 않음.. (iBatis의 가능)
			assertEquals(0, resultList.size());
			/*
			 * assertEquals(2, resultList.size()); assertEquals("WARD",
			 * resultList.get(0).getEmpName()); assertEquals("FORD",
			 * resultList.get(1).getEmpName());
			 */

		}
	}

	@Rollback(false)
	@Test
	public void testStoredProcedureCallCursorWithResultMapAttr() throws Exception {
		if (isOracle) {
			// IN, OUT 또는 INOUT 파라메터 정의
			Map<String, Object> map = new HashMap<String, Object>();
			// empName 의 like 비교용 문자열 조건 - WA'RD',
			// FO'RD'
			map.put("inCondition", "RD");
			// 결과는 해당 map 의 outResult 라는 변수로 담겨 올 것임.

			// procedure call - queryForObject 사용 -
			// parameterMap 의 cursor 변수에 대한 javaType 을
			// java.sql.ResultSet 으로 명시해주며,
			// sql 이 아닌 parameterMap 의 outResult 프로퍼티에
			// 대해 직접 resultMap 속성을 정의하는 경우
			empMapper.selectList("procGetEmpListUsingOracleRefCursorWithResultMapAttr", map);

			// check
			// 파라메터로 전달한 map 의 outResult 변수에 해당 cursor
			// 의 결과 데이터가 담겨 돌아옴
			assertNotNull(map.get("outResult"));
			@SuppressWarnings("unchecked")
			List<EmpVO> resultList = (List<EmpVO>) map.get("outResult");
			assertEquals(2, resultList.size());
			assertEquals("WARD", resultList.get(0).getEmpName());
			assertEquals("FORD", resultList.get(1).getEmpName());

		}
	}

}
