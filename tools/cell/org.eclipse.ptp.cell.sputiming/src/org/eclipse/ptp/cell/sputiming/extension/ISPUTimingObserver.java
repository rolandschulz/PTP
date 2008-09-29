/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.extension;

import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author Richard Maciel
 * @since 1.2.3
 */
public interface ISPUTimingObserver {
	public void afterFileGeneration(IPath filename);
}
