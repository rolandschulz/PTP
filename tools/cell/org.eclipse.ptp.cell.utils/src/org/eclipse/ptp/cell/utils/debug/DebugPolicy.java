/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.debug;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * Common operations used for tracing in Cell IDE plugins.
 * 
 * TODO Javadoc for policies how to use this clas.
 * 
 * @author Daniel Felix Ferber
 */
public class DebugPolicy {
	private static final String UNKNOWN_METHOD_STR = "unknown method"; //$NON-NLS-1$
	private static final String DEBUG_OPTION_INCLUDE_ID = "trace/id"; //$NON-NLS-1$
	private static final String DEBUG_OPTION_INCLUDE_THREADID = "trace/thread"; //$NON-NLS-1$
	private static final String DEBUG_OPTION_INCLUDE_TIMESTAMP = "trace/timestamp"; //$NON-NLS-1$
	private static final String DEBUG_OOPTION_DYNAMIC_TRACE = "trace/dynamic"; //$NON-NLS-1$
	private static final String DEBUG_OOPTION_MASTER = "debug"; //$NON-NLS-1$
	
	private static final String EMPTY_STRING_STR = "<empty string>"; //$NON-NLS-1$
	private static final String NULL_STR = "<null>"; //$NON-NLS-1$
	/**
	 * String constant used in .options file to mark an option as "true". Any
	 * other value will be considered as "false". Comparison is case
	 * insensitive.
	 */
	public static final String TRUE = Boolean.TRUE.toString();
	/**
	 * String constant used in .options file to mark an option as "false". Any
	 * other value will be considered as "true". Comparison is case insensitive.
	 */
	public static final String FALSE = Boolean.FALSE.toString();
	
	/**
	 * Plugin ID that is beign traced with this instance. Set by the
	 * constructor.
	 */
	String pluginId = null;
	/**
	 * Flag to indicate that the options from the .options file were already
	 * read once.
	 */
	boolean hasReadOptions = false;
	/**
	 * Flag to indicate that options shall be re-read from the .options file
	 * when starting a new operation that is to be traced.
	 */
	boolean isDynamic = false;
	
	/**
	 * ID string used to identify the plugin. For readability, it should be a
	 * shorted string that the plugin ID.
	 */ 
	String traceId = null;
	/** Flag to turn on time stamp in trace message. */
	boolean showTimeStamp = false;
	/** Flag to turn on thread ID in trace message. */
	boolean showThreadId = false;
	
	/**
	 * Set of all options IDs that are available in the .options file. Used to
	 * check if a requested option is really expected in the .options file. Used
	 * only when Eclipse is run in debug mode.
	 */
	Set<String> knownDebugProperties = new HashSet<String>();
	
	/** Flag that stores if Eclipse is running in debug mode. */
	public boolean DEBUG = false;
	
	/**
	 * Default constructor. Creates an instance configured for tracing the plugin given as parameter.
	 * If Eclipse is in debug mode, also reads the .options file in order to load all available options.
	 * @param pluginId The ID of the plugin that is being traced with this instance.
	 */
	public DebugPolicy(String pluginId) {
		this.pluginId = pluginId;
		if (Platform.inDebugMode()) {
			try {
				trace("Trace framework for {0} loaded", pluginId); //$NON-NLS-1$
				Bundle bundle = Platform.getBundle(pluginId);
				IPath path = new Path("/.options"); //$NON-NLS-1$
				URL url = FileLocator.find(bundle, path, null);
				if (url == null) {
					throw new IOException("FileLocator.find() returned null"); //$NON-NLS-1$
				}
				InputStream is = url.openStream();
				if (is == null) {
					throw new IOException("InputStream is  null"); //$NON-NLS-1$
				}
				Properties properties = new Properties();
				properties.load(is);
				Enumeration<Object> en = properties.keys();
				while (en.hasMoreElements()) {
					String elem = (String) en.nextElement();
					knownDebugProperties.add(elem);					
				}
			} catch (Exception e) {
				error("Could not load .options file: {0}", e.getMessage()); //$NON-NLS-1$
				logError(e, Messages.DebugPolicy_FailedOptionsFile, pluginId);
			}
		}
	}
	
