package models;

import java.sql.Timestamp;

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

	public String location;
	
	public String hostname;
	
	public String getCreationFmt() { 
		return creation != null ? Utils.asSmartString(creation) : null;
	}
}
