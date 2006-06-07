package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.core.PTPCorePlugin;

/**
 * Resource manager status
 * </p>
 */
public class ResourceManagerStatus implements IStatus {

	/**
	 * A standard OK status with an "ok"  message.
	 *
	 */
	public static final IStatus OK_STATUS = new ResourceManagerStatus(OK, OK, "", null);
	/**
	 * A standard CANCEL status with no message.
	 * 
	 */
	public static final IStatus CANCEL_STATUS = new ResourceManagerStatus(CANCEL, 1, "", null); //$NON-NLS-1$
	/**
	 * The severity. One of
	 * <ul>
	 * <li><code>CANCEL</code></li>
	 * <li><code>ERROR</code></li>
	 * <li><code>WARNING</code></li>
	 * <li><code>INFO</code></li>
	 * <li>or <code>OK</code> (0)</li>
	 * </ul>
	 */
	private int severity = OK;

	/** Plug-in-specific status code.
	 */
	private int code;

	/** Message, localized to the current locale.
	 */
	private String message;

	/** Wrapped exception, or <code>null</code> if none.
	 */
	private Throwable exception = null;

	/** Constant to avoid generating garbage.
	 */
	private static final IStatus[] theEmptyStatusArray = new IStatus[0];

	/**
	 * Creates a new status object.  The created status has no children.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param code the plug-in-specific status code, or <code>OK</code>
	 * @param message a human-readable message, localized to the
	 *    current locale
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 */
	public ResourceManagerStatus(int severity, int code, String message, Throwable exception) {
		setSeverity(severity);
		setCode(code);
		setMessage(message);
		setException(exception);
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public IStatus[] getChildren() {
		return theEmptyStatusArray;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public int getCode() {
		return code;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public Throwable getException() {
		return exception;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public String getMessage() {
		return message;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public String getPlugin() {
		return PTPCorePlugin.PLUGIN_ID;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public int getSeverity() {
		return severity;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public boolean isMultiStatus() {
		return false;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public boolean isOK() {
		return severity == OK;
	}

	/* (Intentionally not javadoc'd)
	 * Implements the corresponding method on <code>IStatus</code>.
	 */
	public boolean matches(int severityMask) {
		return (severity & severityMask) != 0;
	}

	/**
	 * Sets the status code.
	 *
	 * @param code the plug-in-specific status code, or <code>OK</code>
	 */
	protected void setCode(int code) {
		this.code = code;
	}

	/**
	 * Sets the exception.
	 *
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 */
	protected void setException(Throwable exception) {
		this.exception = exception;
	}

	/**
	 * Sets the message.
	 *
	 * @param message a human-readable message, localized to the
	 *    current locale
	 */
	protected void setMessage(String message) {
		Assert.isLegal(message != null);
		this.message = message;
	}


	/**
	 * Sets the severity.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 */
	protected void setSeverity(int severity) {
		Assert.isLegal(severity == OK || severity == ERROR || severity == WARNING || severity == INFO || severity == CANCEL);
		this.severity = severity;
	}

	/**
	 * Returns a string representation of the status, suitable 
	 * for debugging purposes only.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Status "); //$NON-NLS-1$
		if (severity == OK) {
			buf.append("OK"); //$NON-NLS-1$
		} else if (severity == ERROR) {
			buf.append("ERROR"); //$NON-NLS-1$
		} else if (severity == WARNING) {
			buf.append("WARNING"); //$NON-NLS-1$
		} else if (severity == INFO) {
			buf.append("INFO"); //$NON-NLS-1$
		} else if (severity == CANCEL) {
			buf.append("CANCEL"); //$NON-NLS-1$
		} else {
			buf.append("severity="); //$NON-NLS-1$
			buf.append(severity);
		}
		buf.append(": "); //$NON-NLS-1$
		buf.append(" code="); //$NON-NLS-1$
		buf.append(code);
		buf.append(' ');
		buf.append(message);
		buf.append(' ');
		buf.append(exception);
		return buf.toString();
	}
}
