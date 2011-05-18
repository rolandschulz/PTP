/**********************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.core.analysis;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.messages.Messages;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. MPI, OpenMP, etc.). Currently these artifacts include function calls
 * and constants. It adds markers to the source file for C code, marking the
 * position of the artifacts found.
 * 
 * @author Beth Tibbitts
 * 
 */
public class MpiCASTVisitor extends PldtAstVisitor {
	private static final String PREFIX = "MPI_"; //$NON-NLS-1$

	private static/* final */boolean traceOn = false;

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	public MpiCASTVisitor(List<String> mpiIncludes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr) {
		super(mpiIncludes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.MpiCASTVisitor_mpiCall;
		ARTIFACT_CONSTANT = Messages.MpiCASTVisitor_mpiConstant;
		ARTIFACT_NAME = "MPI"; //$NON-NLS-1$

		traceOn = CommonPlugin.getTraceOn();
		if (traceOn)
			System.out.println("MpiCASTVisitor.ctor: traceOn=" + traceOn); //$NON-NLS-1$

	}

	/**
	 * allow dynamic adding to include path? <br>
	 * Note: if this returns true, be certain to override getIncludesPrefID() and
	 * getPreferenceStore() which will be needed by the implementation to do this
	 * 
	 * @return
	 */
	@Override
	public boolean allowIncludePathAdd() {
		boolean canAsk = getPreferenceStore().getBoolean(MpiIDs.MPI_PROMPT_FOR_OTHER_INCLUDES);
		return canAsk;
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected String getIncludesPrefID() {
		return MpiIDs.MPI_INCLUDES;
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected IPreferenceStore getPreferenceStore() {
		return MpiPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path. This type name is used for messages, etc.
	 * 
	 * @return artifact type name such as "MPI", "OpenMP" etc.
	 */
	@Override
	protected String getTypeName() {
		return ARTIFACT_NAME;
	}

	/**
	 * @since 5.0
	 */
	@Override
	public boolean matchesPrefix(String name) {
		return name.startsWith(PREFIX);
	}

}