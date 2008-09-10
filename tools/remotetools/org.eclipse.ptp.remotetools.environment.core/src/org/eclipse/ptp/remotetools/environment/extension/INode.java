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

import org.eclipse.swt.graphics.Image;

/**
 * Represents the children of the ITargetElement's children on
 * RemoteToolsEnvironmentView
 * 
 * @author Hong Chang Lin
 * 
 */
public interface INode {

    /**
     * @return all the children element
     */
    public INode[] getChildren();

    /**
     * @return the parent element
     */
    public Object getParent();

    /**
     * @return the display icon
     */
    public Image getIcon();

    /**
     * @return the display name
     */
    public String getDisplayText();

}
