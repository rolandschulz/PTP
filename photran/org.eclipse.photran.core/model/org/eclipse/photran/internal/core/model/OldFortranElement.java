package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.addl_langs.IAdditionalLanguageElement;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
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
public abstract class OldFortranElement extends SourceManipulation // Parent
    implements ICElement, IParent, ISourceReference, IAdditionalLanguageElement
{

    /**
     * With the exception of unknown and error nodes, every element in the
     * <code>FortranElement</code> hierarchy corresponds to part of its container's parse tree.
     * For example, a function corresponds to a "function-subprogram-node." This is the
     * <code>ParseTreeNode</code> that corresponds to this <code>FortranElement</code>.
     */
    protected ParseTreeNode parseTreeNode = null;

    /**
     * With the exception of unknown and error nodes, every element in the
     * <code>FortranElement</code> hierarchy corresponds to part of its container's parse tree.
     * For example, a function corresponds to a "function-subprogram-node."
     * 
     * @return the <code>ParseTreeNode</code> corresponding to this <code>FortranElement</code>
     */
    public ParseTreeNode getParseTreeNode()
    {
        return parseTreeNode;
    }

    /**
     * With the exception of unknown and error nodes, every element in the
     * <code>FortranElement</code> hierarchy corresponds to part of its container's parse tree.
     * For example, a function corresponds to a "function-subprogram-node." This sets the
     * <code>ParseTreeNode</code> corresponding to this <code>FortranElement</code>.
     * 
     * @param parseTreeNode
     */
    public void setParseTreeNode(ParseTreeNode parseTreeNode)
    {
        this.parseTreeNode = parseTreeNode;
    }

    // -------------------------------------------------------------

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
    }

	//-------------------------------------------------------------

	/**
     * Creates a new <code>FortranElement</code> under the given parent, which has the given name
     * (passed as a <code>Token</code>) and corresponds to the given part of the parse tree for
     * the file in which it appears.
     * 
     * @param parent
     * @param identifier
     * @param parseTreeNode
     */
	public OldFortranElement(Parent parent, Token identifier,
	//int type,
			ParseTreeNode parseTreeNode)
    {
		super(parent, identifier != null ? identifier.getText() : "(anonymous)", -1); // type);
        this.parseTreeNode = parseTreeNode;
        this.identifier = identifier;

        if (identifier != null)
        {
            setIdPos(identifier.getOffset(), identifier.getLength());
            setPos(identifier.getOffset(), identifier.getLength());
            setLines(identifier.getStartLine(), identifier.getEndLine());
        }

        if (parseTreeNode != null)
        {
            Token firstToken = ParseTreeSearcher.findFirstTokenIn(parseTreeNode);
            Token lastToken = ParseTreeSearcher.findLastTokenIn(parseTreeNode);
            if (firstToken != null && lastToken != null)
            {
                int length = (lastToken.getOffset() + lastToken.getLength())
                    - firstToken.getOffset() - 1;
                setPos(firstToken.getOffset(), length);
                setLines(firstToken.getStartLine(), lastToken.getEndLine());
            }
        }
    }

    /**
     * Creates a new <code>FortranElement</code> under the given parent, which has the given name
     * (textual representation).
     * 
     * @param parent
     * @param name
     */
	public OldFortranElement(Parent parent, String name) //, int type)
    {
		super(parent, name == null ? "" : name, -1); //type);
		this.parseTreeNode = null;
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
//		try {
//			String iconName = filename;
//			URL fgIconBaseURL = new URL(CCorePlugin.getDefault().getBundle()
//					.getEntry("/"), "icons/");
//			URL iconURL = new URL(fgIconBaseURL, iconName);
//			return ImageDescriptor.createFromURL(iconURL);
			return ModelIconsPlugin.getImageDescriptor("icons/" + filename);
//		} catch (MalformedURLException e) {
//			CCorePlugin.log(e);
//			return null;
//		}
	}

//	//--------------------------------------------------------------------
//	// Inherited CElement abstract methods, copied from SourceManipulation
//	//--------------------------------------------------------------------
//
//    protected CElementInfo createElementInfo()
//    {
//        return new FortranElementInfo(this);
//    }
//
//    /*
//     * @see JavaElement#generateInfos
//     */
//    protected void generateInfos(Object info, Map newElements,
//                                 IProgressMonitor pm) throws CModelException
//    {
//        Openable openableParent = (Openable) getOpenableParent();
//        if (openableParent == null) { return; }
//
//        CElementInfo openableParentInfo = (CElementInfo) CModelManager
//                        .getDefault().getInfo(openableParent);
//        if (openableParentInfo == null)
//        {
//            openableParent.generateInfos(openableParent.createElementInfo(),
//                                         newElements, pm);
//        }
//        newElements.put(this, info);
//    }
//
//    public IResource getResource()
//    {
//        return null;
//    }
//
//    /**
//     * @see ISourceReference
//     */
//    public String getSource() throws CModelException
//    {
//        return getFortranElementInfo().getSource();
//    }
//
//    /**
//     * @see ISourceReference
//     */
//    public ISourceRange getSourceRange() throws CModelException
//    {
//        return getFortranElementInfo().getSourceRange();
//    }
//
//    /**
//     * @see IMember
//     */
//    public ITranslationUnit getTranslationUnit()
//    {
//        try
//        {
//            return getFortranElementInfo().getTranslationUnit();
//        }
//        catch (CModelException e)
//        {
//            return null;
//        }
//    }
//
//    protected FortranElementInfo getFortranElementInfo()
//                                                                throws CModelException
//    {
//        return (FortranElementInfo) getElementInfo();
//    }

	// --- Concrete Subclasses -------------------------------------------

    /**
     * An element for any random thing that you want in the Outline view
     */
    public static class UnknownNode extends OldFortranElement
    {
        public UnknownNode(Parent parent, String name)
        {
            super(parent, name);
        }
    }

    /**
     * An element representing an error; will display as an error message in the Outline view
     */
    public static class ErrorNode extends OldFortranElement
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

    public static class MainProgram extends OldFortranElement
    {
        public MainProgram(Parent parent, ParseTreeNode mainProgramNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(mainProgramNode), mainProgramNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("mainprogram.gif");
        }
    }

    public static class Module extends OldFortranElement
    {
        public Module(Parent parent, ParseTreeNode moduleNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(moduleNode), moduleNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("module.gif");
        }
    }

    public static class DerivedType extends OldFortranElement
    {
        public DerivedType(Parent parent, ParseTreeNode derivedTypeDefNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(derivedTypeDefNode),
                derivedTypeDefNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("derivedtype.gif");
        }
    }

    public static class Function extends OldFortranElement
    {
        public Function(Parent parent, ParseTreeNode functionSubprogramNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(functionSubprogramNode),
                functionSubprogramNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("function.gif");
        }
    }

    public static class Subroutine extends OldFortranElement
    {
        public Subroutine(Parent parent, ParseTreeNode subroutineSubprogramNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(subroutineSubprogramNode),
                subroutineSubprogramNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif");
        }
    }

    public static class BlockData extends OldFortranElement
    {
        public BlockData(Parent parent, ParseTreeNode blockDataSubprogramNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(blockDataSubprogramNode),
                blockDataSubprogramNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("subroutine.gif");
        }
    }

    public static class Variable extends OldFortranElement
    {
        public Variable(Parent parent, ParseTreeNode nameNode)
        {
            super(parent, ParseTreeSearcher.findFirstIdentifierIn(nameNode), nameNode);
        }

        public Object getBaseImageDescriptor()
        {
            return getImageDescriptorForIcon("variable.gif");
        }
    }
}
