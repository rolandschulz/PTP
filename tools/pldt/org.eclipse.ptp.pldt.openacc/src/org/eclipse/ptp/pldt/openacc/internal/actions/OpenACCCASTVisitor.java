/**********************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.actions;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.IDs;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;

/**
 * DOM-walker that collects &quot;artifacts&quot; related to OpenACC, currently function calls and constants.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public final class OpenACCCASTVisitor extends PldtAstVisitor {
	private static final String PREFIX = "acc_"; //$NON-NLS-1$
	boolean traceOn = false;

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	/**
	 * Constructor. Invoked exclusively from {@link RunAnalyseOpenACCcommandHandler}.
	 */
	OpenACCCASTVisitor(List<String> includes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr) {
		super(includes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.OpenACCCASTVisitor_openacc_call;
		ARTIFACT_CONSTANT = Messages.OpenACCCASTVisitor_openacc_constant;
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically
	 * update the pref store e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected String getIncludesPrefID() {
		return IDs.PREF_INCLUDES;
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically
	 * update the pref store e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Note we need to actually DO this test now because artifacts hidden inside preprocessor expansions
	 * otherwise bring along all their "friends" of fn calls and ids within the macro expansion that aren't
	 * really artifacts.<br>
	 */
	@Override
	public boolean matchesPrefix(String name) {
		return name.startsWith(PREFIX);
	}
}