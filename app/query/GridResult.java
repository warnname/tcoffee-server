package query;

import java.util.List;

import models.UsageLog;

/**
 * Class to handle usage stats grid data
 * 
 * @author Paolo Di Tommaso
 *
 */
public class GridResult { 
	public Long total;
	public List<UsageLog> rows;
}

