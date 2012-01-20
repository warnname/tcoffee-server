package query;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import play.test.UnitTest;
import util.Utils;

public class QueryHelperTest extends UnitTest {

	
	@Test 
	public void testFindUsageMinDate() {
		Date result = QueryHelper.findUsageMinDate();
		assertEquals( "01/02/2011", Utils.asString(result) );
	} 

	@Test 
	public void testFindUsageMaxDate() {
		Date result = QueryHelper.findUsageMaxDate();
		assertEquals( "03/02/2011", Utils.asString(result) );
	} 
	
	
	@Test 
	public void testFindGridResult() { 
		/* 
		 * in the test dataset there are 9 records
		 */
		
		GridResult result = QueryHelper.findUsageGridData(null, 1, 5, null, null, null, null);
		assertTrue( result.total > 0 );
		assertEquals( 5, result.rows.size() );

		result = QueryHelper.findUsageGridData(null, 2, 5, null, null, null, null);
		assertTrue( result.total > 0 );
		assertEquals( 4, result.rows.size() );
		
	}
	
	public void testFindGridResultWithRestrictions() { 
		GridResult result = QueryHelper.findUsageGridData(null, 1, 5, null, null, "rid", "e63796f6");
		assertEquals( new Long(1), result.total );
		assertEquals( "e63796f6", result.rows.get(0).requestId );

		result = QueryHelper.findUsageGridData(null, 1, 5, null, null, "source", "web");
		assertEquals( new Long(9), result.total );
	
	}
	
	@Test 
	public void testFindUsageAggregationWithBundle() { 
		List result = QueryHelper.findUsageAggregation(null);
		
		assertNotNull(result);
	}

	@Test 
	public void testFindUsageAggregationWithFilter() { 
		UsageFilter filter = new UsageFilter();
		filter.bundle = "tcoffee";
		List result = QueryHelper.findUsageAggregation(filter);
		assertEquals( 5, result.size() );
			
		filter.bundle = "catrapid";
		result = QueryHelper.findUsageAggregation(filter);
		assertEquals( 2, result.size() );

		filter.bundle = "xxx";
		result = QueryHelper.findUsageAggregation(filter);
		assertEquals( 0, result.size() );
	
	}
	
	@Test 
	public void testFindUsageAggregationWithDateRange() { 
		UsageFilter filter = new UsageFilter();
		filter.since = Utils.parseDate("03/02/2011");
		filter.until = Utils.parseDate("03/02/2011");
		List<Object[]> result = QueryHelper.findUsageAggregation(filter);
		
		assertEquals(1, result.size());
		assertEquals( new BigInteger("1"), result.get(0)[0] );  	
		assertEquals( "tcoffee", result.get(0)[1] );  	
		assertEquals( "expresso", result.get(0)[2] );  	
		assertEquals( "FAILED", result.get(0)[3] );  	
		assertEquals( Utils.parseDate("03/02/2011"), result.get(0)[4] );  	

		filter.since = Utils.parseDate("03/02/2012");
		filter.until = Utils.parseDate("03/02/2013");
		result = QueryHelper.findUsageAggregation(filter);
		
		assertEquals(0, result.size());
	
	}	
	
	@Test 
	public void testFindUsage() { 
		/* 
		 * in the test dataset there are the following bundle-service pairs 
		 * tcoffee-regular
		 * tcoffee-expresso
		 * tcoffee-mcoffee
		 * catrapid-catrapid
		 */
		List<BundleServiceCount> result = QueryHelper.findUsageBundleServiceCounts();
		
		assertEquals( 4, result.size() );
		
		
		assertEquals( 3, Utils.getItems(result, "service", "catrapid").get(0).count );
		assertEquals( 3, Utils.getItems(result, "service", "regular").get(0).count );
		assertEquals( 2, Utils.getItems(result, "service", "expresso").get(0).count );
		assertEquals( 1, Utils.getItems(result, "service", "mcoffee").get(0).count );

	}
	
	@Test 
	public void testFindUsageServiceMap() { 
		Map<String,List<String>> result = QueryHelper.findUsageServiceMap();
		
		assertEquals( 2, result.keySet().size() );
		assertTrue( result.keySet().contains("tcoffee"));
		assertTrue( result.keySet().contains("catrapid"));
		assertEquals( 3, result.get("tcoffee").size() );
		assertTrue( result.get("tcoffee").contains("regular") );
		assertTrue( result.get("tcoffee").contains("expresso") );
		assertTrue( result.get("tcoffee").contains("mcoffee") );
		assertFalse( result.get("tcoffee").contains("hola") );
		
		assertEquals( 1, result.get("catrapid").size() );
		assertEquals( null, result.get("xxx")  );
		assertTrue( result.get("catrapid").contains("catrapid") );
	}
	
	@Test 
	public void testGlobleStat() {
		GlobeResult result = QueryHelper.findGlobeStat(null,null,null);
		
		Map<String,Object[]> map = new HashMap<String, Object[]>();
		assertEquals( 3, result.items.size() );
		assertEquals( 5, result.max );

		map.put( (String)result.items.get(0)[0] , result.items.get(0));
		map.put( (String)result.items.get(1)[0] , result.items.get(1));
		map.put( (String)result.items.get(2)[0] , result.items.get(2));
		
		assertEquals( "-76.7566", map.get("38.957596")[1]);
		assertEquals( new BigInteger("5"), map.get("38.957596")[2]);

		assertEquals( "-1.7832947", map.get("53.649994")[1]);
		assertEquals( new BigInteger("3"), map.get("53.649994")[2]);
		
		assertEquals( "120.8197", map.get("15.096405")[1]);
		assertEquals( new BigInteger("1"), map.get("15.096405")[2]);
		
		/*
		 * restriction on 'country_code'
		 */
		result = QueryHelper.findGlobeStat(null,"country_code","IT");
		assertEquals( 1, result.items.size() );
		assertEquals( 3, result.max );

		
		/*
		 * empty result 
		 */
		result = QueryHelper.findGlobeStat(null,"country_code","XX");
		assertEquals( 0, result.items.size() );
		assertEquals( 0, result.max );	
	} 
}
