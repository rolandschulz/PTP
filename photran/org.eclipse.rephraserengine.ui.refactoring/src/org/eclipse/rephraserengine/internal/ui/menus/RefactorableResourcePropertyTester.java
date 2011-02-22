/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.menus;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.rephraserengine.ui.menus.RefactorMenu;

/**
 * A property tester that returns true iff the Refactor menu for the current selection is non-empty.
 * 
 * @author Jeff Overbey
 */
public class RefactorableResourcePropertyTester extends PropertyTester
{
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        return !new RefactorMenu().isEmpty();
    }
}