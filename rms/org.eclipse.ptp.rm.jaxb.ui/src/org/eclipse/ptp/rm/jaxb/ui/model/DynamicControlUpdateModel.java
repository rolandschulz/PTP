package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;

public abstract class DynamicControlUpdateModel extends AbstractUpdateModel {

	protected List<Arg> dynamic;

	protected DynamicControlUpdateModel(List<Arg> dynamic, ValueUpdateHandler handler) {
		super(ZEROSTR, handler);
		this.dynamic = dynamic;
	}

	protected DynamicControlUpdateModel(String name, ValueUpdateHandler handler) {
		super(name, handler);
	}

	protected String getResolvedDynamic() {
		return ArgImpl.toString(null, dynamic, lcMap);
	}

	@Override
	protected void storeValue() {
		if (canSave) {
			super.storeValue();
		}
	}
}
