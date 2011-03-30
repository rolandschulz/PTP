package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface IVariableMap {

	Map<String, ?> getDiscovered();

	String getString(String value);

	String getString(String jobId, String value);

	Map<String, ?> getVariables();

	void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration) throws CoreException;

}
