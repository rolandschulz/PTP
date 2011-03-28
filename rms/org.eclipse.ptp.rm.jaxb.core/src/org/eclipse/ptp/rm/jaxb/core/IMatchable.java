package org.eclipse.ptp.rm.jaxb.core;

public interface IMatchable {
	boolean doMatch(StringBuffer segment) throws Throwable;

	boolean isSelected();

	void postProcess() throws Throwable;

	void setSelected(boolean selected);
}
