package models;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;
import util.Utils;

@Entity
@Table(name="USAGE_LOG")
public class UsageLog extends Model {

	/** The request unique ID */
	public String requestId;
	
	/** The date of creation of the request */
	public Timestamp creation;
	
	/** The requestor IP address */
	public String ip;
	
	/** The requested service bundle */
	public String bundle;
	
	/** The request service name */
	public String service;
	
	/** The requested time (as formatted string) to complete the request */
	public String duration;
	
	/** The current status */
	public String status;
	
	/** Time required to complete the request (seconds) */
	public Long elapsed;
	
	/** The request source, it can be 'web' for the web application or other client */
	public String source = "web";

	/** The requestor email address (if providede) */
	public String email;
	
	/** The requestor Play! session ID */
	public String sessionId;
	
	/** 
	 * Flag used to include this request in to the user 'history' page. By default '1' (yes), set to '0'
	 * to exclude a record from the user history log (logical delete)
	 */
	public String history = "1";

	/* 
	 * Gepo location information 
	 */
	
	/** Location Longitude */
	public String lng;
	
	/** Locaiton latitude */
	public String lat;

	/** Location country code */
	@Column(name="COUNTRY_CODE")
	public String countryCode;
	
	/** Location country name */
	public String country;
	
	/** Location city name*/
	public String city;
	
	/** Location data provider */
	@Column(name="LOC_PROVIDER")
	public String locationProvider;
	
	
	
	public String getCreationFmt() { 
		return creation != null ? Utils.asSmartString(creation) : null;
	}
}
