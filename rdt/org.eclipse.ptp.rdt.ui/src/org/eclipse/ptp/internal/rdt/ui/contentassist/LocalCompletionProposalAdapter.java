/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import org.eclipse.core.resources.IProject;

/**
 * Provides local index-based completions to CDT's content assist framework.
 */
public class LocalCompletionProposalAdapter extends AbstractCompletionProposalAdapter {

	protected IContentAssistService getService(IProject project) {
		return new LocalContentAssistService();
	}
}
