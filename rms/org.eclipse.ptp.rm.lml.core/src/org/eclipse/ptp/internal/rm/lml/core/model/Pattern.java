/* Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import org.eclipse.ptp.rm.lml.core.model.IPattern;

public class Pattern implements IPattern {

	private String columntitle;

	private String type;

	private boolean relation;

	private boolean range;

	// Relation
	private String valueRelation;

	private String relationOperator;

	// Range
	private String minValueRange;

	private String maxValueRange;

	public Pattern(String title, String type) {
		columntitle = title;
		this.type = type;
		if (columntitle == null) {
			columntitle = new String();
		}
		if (this.type == null) {
			this.type = new String("alpha"); //$NON-NLS-1$
		}
	}

	@Override
	public String getColumnTitle() {
		return columntitle;
	}

	@Override
	public String getMaxValueRange() {
		return maxValueRange;
	}

	@Override
	public String getMinValueRange() {
		return minValueRange;
	}

	@Override
	public String getRelationOperator() {
		return relationOperator;
	}

	@Override
	public String getRelationValue() {
		return valueRelation;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isRange() {
		return (range && !relation);
	}

	@Override
	public boolean isRelation() {
		return (relation && !range);
	}

	@Override
	public IPattern setRange(String minValueRange, String maxValueRange) {
		this.range = true;
		this.relation = false;
		this.minValueRange = minValueRange;
		this.maxValueRange = maxValueRange;
		if (minValueRange == null) {
			this.minValueRange = new String();
		}
		if (maxValueRange == null) {
			this.maxValueRange = new String();
		}
		return this;
	}

	@Override
	public IPattern setRelation(String relationOperator, String valueRelation) {
		this.relation = true;
		this.range = false;
		this.relationOperator = relationOperator;
		this.valueRelation = valueRelation;
		if (relationOperator == null) {
			this.relationOperator = new String("="); //$NON-NLS-1$
		}
		if (valueRelation == null) {
			this.valueRelation = new String();
		}
		return this;
	}

}
