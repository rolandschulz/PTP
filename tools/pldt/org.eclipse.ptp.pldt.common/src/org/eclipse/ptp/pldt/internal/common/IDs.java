/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.internal.common;

/**
 * Misc. IDs for preferences, markers, etc.
 * 
 * @author beth tibbitts
 * 
 */
public interface IDs
{
	String P_RUN_ANALYSIS = "runAnalysis"; //$NON-NLS-1$
	String P_ECHO_FORCE = "forceEcho"; //$NON-NLS-1$
	String FILENAME = "filename"; //$NON-NLS-1$
	String NAME = "name"; //$NON-NLS-1$
	String DESCRIPTION = "description"; //$NON-NLS-1$

	String ID = "uniqueID"; //$NON-NLS-1$

	/**
	 * note built-in marker id too... not sure this is used
	 * compare with IMarker.LINE_NUMBER which is probably used instead.
	 */
	String LINE = "theLineNo"; //$NON-NLS-1$
	String NEWLINE = "theNewLineNo"; //$NON-NLS-1$

	/** marker attribute for the extra info saved for each marker/artifact */
	String CONSTRUCT_TYPE = "constructType"; //$NON-NLS-1$
	/**
	 * Key for storing preference of whether or not to show popup confirmation
	 * dialog when analysis is complete.
	 */
	String SHOW_ANALYSIS_CONFIRMATION = "showAnalysisConfirmation"; //$NON-NLS-1$
	String UNIQUE_ID = "uniqueID"; //$NON-NLS-1$

}
