package org.eclipse.ptp.debug.external.cdi.model;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;

/**
 */
public class RuntimeOptions extends PTPObject implements ICDIRuntimeOptions {
	public RuntimeOptions(Target t) {
		super(t);
	}

	public void setArguments(String[] args) throws CDIException {
		// Auto-generated method stub
		System.out.println("RuntimeOptions.setArguments()");
		
	}

	public void setEnvironment(Properties props) throws CDIException {
		// Auto-generated method stub
		System.out.println("RuntimeOptions.setEnvironment()");
		
	}

	public void setWorkingDirectory(String wd) throws CDIException {
		// Auto-generated method stub
		System.out.println("RuntimeOptions.setWorkingDirectory()");
		
	}

}
