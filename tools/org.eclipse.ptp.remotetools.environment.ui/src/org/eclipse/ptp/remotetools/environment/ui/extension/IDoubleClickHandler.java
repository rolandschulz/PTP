/**
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */

package org.eclipse.ptp.remotetools.environment.ui.extension;

import org.eclipse.ptp.remotetools.environment.extension.INode;

/**
 * The interface to handle the double click on an <code>INode</code>
 * 
 * @author Hong Chang Lin
 * 
 */
public interface IDoubleClickHandler {

    /**
     * @param node
     * @return true if the double click on the node can be handled
     */
    public boolean handle(INode node);
}
