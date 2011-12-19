/* Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.internal.core.model;

import org.eclipse.ptp.rm.lml.core.model.IPattern;

public class Pattern implements IPattern {

	private String value;

	private String relation;

	private String type;

	public Pattern(String value, String relation, String type) {
		this.value = value;
		this.relation = relation;
		this.type = type;
		if (this.value == null) {
			this.value = new String();
		}
		if (this.relation == null) {
			this.relation = new String("=");
		}

		if (this.type == null) {
			this.type = new String("alpha");
		}
	}

	public String getRelation() {
		return relation;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

}
