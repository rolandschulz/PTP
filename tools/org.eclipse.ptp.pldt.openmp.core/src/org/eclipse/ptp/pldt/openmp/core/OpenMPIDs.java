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
package org.eclipse.ptp.pldt.openmp.core;

import org.eclipse.ptp.pldt.openmp.core.messages.Messages;

/**
 * General IDs - moved to internal class, shouldn't be needed outside this project
 * 
 * @author Beth Tibbitts
 * @deprecated
 * @see org.eclipse.ptp.pldt.openmp.internal.core
 * 
 * 
 */
public class OpenMPIDs {

	public static final String OpenMP_INCLUDES = Messages.OpenMPIDs_OpenMP_includes;
	public static final String OpenMP_BUILD_CMD = "gcc -fopenmp"; //$NON-NLS-1$

}
