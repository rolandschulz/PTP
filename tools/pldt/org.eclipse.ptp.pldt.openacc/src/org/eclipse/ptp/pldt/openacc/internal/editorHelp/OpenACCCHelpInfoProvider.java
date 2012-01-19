/**********************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.editorHelp;

import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * Implementation of {@link ICHelpProvider} providing hover help, dynamic help, and content assist for OpenACC API functions in CDT.
 * 
 * @author unknown (IBM)
 * @author Jeff Overbey (Illinois)
 */
public class OpenACCCHelpInfoProvider extends CHelpProviderImpl {
	@Override
	public void initialize() {
		helpBook = new OpenACCCHelpBook();
	}
}
