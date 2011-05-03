package org.eclipse.ptp.rm.lml.internal.core.model;

public class Cell {
	public String value;
	public Row row;
	
	public Cell(String value, Row row) {
		this.value = value;
		this.row = row;
	}
	
	public String toString() {
		return value;
	}

}
