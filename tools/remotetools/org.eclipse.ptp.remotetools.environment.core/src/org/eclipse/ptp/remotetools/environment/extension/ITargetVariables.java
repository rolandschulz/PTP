/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.extension;

/**
 * Interface that provides support for resolution
 * of variables on each environment type. This interface must
 * be implemented by each plugin that extends the 
 * <extension point name here> extension point
 * 
 * @author Richard Maciel
 *
 */
public interface ITargetVariables {
	public String getSystemWorkspace();
}
