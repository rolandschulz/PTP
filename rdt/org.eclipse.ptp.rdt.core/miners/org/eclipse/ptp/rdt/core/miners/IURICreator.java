/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.miners;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Interface for contributors that know how to construct a URI for a given
 * filesystem.
 * 
 * @author crecoskie
 * @since 2.0
 * 
 */
public interface IURICreator {

	public URI createURIForScheme(String scheme, String host, String path, String mappedPath) throws URISyntaxException;
}
