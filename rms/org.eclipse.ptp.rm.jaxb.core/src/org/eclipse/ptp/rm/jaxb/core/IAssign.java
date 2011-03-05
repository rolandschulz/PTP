package org.eclipse.ptp.rm.jaxb.core;

public interface IAssign {

	@SuppressWarnings("rawtypes")
	void assign(Object target, String[] values) throws Throwable;
}
