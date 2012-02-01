package query;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import models.OutResult;
import models.Repo;
import models.Status;
import models.UsageLog;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import util.Utils;
import bundle.BundleRegistry;

public class History {
	
	/** the alignment request-id */
	private String rid;
	
	/** the alignment request date-time */
	private Date begin;
	
	/** the history entry empiration date-time */
	private Date expire;

	/* the alignment job duration in millis */
	private Long duration;
	
	/* the label used to show this entry in the history list */
	private String label;
	
	/** name of the bundle that produced this history item */
	private String bundle;
	
	/* the current job execution status */
	private String status = "NA";
	
	private boolean fHasResult; 

	protected History( UsageLog log ) {

		this.rid = log.requestId;
		this.begin = log.creation;
		this.duration = log.elapsed != null ? log.elapsed *1000 : null;
		this.bundle = log.bundle;
		
		try {
			this.status = log.status != null ? Status.valueOf(log.status).toString() : status; 
		} catch( Exception e ) {
			Logger.warn("Unable to decode log status: %s", log.status);
		} 

		Repo repo=null;
		if( StringUtils.isNotEmpty(log.requestId) ) {
			repo = new Repo(log.requestId,false);
		}
		OutResult out = repo != null ? repo.getResult() : null;

		
		long _exp = repo != null ? repo.getExpirationTime() : Long.MAX_VALUE;
		this.expire = _exp != Long.MAX_VALUE ? new Date(_exp) : null;
		this.label = out != null ? out.title : null;
		this.fHasResult = out != null;
		
		// try to lookup the service label throught the service declaration
		if( this.label == null ) {
			this.label = BundleRegistry.instance().getServiceTitle(log.bundle, log.service);
		}
		
	}


	Long safeLong( String value ) { 
		if( Utils.isEmpty(value)) { return null; }
		try { 
			return Long.parseLong(value);
		}
		catch( NumberFormatException e ) { 
			Logger.warn("Invalid long value: '%s'", value);
			return null;
		}
 	}
	
	Date safeDate( String value ) { 
		Long l = safeLong(value);
		return l != null ? new Date(l) : null;
	}
	
	public String getRid() { 
		return rid;
	}
	
	public String getStatus() { 
		return status;
	}
	
	
	public String getBegin() {
		return Utils.asSmartString(begin);
	}
	
	Date getBeginDate() { 
		return begin;
	}
	
	public String getExpire() { 
		return expire != null ? Utils.asSmartString(expire) : "--";
	}

	Date getExpireDate() { 
		return expire;
	}
	
	public String getDuration() { 
		if( duration == null ) { return "--"; }
		if( duration < 1000 ) { return "1 sec"; }
		return Utils.asDuration(duration);
	}
	
	
	public String getLabel() { 
		return Utils.asString(label);
	}
	
	public void setLabel(String mode) {
		this.label = mode;
	}
	
	public String getBundle() { 
		return bundle;
	}
	
	public void setBundle( String value ) { 
		this.bundle = value;
	}
	
	public boolean getHasResult() {
		return this.fHasResult;
	} 
	
	/**
	 * Serialize this {@link History} instance to an equivalent string value
	 */
	@Override
	public String toString() { 
		return Utils.dump(this, "rid", "status", "begin", "end");
	}
	
	/**
	 * Find a {@link History} instance by the request identifier
	 * 
	 * @param rid the request unique ID
	 * @return an instance of {@link History} of <code>null</code> if not exist
	 */
	public static History find( String rid ) { 

		String sql = 
				"from UsageLog as log " +
				"where " +
				"	log.requestId = '%s' and log.history='1'";
		
		List<UsageLog> result = JPA.em().createQuery( String.format(sql,rid), UsageLog.class ).getResultList();
		
		return result != null && result.iterator().hasNext() ? new History(result.iterator().next()) : null;
	}
	
	

	
	public static List<History> findBySessionAndEmailAndSinceDate(String sessionId, String email, Date since ) {
		Logger.debug("History#findBySessionAndEmail('%s','%s')", sessionId, email);
		
		String sql = 
				"from UsageLog as log " +
				"where " +
				"	log.history='1' and ( log.sessionId=?1 OR log.email=?2 ) and (log.creation >= :since OR :since is null)" +
				"order by " +
				"	log.creation desc";

		Query query = JPA.em().createQuery( sql, UsageLog.class );
		query.setParameter(1, sessionId);
		query.setParameter(2, email);
		query.setParameter("since", since);
		
		List<UsageLog> list = query.getResultList();
		Iterator<UsageLog> it = list.iterator();

		List<History> result = new ArrayList<History>( list.size() );
		while( it.hasNext() ) {
			result.add( new History(it.next()) );
		}
		return result;
	}



	public static boolean deleteByRequestId(String rid) {
		Logger.debug("History#deleteByRequestId	('%s')", rid);
		
		String sql = 
				"update UsageLog as log " +
				"set log.history = '0' " +
				"where " +
				"	log.requestId = '%s' and log.history='1'";
		

		return 1 == JPA.em().createQuery(String.format(sql, rid)).executeUpdate();
		
	}


	public static boolean deleteBySessionAndEmail(String sid, String email) {
		Logger.debug("History#deleteBySessionAndEmail('%s','%s')", sid, email);

		String sql = 
				"update UsageLog as log " +
				"set log.history = '0' " +
				"where " +
				"	log.history='1' and ( log.sessionId='%s' OR log.email='%s' ) ";


		return 0 < JPA.em().createQuery(String.format(sql, sid, email)).executeUpdate();
		
	}
	

}
