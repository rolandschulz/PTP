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

public interface IPattern {

	public String getColumnTitle();

	public String getMaxValueRange();

	public String getMinValueRange();

	public String getRelationOperator();

	public String getType();

	public String getRelationValue();

	public boolean isRange();

	public boolean isRelation();

	public IPattern setRange(String minValueRange, String maxValueRange);

	public IPattern setRelation(String relationOperator, String valueRelation);
}
