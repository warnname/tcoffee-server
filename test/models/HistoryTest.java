package models;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.test.UnitTest;

public class HistoryTest extends UnitTest {
	
	
	Date date1;
	Date date2;
	

	@Before public void init() { 
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2010);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 23);
		c.set(Calendar.HOUR_OF_DAY, 14);
		c.set(Calendar.MINUTE, 30);
		
		date1 = c.getTime();
		date2 = new Date( date1.getTime()  +24*60*60*1000);			
	}
	
	@Test
	public void testConstructorBasic() { 
		History h = new History("123");
		assertEquals("123", h.getRid());
		assertNotNull(h.getBegin());
		assertNotNull(h.getExpire());
	}
	
	@Test
	public void testConstructorAll() { 

		History h = new History("123", "expresso", date1, date2, 500L, "done");
		assertEquals("123", h.getRid());
		assertEquals("expresso", h.getMode());
		assertEquals("23 Feb", h.getBegin());
		assertEquals("24 Feb", h.getExpire());
		assertEquals("500 ms", h.getDuration());
		assertEquals("done", h.getStatus());
	}
	
	@Test public void testConstructorByCookie() { 
		Cookie cookie = new Cookie();
		cookie.name = "RID_3232";
		cookie.value = String.format("expresso|%s|%s", date1.getTime(), date2.getTime());
		History result = new History(cookie);
		
		assertEquals("3232", result.getRid());
		assertEquals("23 Feb", result.getBegin());
		assertEquals("24 Feb", result.getExpire());
		assertEquals("--", result.getDuration());
		assertEquals("Unknown", result.getStatus());
	}
	
	@Test public void testConstructByEmptyCookie() { 
		Cookie cookie = new Cookie();
		cookie.name = "RID_123";
		cookie.value = "regular| | | | ";
		History result = new History(cookie);
		
		assertEquals("123", result.getRid());
		assertEquals("", result.getBegin());
		assertEquals("", result.getExpire());
		assertEquals("--", result.getDuration());
		assertEquals("regular", result.getMode());
	}
	
	@Test public void testToValue() { 
		History h = new History("anyvalue");
		assertEquals(String.format("|%s|%s", h.getBeginDate().getTime(), h.getExpireDate().getTime()), h.toValue());
	}
	

	@Test public void testToCookie() { 
		History h = new History("123");
		h.setMode("regular");
		
		Cookie c = h.toCookie();
		assertEquals( "RID_123", c.name);
		long t1 = h.getBeginDate().getTime();
		long t2 = h.getExpireDate().getTime();
		assertEquals( String.format("regular|%s|%s", t1, t2), c.value );

		assertEquals( (t2-t1)/1000, (long)c.maxAge );
	}
}
