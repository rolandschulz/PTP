package org.eclipse.ptp.rdt.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.core.activator.Activator;

/**
 * Convenience class for writing to the RDT core log.
 * 
 * @author Mike Kucera
 */
public class RDTLog {

	public static void log(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}
	
	public static void logError(Throwable exception, String message) {
		log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, exception));
	}
	
	public static void logError(Throwable exception) {
		logError(exception, exception.getMessage());
	}
	
	public static void logError(String message) {
		log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
	}
	
	public static void logInfo(String message) {
		log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message));
	}
	
}
