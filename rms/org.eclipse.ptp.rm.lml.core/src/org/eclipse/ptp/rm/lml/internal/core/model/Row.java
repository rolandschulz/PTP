package org.eclipse.ptp.rm.lml.internal.core.model;

import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;

public class Row {
	public String oid;
	public JobStatusData status = null;
	public LMLColor color = null;
	public Cell[] cells;
	
	public Row(String oid) {
		this.oid = oid;
	}
	
	public String toString() {
		return oid;
	}
	
	public void setJobStatusData(JobStatusData status) {
		this.status = status;
	}
	
	public void setCells(Cell[] cells) {
		this.cells = cells;
	}
	
	public void  setColor(LMLColor color) {
		this.color = color;
	}

}
