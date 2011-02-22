/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg.eclipse;

import org.eclipse.rephraserengine.core.vpg.IVPGComponentFactory;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.NodeRef;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGLog;

/**
 * An {@link IVPGComponentFactory} for an {@link EclipseVPG}.
 * <p>
 * This class overrides {@link IVPGComponentFactory#createVPGWriter(VPGDB, VPGLog)}, requiring that
 * method to return an {@link EclipseVPGWriter}.
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 3.0
 */
public interface IEclipseVPGComponentFactory<A, T, R extends IVPGNode<T>>
         extends IVPGComponentFactory<A, T, R>
{
    public abstract EclipseVPGWriter<A, T, R> createVPGWriter(VPGDB<A, T, R> db, VPGLog<T,R> log);
}
