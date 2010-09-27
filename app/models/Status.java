package models;

import java.io.Serializable;

/** The repository status */
public enum Status implements Serializable { 
	
	READY("Ready"),
	RUNNING("Running"), 
	DONE("Done"), 
	FAILED("Failed"), 
	UNKNOWN("Unknown");  

	String value;
	Status(String value) { this.value = value; }
	
	@Override
	public String toString() { return value; }
	
	public boolean isDone() {
		return DONE.equals(this);
	}

	public boolean isRunning() {
		return RUNNING.equals(this);
	}
	
	public boolean isUnknown() {
		return UNKNOWN.equals(this);
	}
	
	public boolean isFailed() {
		return FAILED.equals(this);
	}
	
	public boolean isReady() {
		return READY.equals(this);
	}
};
