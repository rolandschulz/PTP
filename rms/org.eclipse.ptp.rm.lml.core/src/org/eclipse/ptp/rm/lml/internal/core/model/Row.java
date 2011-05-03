package org.eclipse.ptp.rm.lml.internal.core.model;

public class Row {
	public String oid;
	public LMLColor color = null;
	public Cell[] cells;
	
	public Row(String oid) {
		this.oid = oid;
	}
	
	public String toString() {
		return oid;
	}
	
	public void setCells(Cell[] cells) {
		this.cells = cells;
	}
	
	public void  setColor(LMLColor color) {
		this.color = color;
	}

}