	/**
	 * Returns the string value of a debug option for the plugin that this instance was created for.
	 * @param id The ID of the options (without plugin ID)
	 * @return The value of the option
	 */
	public String getStringOption(String id) {
		String option = this.pluginId+"/"+id; //$NON-NLS-1$
		if (Platform.inDebugMode()) {
			if (! knownDebugProperties.contains(option)) {
				error("Unknown debug option: {0}", option); //$NON-NLS-1$
				logError(Messages.DebugPolicy_UnknownDebugOptions, option);
			}
		}
		String result = Platform.getDebugOption(option);
// if (result == null) {
// error("Unknown debug option: {0}", option);
// }
		return result;
	}

	/**
	 * Returns the boolean value of a debug option for the plugin that this instance was created for.
	 * The string in the constant {@link #TRUE} is the only case insensitive value accepted as "true". Any
	 * other value, or if the option is missing, is supposed as "false".
	 * @param id The ID of the options (without plugin ID)
	 * @return The value of the option
	 */
	public boolean getBooleanOption(String id) {
		return TRUE.equalsIgnoreCase(getStringOption(id));
	}
	
	/**
	 * Ensures that the most important options are read. Returns true if plugin specific debug options should be read also.
	 * Options are always read if the .options files states that tracing is dynamic (options may change while Eclipse is running).
	 * Else, options are read only once. This method shall be called every time that is started a new operation (that requires tracing) .
	 * @return
	 */
	public boolean read() {
		if (isDynamic || ! hasReadOptions) {
			boolean inDebug = Platform.inDebugMode();
			DEBUG = inDebug &&  getBooleanOption(DEBUG_OOPTION_MASTER);
			isDynamic = getBooleanOption(DEBUG_OOPTION_DYNAMIC_TRACE);
			showTimeStamp = getBooleanOption(DEBUG_OPTION_INCLUDE_TIMESTAMP);
			showThreadId = getBooleanOption(DEBUG_OPTION_INCLUDE_THREADID);		
			traceId = getStringOption(DEBUG_OPTION_INCLUDE_ID);
			if (traceId == null) {
				traceId = this.pluginId;
			}
			hasReadOptions = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Prints a tracing message.
	 * @param pattern The message with optional place holders as used in {@link MessageFormat}.
	 * @param arguments Values for the place holders
	 */
	public void trace(String pattern, Object ... arguments) {
		trace(MessageFormat.format(pattern, arguments));
	}
	
	/**
	 * Prints a tracing message only if the parameter <code>option</code> is true. This parameter
	 * shall be one of the options read from the .options file.
	 * @param option the value of the option (true/false)
	 * @param pattern The message with optional place holders as used in {@link MessageFormat}.
	 * @param arguments Values for the place holders
	 */
	public void trace(boolean option, String pattern, Object ... arguments) {
		trace(option, MessageFormat.format(pattern, arguments));
	}

	/**
	 * Prints a tracing message.
	 * @param pattern The message
	 */
	public void trace(String message) {
		if (traceId != null) {
			System.out.print(traceId);
		}
		if (showTimeStamp) {
			System.out.print(" "+Long.toString(System.currentTimeMillis())); //$NON-NLS-1$
		}
		if (showThreadId) {
			String name = Thread.currentThread().getName();
			if (name == null) {
				name = Long.toString(Thread.currentThread().getId());
			}
			System.out.print(" "+name); //$NON-NLS-1$
		}
		if ((traceId != null) || showThreadId || showTimeStamp) {
			System.out.print(": "); //$NON-NLS-1$
		}
		System.out.println(message);
		System.out.flush();
	}

	/**
	 * Prints a tracing message only if the parameter <code>option</code> is true. This parameter
	 * shall be one of the options read from the .options file.
	 * @param option the value of the option (true/false)
	 * @param pattern The message
	 */
	public void trace(boolean option, String message) {
		if (option) {
			trace(message);
		}
	}
	
	/**
	 * Prints a error message.
	 * @param pattern The message with optional place holders as used in {@link MessageFormat}.
	 * @param arguments Values for the place holders
	 */
	public void error(String pattern, Object ... arguments) {
		error(MessageFormat.format(pattern, arguments));
	}
	
	/**
	 * Prints a error message only if the parameter <code>option</code> is true. This parameter
	 * shall be one of the options read from the .options file.
	 * @param option the value of the option (true/false)
	 * @param pattern The message with optional place holders as used in {@link MessageFormat}.
	 * @param arguments Values for the place holders
	 */
	public void error(boolean option, String pattern, Object ... arguments) {
		error(option, MessageFormat.format(pattern, arguments));
	}
		
	/**
	 * Prints a error message, only if the parameter <code>option</code> is true. This parameter
	 * shall be one of the options read from the .options file.
	 * @param option the value of the option (true/false)
	 * @param pattern The message
	 */
	public void error(String message) {
		System.out.print("(!) "); //$NON-NLS-1$
		trace(message);
	}

	/**
	 * Prints a error message.
	 * @param pattern The message
	 */
	public void error(boolean option, String message) {
		if (option) {
			error(message);
		}
	}
	
	/**
	 * Prints a error message for the exception.
	 * @param e.the exception that caused the error
	 */
	public void error(Throwable e) {
		error("Unexpected exception: "+e.getMessage()); //$NON-NLS-1$
	}

	/**
	 * Prints a error message for the exception, only if the parameter <code>option</code> is true. This parameter
	 * shall be one of the options read from the .options file.
	 * @param option the value of the option (true/false)
	 * @param e.the exception that caused the error
	 */
	public void error(boolean option, Throwable e) {
		if (option) {
			error(e);
		}
	}

	
	/**
	 * Prints a message that a method was called. This parameter
	 * shall be one of the options read from the .options file. The method name and the class are got from the call stack.
	 */
	public void enter() {
		StackTraceElement trace = getStackTraceElement();
		String functionName = getMethodName(trace);
		trace("(=>) "+functionName+"()"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Prints a message that a method was called. 
	 * The method name and the class are got from the call stack.
	 * @param parameters Parameters of the method whose values are important to be shown in the message..
	 */
	public void enter(Object ... parameters) {
		StackTraceElement trace = getStackTraceElement();
		String functionName = getMethodName(trace);
		String parameterList = getParameterList(parameters);
		trace("(=>) "+functionName+"("+parameterList+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Prints a message that a method was called, only if the parameter <code>option</code> is true. 
	 * This parameter shall be one of the options read from the .options file. 
	 * The method name and the class are got from the call stack.
	 * @param option the value of the option (true/false)
	 * @param parameters Parameters of the method whose values are important to be shown in the message..
	 */	
	public void enter(boolean option, Object ... parameters) {
		if (option) {
			enter(parameters);
		}
	}
	
	/**
	 * Prints a message that a method was called, only if the parameter <code>option</code> is true. 
	 * This parameter shall be one of the options read from the .options file. 
	 * The method name and the class are got from the call stack.
	 * @param option the value of the option (true/false)
	 */	
	public void enter(boolean option) {
		if (option) {
			enter();
		}
	}

	/**
	 * Prints a message that a method returned.
	 * The method name and the class are got from the call stack.
	 */	
	public void exit() {
		StackTraceElement trace = getStackTraceElement();
		String functionName = getMethodName(trace);
		trace("(<=) "+functionName); //$NON-NLS-1$
	}
	
	/**
	 * Prints a message that a method returned. 
	 * The method name and the class are got from the call stack.
	 * @param the returnValue The value that is being returned
	 */	
	public void exit(Object returnValue) {
		StackTraceElement trace = getStackTraceElement();
		String functionName = getMethodName(trace);
		String returnString = getReturnString(returnValue);
		trace("(<=) "+functionName+": "+returnString); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Prints a message that a method returned, only if the parameter <code>option</code> is true.
	 * This parameter shall be one of the options read from the .options file. 
	 * The method name and the class are got from the call stack.
	 * @param option the value of the option (true/false)
	 * @param the returnValue The value that is being returned
	 */	
	public void exit(boolean option, Object returnValue) {
		if (option) {
			exit(returnValue);
		}
	}
	
	/**
	 * Prints a message that a method returned, only if the parameter <code>option</code> is true.
	 * This parameter shall be one of the options read from the .options file. 
	 * The method name and the class are got from the call stack.
	 * @param option the value of the option (true/false)
	 */	
	public void exit(boolean option) {
		if (option) {
			exit();
		}
	}
	
	public void exit(boolean option, String string, Object ... params) {
		if (option) {
			exit(string, params);
		}
	}
	
	public void exit(String string, Object[] params) {
		exit(MessageFormat.format(string, params));
	}

	public void pass(boolean option) {
		if (option) {
			pass();
		}
	}
	
	public void pass() {
		StackTraceElement trace = getStackTraceElement();
		String functionName = getMethodName(trace);
		trace("(==) "+functionName); //$NON-NLS-1$
	}
	
	public void pass(boolean option, Object ...parameters) {
		if (option) {
			pass();
		}
	}
	
	public void pass(Object ...parameters) {
		pass(getParameterList(parameters));
	}

	public void pass(boolean option, String pattern, Object ...parameters) {
		if (option) {
			pass(pattern, parameters);
		}
	}
	
	public void pass(String pattern, Object ...parameters) {
		pass(MessageFormat.format(pattern, parameters));
	}

	/**
	 * Formats return value of a function.
	 * 
	 * @param returnValue
	 * @return
	 */
	private String getReturnString(Object returnValue) {
		String returnString = null;
		if (returnValue == null) {
			returnString = NULL_STR;
		} else {
			returnString = returnValue.toString();
			if (returnString.length() == 0) returnString = EMPTY_STRING_STR;
		}
		return returnString;
	}

	/**
	 * Extracts and formats method name from StackTraceElement.
	 * 
	 * @param trace
	 * @return
	 */
	private String getMethodName(StackTraceElement trace) {
		if (trace == null) {
			return UNKNOWN_METHOD_STR;
		} else {
			return trace.getClassName()+"."+trace.getMethodName(); //$NON-NLS-1$
		}
	}
	
	/**
	 * Formats parameter list.
	 * 
	 * @param parameters
	 * @return
	 */
	private String getParameterList(Object... parameters) {
		String parameterList = ""; //$NON-NLS-1$
		for (int i = 0; i < parameters.length; i++) {
			String p = null;
			if (parameters[i] == null) {
				p = NULL_STR;
			} else {
				p = parameters[i].toString();
			}
			if (i>0) parameterList += ", "; //$NON-NLS-1$
			parameterList += p;
		}
		return parameterList;
	}


	/**
	 * Searches the current stack trace for the top most method calls that does
	 * not belong to this class (DebugPolicy). This method is used in order to
	 * discover which method called a method from DebugPoliciy, even when
	 * DebugPoliciy calls methods from itself.
	 * 
	 * @return The StackTraceElement of the method that called DebugPoliciy.
	 */
	private StackTraceElement getStackTraceElement() {
		Throwable t = new Throwable();
		StackTraceElement stack [] = t.getStackTrace();
		if (stack == null) return null;
		if (stack.length == 0) return null;
		
		int index = 0;
		while ((index < stack.length) && stack[index].getClassName().equals(DebugPolicy.class.getName())) index++;
		
		if (index >= stack.length) {
			/*
			 * If all stack trace contains only DebugPolicy method calls, then
			 * return the bottom entry. This only may happen when debugging this
			 * class.
			 */
			return stack[stack.length-1];
		} else {
			/*
			 * Return the element that was found.
			 */
			return stack[index];
		}
	}
	
	public void logStatus(IStatus status) {
		try {
			Bundle bundle = Platform.getBundle(pluginId);
			ILog log = Platform.getLog(bundle);
			log.log(status);
		} catch (Exception e) {
			/* Lets be safe. Provide some feedback even if logging fails. */
			System.err.println(Messages.DebugPolicy_FailedLogStatus);
			System.err.println(status.getMessage());
			if (status.getException() != null) {
				status.getException().printStackTrace(System.err);
			}
			e.printStackTrace(System.err);
		}
	}
	
	public void logError(Throwable exception, String message, Object ... parameters) {
		logStatus(new Status(IStatus.ERROR, pluginId, IStatus.OK, MessageFormat.format(message, parameters), exception));		
	}

	public void logError(String message, Object ... parameters) {
		logError(null, message, parameters);
	}

	public void logError(Throwable e) {
		if (e instanceof CoreException) {
			CoreException coreException = (CoreException) e;
			logStatus(coreException.getStatus());	
		} else {
			logError(e, Messages.DebugPolicy_InternalErrorMessage);
		}
	}
}
