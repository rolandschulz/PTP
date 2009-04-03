/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.remote.core;

import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends InputStream {
	@Override
	public int read() throws IOException {
		return -1;
	}
	
	@Override
	public int available() {
		return 0;
	}
}
