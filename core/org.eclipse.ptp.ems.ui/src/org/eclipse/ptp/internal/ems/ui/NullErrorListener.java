/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.ui;

import org.eclipse.ptp.ems.ui.IErrorListener;

/**
 * An {@link IErrorListener} which ignores all errors (a Null Object).
 * 
 * @author Jeff Overbey
 */
public final class NullErrorListener implements IErrorListener {
	@Override
	public void errorRaised(String message) {
	}

	@Override
	public void errorCleared() {
	}
}
