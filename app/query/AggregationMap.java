package query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationMap { 
	
	Map<String,TimeSeries> all = new HashMap<String, TimeSeries>();
	
	/* manteins the insertion order of the main key */
	private List<String> index = new ArrayList(); 
	
	public void put( String key, Date date, int value  ) { 
		
		if( !index.contains(key) ) { 
			index.add(key);
		}
		
		TimeSeries data = all.get(key);
		if( data == null ) { 
			data = new TimeSeries();
			all.put(key,data);
		}
		
		Integer count = data.get(date);
		if( count == null ) { 
			count = 0;
		}
		
		// increment totals 
		data.put(date, count+value);
	}
	
	public Integer get( String key, Date date ) { 

		Map<Date,Integer> data = all.get(key);
		if( data == null ) { 
			return null;
		}
		
		return data.get(date);
	
	}
	
	public TimeSeries get( String key ) { 
		return all.get(key);
	}
	
	public TimeSeries get( int pos ) { 
		String key = index.get(pos);
		return all.get(key);
	}

	public List<String> getIndex() { 
		return new ArrayList(index);
	}
	
	public String[] getIndexArray() {
		return index.toArray(new String[]{});
	}
	
}
