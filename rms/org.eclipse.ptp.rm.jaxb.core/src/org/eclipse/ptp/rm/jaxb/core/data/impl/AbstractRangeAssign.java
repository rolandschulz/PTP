/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public abstract class AbstractRangeAssign extends AbstractAssign {

	protected class Range {

		private final String expression;
		private int[] from;
		private int[] to;
		private int len;

		public Range(String expression) {
			RMVariableMap map = RMVariableMap.getActiveInstance();
			assert (null != map);
			this.expression = map.getString(expression);
		}

		public List<Object> findInRange(String[] values) {
			List<Object> found = new ArrayList<Object>();
			for (int i = 0; i < from.length; i++) {
				if (from[i] == to[i]) {
					found.add(values[from[i]]);
				} else {
					for (int j = from[i]; j < to[i]; j++) {
						found.add(values[j]);
					}
				}
			}
			return found;
		}

		public boolean isInRange(int line) {
			for (int i = 0; i < from.length; i++) {
				if ((from[i] == line && line <= to[i]) || (from[i] < line && line < to[i])) {
					return true;
				}
			}
			return false;
		}

		public void setLen(int len) {
			this.len = len;
			parse(expression);
		}

		private int maybeInterpretLength(String n) {
			int i = -1;
			if (n.indexOf(LEN) >= 0) {
				String[] lenExp = n.split(HYPH);
				if (lenExp.length == 2) {
					i = len - Integer.parseInt(lenExp[1]);
				} else {
					i = len;
				}
			} else {
				i = Integer.parseInt(n.trim());
			}
			return i;
		}

		private void parse(String expression) {
			List<String> from = new ArrayList<String>();
			List<String> to = new ArrayList<String>();
			String[] commas = expression.split(CM);
			for (int i = 0; i < commas.length; i++) {
				String[] colon = commas[i].split(CO);
				if (colon.length == 2) {
					from.add(colon[0]);
					to.add(colon[1]);
				} else {
					from.add(colon[0]);
					to.add(colon[0]);
				}
			}

			this.from = new int[from.size()];
			this.to = new int[from.size()];

			for (int i = 0; i < this.from.length; i++) {
				this.from[i] = maybeInterpretLength(from.get(i).trim());
				this.to[i] = maybeInterpretLength(to.get(i).trim());
			}
		}
	}

	protected Range range;
}
