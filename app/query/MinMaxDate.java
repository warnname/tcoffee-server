package query;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Just an handly class to handle a couple of dates 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class MinMaxDate {

	public Date min;
	
	public Date max;
	
	public MinMaxDate( Object min, Object max ) { 
		/* date handling delirious ... */
		this.min = (min instanceof Date) ? new Date(((Date)min) .getTime()) : null;
		this.max = (max instanceof Date) ? new Date(((Date)max) .getTime()) : null;
	}
	
	public MinMaxDate( Timestamp min, Timestamp max ) { 
		this.min = min != null ? new Date(min.getTime()) : null;
		this.max = max != null ? new Date(max.getTime()) : null;
	}
}
