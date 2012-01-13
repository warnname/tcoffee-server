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

	
	public String requestId;
	
	public Timestamp creation;
	
	public String ip;
	
	public String bundle;
	
	public String service;
	
	public String duration;
	
	public String status;
	
	public Long elapsed;
	
	public String source = "web";

	public String email;

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
