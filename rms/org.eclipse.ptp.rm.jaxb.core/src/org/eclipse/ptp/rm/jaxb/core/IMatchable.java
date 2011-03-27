package org.eclipse.ptp.rm.jaxb.core;


public interface IMatchable {
	void postProcess() throws Throwable;

	boolean doMatch(StringBuffer segment) throws Throwable;
}
