package query;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import play.db.jpa.JPAPlugin;
import play.test.UnitTest;
import util.Utils;

public class HistoryTest extends UnitTest {

	@Test
	public void testFind() {
		History history = History.find("3e3eb15");
		
		assertNotNull(history);
		assertEquals( "3e3eb15", history.getRid());
		assertEquals( "tcoffee", history.getBundle() );
		assertEquals( "T-Coffee", history.getLabel() );
		assertEquals( "Done", history.getStatus() );
		assertEquals( "02/02/2011", history.getBegin() );
		assertEquals( "10 sec", history.getDuration() );
		assertEquals( "--", history.getExpire());
		assertEquals( false, history.getHasResult());
	}
	
	@Test 
	public void testfindBySessionAndEmailAndSinceDate() {
		
		/*
		 * why these "magic" numbers 
		 * see the file "usage.log" and import class UsageImportJob
		 */
		
		List<History> list = History.findBySessionAndEmailAndSinceDate( "123", null, null );
		assertEquals( 5, list.size() ); 

		list = History.findBySessionAndEmailAndSinceDate( "321", null, null );
		assertEquals( 4, list.size() ); 
		
		list = History.findBySessionAndEmailAndSinceDate( null, "tcoffee.msa@gmail.com", null );
		assertEquals( 5, list.size() ); 

		list = History.findBySessionAndEmailAndSinceDate( null, "paolo@gmail.com", null );
		assertEquals( 0, list.size() ); 

		list = History.findBySessionAndEmailAndSinceDate( "123", "tcoffee.msa@gmail.com", null );
		assertEquals( 7, list.size() ); 
	

		list = History.findBySessionAndEmailAndSinceDate( "999", null, null );
		assertEquals( 0, list.size() ); 
		
	} 
	
	@Test
	public void testFindByDate() {
		Date since = Utils.parseDate("2011-01-01");

		List<History> list = History.findBySessionAndEmailAndSinceDate( "321", null, since );
		assertEquals( 4, list.size() ); 
	
	}
	
	@Test
	public void testDeleteOne() {
		JPAPlugin.startTx(false);
		try {
			
			History one = History.find("3e3eb15");
			assertNotNull(one);
			
			// now delete 
			boolean result = History.deleteByRequestId("3e3eb15");
			assertTrue( result );
			
			assertNull( History.find("3e3eb15") );
			
		}
		finally {
			JPAPlugin.closeTx(true); //<-- rollback
		}
	} 
	
	
	
	@Test
	public void testDeleteAll() {
		JPAPlugin.startTx(false);
		try {
			
			assertTrue( History.findBySessionAndEmailAndSinceDate("123",null,null).size()>0 );
			
			// now delete 
			boolean result = History.deleteBySessionAndEmail("123", null);
			assertTrue( result );
			
			assertTrue( History.findBySessionAndEmailAndSinceDate("123",null,null).size() == 0 );
			
		}
		finally {
			JPAPlugin.closeTx(true); //<-- rollback
		}
	} 	
	
}
