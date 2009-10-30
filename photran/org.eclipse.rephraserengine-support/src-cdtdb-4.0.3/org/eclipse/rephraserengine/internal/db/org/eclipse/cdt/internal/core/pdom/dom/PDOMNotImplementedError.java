/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.dom;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMNotImplementedError extends Error {

    public static final long serialVersionUID = 0;

    public PDOMNotImplementedError() {
        super();
    }
    
    public PDOMNotImplementedError(String message) {
        super(message);
    }
}
