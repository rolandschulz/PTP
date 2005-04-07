package org.eclipse.ptp.debug.core.cdi.model;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;

/**
 */
public class RuntimeOptions extends PTPObject implements ICDIRuntimeOptions {
	
	public RuntimeOptions(Target t) {
		super(t);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setArguments(String)
	 */
	public void setArguments(String[] args) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setEnvironment(Properties)
	 */
	public void setEnvironment(Properties props) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions#setWorkingDirectory(String)
	 */
	public void setWorkingDirectory(String wd) throws CDIException {
	}

}
