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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.remotetools.environment.extension.INode;

/**
 * Manage a list of <code>IDoubleClickHandler</code> contributed by extensions
 * 
 * @author Hong Chang Lin
 * 
 */
public class DoubleClickHandlerManager {

    private final static String EXT_HANDLER_ID = "org.eclipse.ptp.remotetools.environment.ui.doubleClickHandler";

    private List<IDoubleClickHandler> handlers;

    public DoubleClickHandlerManager() {
        processDoubleClickHandlers();
    }

    public boolean doubleClickExecute(INode node) {
        for (IDoubleClickHandler handler : handlers) {
            if (handler.handle(node)) {
                return true;
            }
        }
        return false;
    }

    private void processDoubleClickHandlers() {
        handlers = new ArrayList<IDoubleClickHandler>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry
                .getExtensionPoint(EXT_HANDLER_ID);
        IExtension[] extensions = extensionPoint.getExtensions();

        try {
            for (int i = 0; i < extensions.length; i++) {
                IExtension extension = extensions[i];
                IConfigurationElement[] elements = extension
                        .getConfigurationElements();
                IConfigurationElement element = elements[0];
                if ("doubleClickHandler".equals(element.getName())) {
                    handlers.add((IDoubleClickHandler) element
                            .createExecutableExtension("class"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
