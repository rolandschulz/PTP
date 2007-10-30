/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.launcher.core;

/**
 * A listener that receives the application output and allows adding customization steps.
 * The launch observers are defined by extension points.
 * The user can select one launch observer to observer de application execution.
 * @author Daniel Felix Ferber
 */
public interface ILaunchObserver extends ILaunchIntegration {
	void receiveOutput(String text);
}
