package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class TokenImpl extends AbstractRangePart implements IJAXBNonNLSConstants {

	private String delim;
	private List<AddImpl> adds;
	private List<SetImpl> sets;
	private List<PutImpl> puts;
	private TokenImpl child;

	public TokenImpl(Token token) throws Throwable {
		delim = token.getDelim();

		RMVariableMap map = RMVariableMap.getActiveInstance();
		assert (null != map);
		if (delim != null) {
			delim = map.getString(delim);
		}

		String exp = token.getRange();
		if (exp != null) {
			this.range = new Range(exp);
		}

		Token t = token.getToken();

		if (t != null) {
			child = new TokenImpl(t);
		} else {
			child = null;
			List<?> list = token.getAdd();
			if (list != null) {
				adds = new ArrayList<AddImpl>();
				for (Object add : list) {
					adds.add(new AddImpl((Add) add));
				}
			}
			list = token.getSet();
			if (list != null) {
				sets = new ArrayList<SetImpl>();
				for (Object set : list) {
					sets.add(new SetImpl((Set) set));
				}
			}
			list = token.getPut();
			if (list != null) {
				puts = new ArrayList<PutImpl>();
				for (Object put : list) {
					puts.add(new PutImpl((Put) put));
				}
			}
		}
	}

	public void tokenize(String expression) throws Throwable {
		String[] segments = null;
		if (delim == null) {
			segments = new String[] { expression };
		} else {
			segments = expression.split(delim);
		}
		for (int i = 0; i < segments.length; i++) {
			if (range != null && !range.isInRange(i)) {
				continue;
			}
			if (child != null) {
				child.tokenize(segments[i]);
			} else {
				if (adds != null) {
					for (AddImpl add : adds) {
						add.doAdd(i, segments[i]);
					}
				}
				if (sets != null) {
					for (SetImpl set : sets) {
						set.doSet(i, segments[i]);
					}
				}
				if (puts != null) {
					for (PutImpl put : puts) {
						put.doPut(i, segments[i]);
					}
				}
			}
		}

	}
}
