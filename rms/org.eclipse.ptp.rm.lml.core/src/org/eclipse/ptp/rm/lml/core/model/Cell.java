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


public class Cell {
	public String value;
	public Row row;

	public Cell(String value, Row row) {
		this.value = value;
		this.row = row;
	}

	@Override
	public String toString() {
		return value;
	}

}
