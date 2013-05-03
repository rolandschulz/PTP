/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.core.model;

import org.eclipse.ptp.rm.lml.core.JobStatusData;

public class Row {
	public String oid;
	public JobStatusData status = null;
	public LMLColor color = null;
	public Cell[] cells;

	public Row() {

	}

	public void setCells(Cell[] cells) {
		this.cells = cells;
	}

	public void setColor(LMLColor color) {
		this.color = color;
	}

	public void setJobStatusData(JobStatusData status) {
		this.status = status;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public String toString() {
		return oid;
	}

}
