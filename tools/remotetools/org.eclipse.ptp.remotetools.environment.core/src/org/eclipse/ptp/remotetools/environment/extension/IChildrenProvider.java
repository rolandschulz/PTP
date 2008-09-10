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
package org.eclipse.ptp.remotetools.environment.extension;

import org.eclipse.ptp.remotetools.environment.core.ITargetElement;

/**
 * The interface to manage the children hierarchies of a target element
 * 
 * @author Hong Chang Lin
 * 
 */
public interface IChildrenProvider {

    /**
     * Return all the child objects of an ITargetElement
     * 
     * @param
     * @return
     */
    public INode[] getChildren(ITargetElement targetElement);
}
