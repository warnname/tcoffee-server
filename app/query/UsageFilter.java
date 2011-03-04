package query;

import java.util.Date;

import play.data.binding.As;

public class UsageFilter {

	public String bundle;
	
	public String service;
	
	public String status;

	@As("dd/MM/yyyy")
	public Date since;
	
	@As("dd/MM/yyyy")
	public Date until;
	
}
