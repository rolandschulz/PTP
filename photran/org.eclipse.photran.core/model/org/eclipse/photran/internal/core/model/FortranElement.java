package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.addl_langs.IContributedCElement;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.photran.internal.core.f95modelparser.Token;
import org.eclipse.photran.modelicons.ModelIconsPlugin;

/**
 * Photran inherits from its CDT heritage the C Model, which is a tree representing a C workspace.
 * Projects are just beneath the root, and they have folders and translation units (files) beneath
 * them. The model also knows the high-level structure of each translation unit. E.g., it knows the
 * modules, functions, classes (C++), and fields (C++) in each file, but does not know about
 * individual statements, local variables, etc.
 * 
 * Every model element beneath a Fortran translation unit is a subclass of
 * <code>FortranElement</code>. The <code>FortranElement</code> hierarchy is a Fortran-specific
 * extension of <code>ICElement</code>.
 * 
 * Every subclass of <code>FortranElement</code> should be written as an inner class of
 * <code>FortranElement</code>. Why? Because (1) the subclasses are all tiny, and (2) prefixing
 * every instance with <code>FortranElement.</code> will make it very clear that we're working in
 * our (customized) part of the <code>ICElement</code>/model hierarchy.
 * 
 * <code>FortranElement</code> was originally a subclass of <code>SourceManipulation</code>,
 * but that wasn't right since we don't support parse tree rewriting via the
 * <code>ISourceManipulation</code> interface. So we are subclassing <code>Parent</code> instead
 * (which still isn't quite right for, say, variables, but isn't hurting anything) and implementing
 * a few methods by copying them directly from <code>SourceManipulation</code>. See also
 * <code>FortranElementInfo</code>, which also has a few methods copied from
 * <code>SourceManipulationInfo</code>.
 * 
 * TODO-Jeff: If we inherit from Parent instead, use the commented-out methods, and use
 * FortranElementInfo, pieces of the Outline view seem to be missing (things beyond two levels
 * deep?) WTF?!
 * 
 * @author joverbey
 * @see ICElement
 * @see Parent
 * @see FortranElementInfo
 */
public abstract class FortranElement extends SourceManipulation // Parent
    implements ICElement, IParent, ISourceReference, IContributedCElement
{
    /**
     * Most elements in the <code>FortranElement</code> hierarchy have a name (functions,
     * subroutines, etc.). For the ones that do, this is the <code>Token</code> for that name. It
     * is expected to contain position information within the source file.
     */
    protected Token identifier = null;

    /**
     * Most elements in the <code>FortranElement</code> hierarchy have a name (functions,
     * subroutines, etc.). For the ones that do, this is the <code>Token</code> for that name. It
     * is expected to contain position information within the source file.
     * 
     * @return the identifier <code>Token</code> for this element.
     */
    public Token getIdentifier()
    {
        return identifier;
    }

    /**
     * Most elements in the <code>FortranElement</code> hierarchy have a name (functions,
     * subroutines, etc.). For the ones that do, this sets the <code>Token</code> for that name.
     * It is expected to contain position information within the source file.
     * 
     * @param identifier The identifier <code>Token</code> for this element.
     */
    public void setIdentifier(Token identifier)
    {
        this.identifier = identifier;

        if (identifier != null)
        {
            setIdPos(identifier.getOffset(), identifier.getLength());
            setPos(identifier.getOffset(), identifier.getLength());
            setLines(identifier.getStartLine(), identifier.getEndLine());
        }
    }

	//-------------------------------------------------------------

	/**
     * Creates a new <code>FortranElement</code> under the given parent, which has the given name
     * (passed as a <code>Token</code>) from the source text.
     * 
     * @param parent
     * @param identifier
     * @param parseTreeNode
     */
	public FortranElement(Parent parent, Token identifier)
    {
		super(parent, identifier != null ? identifier.getText() : "(anonymous)", -1); // type);
        this.setIdentifier(identifier);
    }

    /**
     * Creates a new <code>FortranElement</code> under the given parent, which has the given name
     * (textual representation).
     * 
     * @param parent
     * @param name
     */
	public FortranElement(Parent parent, String name) //, int type)
    {
		super(parent, name == null ? "" : name, -1); //type);
	}

	/**
     * @return an <code>ImageDescriptor</code> for displaying alongside the element in the Outline
     *         (or similar view)
     * 
     * Note...
     * 
     * In the <code>ICElement</code> hierarchy, icons are determined by a giant <code>case</code>
     * statement in <code>CElementImageProvider#getBaseImageDescriptor</code>. Since
     * <code>case</code> statements based on types are bad (and error-prone) and we prefer to have
     * everything in one place, we simply ask each <code>FortranElement</code> to tell us what its
     * pictorial representation is. Yeah, if you want to argue that this is user interface stuff,
     * it's in the wrong place... but for us, the chances are much higher that we will change the
     * hierarchy than change the icons, so putting everything in one place is a better move.
     * 
     * To make this work, at the top of <code>CElementImageProvider#getBaseImageDescriptor</code>,
     * I added these lines: <code>
	 * if (celement instanceof FortranElement)
	 *     return ((FortranElement)celement).getBaseImageDescriptor();
	 * </code>
     */
	public Object getBaseImageDescriptor()
    {
		return getImageDescriptorForIcon("unknown.gif");
	}

	/**
     * Returns an <code>ImageDescriptor</code> for an icon in the icons folder of the core plugin.
     * Based on code in <code>CPluginImages</code>.
     * 
     * @param filename
     * @return <code>ImageDescriptor</code>
     */
	protected Object getImageDescriptorForIcon(String filename)
    {
	    return ModelIconsPlugin.getImageDescriptor("icons/" + filename);
	}

	// --- Concrete Subclasses -------------------------------------------

    /**
     * An element for any random thing that you want in the Outline view
     */
    public static class UnknownNode extends FortranElement
    {
        public UnknownNode(Parent parent, String name)
        {
            super(parent, name);
        }
    }

    /**
     * An element representing an error; will display as an error message in the Outline view
     */
    public static class ErrorNode extends FortranElement
    {
        public ErrorNode(Parent parent, String name)
        {
            super(parent, name);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("skullface.gif");
        }
    }

    public static class MainProgram extends FortranElement
    {
        public MainProgram(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("mainprogram.gif");
        }
    }

    public static class Module extends FortranElement
    {
        public Module(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("module.gif");
        }
    }

    public static class DerivedType extends FortranElement
    {
        public DerivedType(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("derivedtype.gif");
        }
    }

    public static class Function extends FortranElement
    {
        public Function(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("function.gif");
        }
    }

    public static class Subroutine extends FortranElement
    {
        public Subroutine(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif");
        }
    }

    public static class BlockData extends FortranElement
    {
        public BlockData(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }
        
        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif");
        }
    }

    public static class Variable extends FortranElement
    {
        public Variable(Parent parent, Token nameToken)
        {
            super(parent, nameToken);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("variable.gif");
        }
    }
}
