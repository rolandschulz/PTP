package org.eclipse.fdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.fdt.ui.*;

/**
 * Convenience class for error exceptions thrown inside JavaUI plugin.
 */
public class FortranUIStatus extends Status {

	public FortranUIStatus(int code, String message, Throwable throwable) {
		super(IStatus.ERROR, FortranUIPlugin.getPluginId(), code, message, throwable);
	}
	
	private FortranUIStatus(int severity, int code, String message, Throwable throwable) {
		super(severity, FortranUIPlugin.getPluginId(), code, message, throwable);
	}
	
	public static IStatus createError(int code, Throwable throwable) {
		String message= throwable.getMessage();
		if (message == null) {
			message= throwable.getClass().getName();
		}
		return new FortranUIStatus(IStatus.ERROR, code, message, throwable);
	}

	public static IStatus createError(int code, String message, Throwable throwable) {
		return new FortranUIStatus(IStatus.ERROR, code, message, throwable);
	}
	
	public static IStatus createWarning(int code, String message, Throwable throwable) {
		return new FortranUIStatus(IStatus.WARNING, code, message, throwable);
	}

	public static IStatus createInfo(int code, String message, Throwable throwable) {
		return new FortranUIStatus(IStatus.INFO, code, message, throwable);
	}
	
}


