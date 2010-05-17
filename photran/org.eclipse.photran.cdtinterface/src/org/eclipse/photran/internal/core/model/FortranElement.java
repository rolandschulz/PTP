/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IContributedCElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.photran.internal.cdtinterface.CDTInterfacePlugin;

/**
 * An Fortran element in the C Model.
 * <p>
 * Every model element beneath a Fortran Translation Unit is a subclass of
 * <code>FortranElement</code>. The <code>FortranElement</code> hierarchy is a Fortran-specific
 * extension of <code>ICElement</code>.
 * 
 * @author Jeff Overbey
 * 
 * @see ICElement
 * @see Parent
 */
/*
 * Every subclass of <code>FortranElement</code> should be written as an inner class of
 * <code>FortranElement</code>. Why? Because (1) the subclasses are all tiny, and (2) prefixing
 * every instance with <code>FortranElement.</code> will make it very clear that we're working in
 * our (customized) part of the <code>ICElement</code>/model hierarchy.
 * 
 * <code>FortranElement</code> was originally a subclass of <code>SourceManipulation</code>, but
 * that wasn't right since we don't support parse tree rewriting via the
 * <code>ISourceManipulation</code> interface. So we are subclassing <code>Parent</code> instead
 * (which still isn't quite right for, say, variables, but isn't hurting anything) and implementing
 * a few methods by copying them directly from <code>SourceManipulation</code>. See also
 * <code>FortranElementInfo</code>, which also has a few methods copied from
 * <code>SourceManipulationInfo</code>.
 */
@SuppressWarnings("restriction")
public abstract class FortranElement extends SourceManipulation
    implements ICElement, IParent, ISourceReference, IContributedCElement
{
    private static final String ICONS_DIRECTORY = "icons/model/"; //$NON-NLS-1$

    private static final int NO_TYPE = -1;

    /**
     * Creates an empty <code>FortranElement</code> under the given parent.
     */
    public FortranElement(Parent parent)
    {
        super(parent, "", NO_TYPE); //$NON-NLS-1$
    }

    public Object getAdapter(Class required)
    {
        if (ImageDescriptor.class.equals(required))
            return getBaseImageDescriptor();
        else
            return super.getAdapter(required);
    }

    protected abstract ImageDescriptor getBaseImageDescriptor();

    /**
     * Returns an <code>ImageDescriptor</code> for an icon in the icons folder of the core plugin.
     * Based on code in <code>CPluginImages</code>.
     * 
     * @param filename
     * @return <code>ImageDescriptor</code>
     */
    public static ImageDescriptor getImageDescriptorForIcon(String filename)
    {
        return CDTInterfacePlugin.getImageDescriptor(ICONS_DIRECTORY + filename);
    }

    /**
     * Returns an <code>ImageDescriptor</code> for elements that don't have a dedicated icon.
     * 
     * @return <code>ImageDescriptor</code>
     */
    public static ImageDescriptor unknownImageDescriptor()
    {
        return getImageDescriptorForIcon("unknown.gif"); //$NON-NLS-1$
    }

    // --- Concrete Subclasses -------------------------------------------

    /**
     * An element for anything that is not covered by one of the specific classes below.
     * <p>
     * One of these elements can be added to the model, for example, to display an informative
     * message in the Outline view.
     */
    public static class UnknownNode extends FortranElement
    {
        public UnknownNode(Parent parent, String name)
        {
            super(parent);
            setElementName(name);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("unknown.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing an error; will display as an error message with an corresponding icon
     * in the Outline view.
     */
    public static class ErrorNode extends FortranElement
    {
        public ErrorNode(Parent parent, String name)
        {
            super(parent);
            setElementName(name);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("skullface.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a main program (PROGRAM X ... END PROGRAM).
     */
    public static class MainProgram extends FortranElement
    {
        public MainProgram(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("mainprogram.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a module (MODULE X ... END MODULE).
     */
    public static class Module extends FortranElement
    {
        public Module(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("module.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a Fortran 2008 submodule (SUBMODULE (X) Y ... END SUBMODULE).
     */
    public static class Submodule extends FortranElement
    {
        public Submodule(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("submodule.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a derived type (TYPE X ... END TYPE).
     */
    public static class DerivedType extends FortranElement
    {
        public DerivedType(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("derivedtype.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a function (FUNCTION X ... END FUNCTION).
     */
    public static class Function extends FortranElement
    {
        public Function(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("function.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a subroutine (SUBROUTINE X ... END SUBROUTINE).
     */
    public static class Subroutine extends FortranElement
    {
        public Subroutine(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a subroutine or function when we're not sure which one it is (e.g.,
     * in an EXTERNAL or MODULE PROCEDURE statement)
     */
    public static class Subprogram extends FortranElement
    {
        public Subprogram(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing an interface declaration (INTERFACE ... END INTERFACE).
     */
    public static class Interface extends FortranElement
    {
        public Interface(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return unknownImageDescriptor();
        }
    }

    /**
     * An element representing a block data subprogram (BLOCK DATA X ... END BLOCK DATA).
     */
    public static class BlockData extends FortranElement
    {
        public BlockData(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif"); //$NON-NLS-1$
        }
    }

    /**
     * An element representing a variable declaration.
     */
    public static class Variable extends FortranElement
    {
        public Variable(Parent parent)
        {
            super(parent);
        }

        public ImageDescriptor getBaseImageDescriptor()
        {
            return imageDescriptor();
        }

        public static ImageDescriptor imageDescriptor()
        {
            return getImageDescriptorForIcon("variable.gif"); //$NON-NLS-1$
        }
    }
}
