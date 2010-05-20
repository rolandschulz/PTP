/**********************************************************************
 * Copyright (c) 2008,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc;

import org.eclipse.ptp.pldt.upc.messages.Messages;

/**
 * UPC Plugin constants
 * 
 * @author Beth Tibbitts
 */
public interface UPCIDs {
	// preference page name for UPC
	String UPC_INCLUDES = Messages.UPCIDs_upc_includes;

	String MARKER_ID = "org.eclipse.ptp.pldt.upc.upcMarker"; //$NON-NLS-1$
	String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.upc.upcErrorMarker"; //$NON-NLS-1$

	// artifact view id
	String UPC_VIEW_ID = "org.eclipse.ptp.pldt.upc.views.upcArtifactView"; //$NON-NLS-1$
}
