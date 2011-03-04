package org.eclipse.ptp.rm.jaxb.core.data.impl;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.data.Add;
import org.eclipse.ptp.rm.jaxb.core.data.Append;
import org.eclipse.ptp.rm.jaxb.core.data.Assign;
import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.data.Set;

public class AssignFactory {

	private AssignFactory() {
	}

	public static IAssign createIAssign(Assign assign) {
		String field = assign.getName();
		Add add = assign.getAdd();
		if (add != null) {
			return new AddImpl(field, add);
		}
		Append append = assign.getAppend();
		if (append != null) {
			return new AppendImpl(field, append);
		}
		Put put = assign.getPut();
		if (put != null) {
			return new PutImpl(field, put);
		}
		Set set = assign.getSet();
		if (set != null) {
			return new SetImpl(field, set);
		}
		return null; // should not happen
	}
}
