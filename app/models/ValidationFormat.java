package models;

import java.io.Serializable;

/** 
 * Supported validation formats 
 * 
 * @author Paolo Di tommaso
 *
 */
public enum ValidationFormat implements Serializable {

	TEXT, 
	EMAIL,
	INTEGER,
	DECIMAL,
	DATE,
	FASTA
}
