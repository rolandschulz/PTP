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
package org.eclipse.ptp.remotetools.environment.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.ptp.remotetools.environment.extension.IChildrenProvider;
import org.eclipse.ptp.remotetools.environment.extension.INode;
import org.eclipse.ptp.remotetools.environment.extension.IProcessMemberVisitor;
import org.eclipse.ptp.remotetools.environment.extension.ProcessExtensions;

/**
 * Manage a list of <code>IChildrenProvider</code> contributed by extensions
 * 
 * @author Hong Chang Lin
 * 
 */
public class ChildrenProviderManager {

    private final static String EXT_PROVIDER_ID = "org.eclipse.ptp.remotetools.environment.core.childrenProvider"; //$NON-NLS-1$

    private List<IChildrenProvider> providers;

    public ChildrenProviderManager() {
        processChildrenProviders();
    }

    public INode[] getChildren(ITargetElement targetElement) {
        List<INode> children = new ArrayList<INode>();
        for (IChildrenProvider provider : providers) {
            INode[] objs = provider.getChildren(targetElement);
            for (int i = 0; i < objs.length; i++) {
                children.add(objs[i]);
            }
        }

        return children.toArray(new INode[0]);
    }
    
    private void processChildrenProviders() {
        providers = new ArrayList<IChildrenProvider>();
        ProcessExtensions.process(EXT_PROVIDER_ID, new IProcessMemberVisitor() {

            public Object process(IExtension extension,
                    IConfigurationElement member) {
                Object mprovider;
                try {

                    mprovider = member.createExecutableExtension("class"); //$NON-NLS-1$
                    if (IChildrenProvider.class.isAssignableFrom(mprovider
                            .getClass())) {
                        providers.add((IChildrenProvider) mprovider); //$NON-NLS-1$
                    }
                } catch (CoreException e) {
                    mprovider = null;
                }

                return mprovider;
            }

        });
    }
}
