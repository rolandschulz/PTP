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
package org.eclipse.rephraserengine.core.vpg;

/**
 * Creates various objects for a particular {@link VPG}.
 * <p>
 * Specifically, an {@link IVPGComponentFactory} can
 * <ul>
 * <li> provide its caller with the {@link IVPGNode}/{@link NodeRef} object corresponding to a given AST node.
 * </ul>
 * When the VPG is first being constructed and initialized, the {@link IVPGComponentFactory} is used to create
 * <ul>
 * <li> the error/warning log ({@link VPGLog}),
 * <li> the database ({@link VPGDB}), and
 * <li> the {@link VPGWriter}.
 * </ul>
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 3.0
 */
public interface IVPGComponentFactory<A, T, R extends IVPGNode<T>>
{
    public abstract VPGLog<T, R> createLog();

    public abstract VPGDB<A, T, R> createDatabase(VPGLog<T,R> log);

    public abstract VPGWriter<A, T, R> createVPGWriter(VPGDB<A, T, R> db, VPGLog<T,R> log);
    
    /**
     * @return an {@link IVPGNode}/{@link NodeRef} referring to the AST node/token at the given
     *         position in the given file.
     */
    public abstract R getVPGNode(String filename, int offset, int length);
}
