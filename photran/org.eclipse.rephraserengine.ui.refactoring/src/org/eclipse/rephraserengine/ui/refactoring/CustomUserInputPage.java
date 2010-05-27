/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * In a plugin.xml file, when a refactoring is contributed to the <i>refactorings</i> extension
 * point using either the <tt>resourceRefactoring</tt> or <tt>editorRefactoring</tt> tag, if
 * the <code>inputPage</code> attribute is supplied, this must be the base class of the class
 * named in that attribute.
 * <p>
 * This is essentially just a {@link UserInputWizardPage} with a no-argument constructor and a
 * field (with accessor and mutator methods) to store a refactoring.
 *
 * @author Jeff Overbey
 *
 * @param R the type of refactoring for which this wizard page provides input
 * 
 * @since 1.0
 */
public abstract class CustomUserInputPage<R extends Refactoring> extends UserInputWizardPage
{
    protected R refactoring = null;

    public CustomUserInputPage()
    {
        super("Refactoring");
    }

    /**
     * Sets the refactoring on which this wizard will operate.
     *
     * @param refactoring non-<code>null</code>
     */
    public final void setRefactoring(R refactoring)
    {
        if (refactoring == null)
            throw new IllegalArgumentException(
                "CustomUserInputPage#setRefactoring(R refactoring): refactoring may not be null");

        this.refactoring = refactoring;
    }

    /**
     * Returns the refactoring on which this wizard will operate.
     *
     * @return the refactoring on which this wizard will operate (non-<code>null</code>)
     */
    @Override public final R getRefactoring()
    {
        if (refactoring == null)
            throw new IllegalStateException(
                "CustomUserInputPage#getRefactoring() may not be invoked until " +
                "CustomUserInputPage#setRefactoring(R) has been invoked");

        return refactoring;
    }

    public abstract void createControl(Composite parent);
}
