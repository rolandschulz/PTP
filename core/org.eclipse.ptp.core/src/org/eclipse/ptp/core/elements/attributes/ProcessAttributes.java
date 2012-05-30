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
package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.messages.Messages;

/**
 * Process attributes
 */
@Deprecated
public class ProcessAttributes {
	public enum State {
		STARTING,
		RUNNING,
		SUSPENDED,
		COMPLETED
	}

	private static final String STATE_ATTR_ID = "processState"; //$NON-NLS-1$
	private static final String STATUS_ATTR_ID = "processStatus"; //$NON-NLS-1$
	private static final String EXIT_CODE_ATTR_ID = "processExitCode"; //$NON-NLS-1$
	private static final String SIGNAL_NAME_ATTR_ID = "processSignalName"; //$NON-NLS-1$
	private static final String STDOUT_ATTR_ID = "processStdout"; //$NON-NLS-1$
	private static final String STDERR_ATTR_ID = "processStderr"; //$NON-NLS-1$
	private static final String NODEID_ATTR_ID = "processNodeId"; //$NON-NLS-1$

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = new EnumeratedAttributeDefinition<State>(
			STATE_ATTR_ID, "State", //$NON-NLS-1$
			Messages.ProcessAttributes_0, false, State.STARTING);

	private final static StringAttributeDefinition statusAttrDef = new StringAttributeDefinition(STATUS_ATTR_ID, "Status", //$NON-NLS-1$
			Messages.ProcessAttributes_8, true, ""); //$NON-NLS-1$

	private final static IntegerAttributeDefinition exitCodeAttrDef = new IntegerAttributeDefinition(EXIT_CODE_ATTR_ID,
			"Exit Code", //$NON-NLS-1$
			Messages.ProcessAttributes_2, true, 0);

	private final static StringAttributeDefinition signalNameAttrDef = new StringAttributeDefinition(SIGNAL_NAME_ATTR_ID,
			"Exit Signal", //$NON-NLS-1$
			Messages.ProcessAttributes_4, true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition stdoutAttrDef = new StringAttributeDefinition(STDOUT_ATTR_ID, "Process Stdout", //$NON-NLS-1$
			Messages.ProcessAttributes_5, false, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition stderrAttrDef = new StringAttributeDefinition(STDERR_ATTR_ID, "Process Stderr", //$NON-NLS-1$
			Messages.ProcessAttributes_6, false, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition nodeIdAttrDef = new StringAttributeDefinition(NODEID_ATTR_ID, "Process Node ID", //$NON-NLS-1$
			Messages.ProcessAttributes_7, true, ""); //$NON-NLS-1$

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { stateAttrDef, statusAttrDef, exitCodeAttrDef, signalNameAttrDef, stdoutAttrDef,
				stderrAttrDef, nodeIdAttrDef };
	}

	public static IntegerAttributeDefinition getExitCodeAttributeDefinition() {
		return exitCodeAttrDef;
	}

	public static StringAttributeDefinition getNodeIdAttributeDefinition() {
		return nodeIdAttrDef;
	}

	public static StringAttributeDefinition getSignalNameAttributeDefinition() {
		return signalNameAttrDef;
	}

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}

	public static StringAttributeDefinition getStatusAttributeDefinition() {
		return statusAttrDef;
	}

	public static StringAttributeDefinition getStderrAttributeDefinition() {
		return stderrAttrDef;
	}

	public static StringAttributeDefinition getStdoutAttributeDefinition() {
		return stdoutAttrDef;
	}
}
