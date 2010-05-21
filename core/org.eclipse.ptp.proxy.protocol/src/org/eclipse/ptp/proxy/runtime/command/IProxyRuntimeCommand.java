/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.IProxyCommand;

public interface IProxyRuntimeCommand extends IProxyCommand {
	/*
	 * Command IDs
	 */
	public static final int INIT = 1;
	public static final int MODEL_DEF = 2;
	public static final int START_EVENTS = 3;
	public static final int STOP_EVENTS = 4;
	public static final int SUBMIT_JOB = 5;
	public static final int TERMINATE_JOB = 6;
	public static final int FILTER_EVENTS = 7;
	/**
	 * @since 4.0
	 */
	public static final int SUSPEND_EVENTS = 8;
	/**
	 * @since 4.0
	 */
	public static final int RESUME_EVENTS = 9;
	/**
	 * @since 4.0
	 */
	public static final int SET_FILTERS = 10;
	/**
	 * @since 4.0
	 */
	public static final int CLEAR_FILTERS = 11;
	/**
	 * @since 4.0
	 */
	public static final int GET_ATTRIBUTES = 12;
	/**
	 * @since 4.0
	 */
	public static final int QUERY_ATTRIBUTES = 13;
}