package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Which parts of output should be examined. Expression is a comma-delimited
 * list, which may also contain colon-delimited ranges; by convention, ":N" = up
 * to final element returned.
 * 
 * @author arossi
 * 
 */
public class Range implements IJAXBNonNLSConstants {

	private int[] from;
	private int[] to;

	public Range(String expression) {
		RMVariableMap map = RMVariableMap.getActiveInstance();
		assert (null != map);
		expression = map.getString(expression);
		parse(expression);
	}

	public boolean isInRange(int line) {
		for (int i = 0; i < from.length; i++) {
			if (from[i] <= line && (to[i] == -1 || line <= to[i])) {
				return true;
			}
		}
		return false;
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
			this.from[i] = Integer.parseInt(from.get(i).trim());
			String n = to.get(i);
			if (n.indexOf(LEN) >= 0) {
				this.to[i] = -1;
			} else {
				this.to[i] = Integer.parseInt(n.trim());
			}
		}
	}
}
