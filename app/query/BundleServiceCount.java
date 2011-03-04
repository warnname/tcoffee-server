package query;


public class BundleServiceCount {

	public String bundle;
	
	public String service;
	
	public long count;
	
	public BundleServiceCount( String bundle, String service, Long count ) { 
		this.bundle = bundle;
		this.service = service;
		this.count = count != null ? count.longValue() : 0;
	}
}
