package org.eclipse.photran.internal.core.model;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.Activator;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * The Fortran model builder calls a <code>FortranModelBuildingVisitor</code> to create the model
 * you see in the (normal) Outline view.
 * 
 * Editors can force the model builder to use fixed or free format for a given file by calling
 * <code>forceFormat</code>.  Otherwise, the format is determined by content type (i.e., by the
 * filename extension and the user's workspace preferences).
 * 
 * All CDT extension languages are expected to supply a model builder.
 * @see IContributedModelBuilder
 * 
 * @author joverbey
 */
public class FortranModelBuilder implements IFortranModelBuilder
{
    private TranslationUnit translationUnit;
    private Map newElements;
    private boolean isFixedForm;

    public void setTranslationUnit(ITranslationUnit tu)
    {
        if (!(tu instanceof ITranslationUnit)) throw new Error("Unexpected subclass of ITranslationUnit");
        
        this.translationUnit = (TranslationUnit)tu;
    }

    public void setIsFixedForm(boolean isFixedForm)
    {
        this.isFixedForm = isFixedForm;
    }


    public void parse(boolean quickParseMode) throws Exception
    {
        this.newElements = new HashMap();
        boolean wasSuccessful = true;

        IAccumulatingLexer lexer = null;
        try
        {
//            SourceForm sourceForm = isFixedForm ? SourceForm.FIXED_FORM : SourceForm.UNPREPROCESSED_FREE_FORM;
//            IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(translationUnit.getFile());
            
            IFile file = translationUnit.getFile();
            lexer = LexerFactory.createLexer(
                new ByteArrayInputStream(translationUnit.getBuffer().getContents().getBytes()),
                file.getName(),
                isFixedForm ? SourceForm.FIXED_FORM : SourceForm.UNPREPROCESSED_FREE_FORM);
            // There may be more than one FortranModelBuilder running at once, so, unfortunately, we have to
            // create a new parser each time
            IFortranAST ast = new FortranAST(file, new Parser().parse(lexer), lexer.getTokenList());
                        
            if (isParseTreeModelEnabled())
                 ast.visitUsing(new FortranParseTreeModelBuildingVisitor(translationUnit, this));
            else
                 ast.visitUsing(new FortranModelBuildingVisitor(translationUnit, this));
            
            //FortranElement note = new FortranElement.UnknownNode(translationUnit, isFixedForm ? "<Fixed Form Source>" : "<Free Form Source>");
            //this.addF90Element(note);

            /* For whatever reason, we have to only set the *parent* while we
             * parse, then go back through (now) and traverse the model in
             * preorder, executing addChild().  This doesn't make any sense,
             * but the "obvious" way of doing it all while parsing (in
             * BuildModelParserAction.java) doesn't work.
             */
            //newElements.putAll(newElts);
            //this.addF90Elements(newElements);
        }
        catch (Exception e)
        {
            FortranElement elt = createParseFailureNode(translationUnit, e.getMessage());
            if (lexer != null)
            {
                int offset = lexer.getLastTokenFileOffset();
                int length = lexer.getLastTokenLength();
                int line = lexer.getLastTokenLine();
                if (offset >= 0 && length > 0)
                {
                    elt.setIdPos(offset, length);
                    elt.setPos(offset, length);
                    if (line > 0) elt.setLines(line, line);
                }
            }
            wasSuccessful = false;
        }

        // From CDT: important to know if the unit has parse errors or not
        setIsStructureKnown(wasSuccessful);
    }

    public void setIsStructureKnown(boolean isStructureKnown)
    {
        translationUnit.setIsStructureKnown(isStructureKnown);
    }

    private boolean isParseTreeModelEnabled()
    {
        return FortranPreferences.SHOW_PARSE_TREE.getValue();
    }

    // --NODE CREATION METHODS-------------------------------------------

//    private FortranElement createParseFailureNode(Parent parent, Token errorToken)
//        throws CModelException
//    {
//
//        StringBuffer errorMessage = new StringBuffer();
//        errorMessage.append("Error: Unexpected ");
//        errorMessage.append(errorToken.getTerminal().getDescription());
//        errorMessage.append(" \"");
//        errorMessage.append(errorToken.getText());
//        errorMessage.append("\"");
//        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage.toString());
//        element.setIdPos(errorToken.getOffset(), errorToken.getLength());
//        element.setPos(errorToken.getOffset(), errorToken.getLength());
//        element.setLines(errorToken.getStartLine(), errorToken.getEndLine());
//
//        parent.addChild(element);
//        this.newElements.put(element, element.getElementInfo());
//        return element;
//
//    }

    private FortranElement createParseFailureNode(Parent parent, String errorMessage)
        throws CModelException
    {

        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage);
        parent.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

//    private FortranElement createSemanticFailureNode(Parent parent, SemanticError e)
//        throws CModelException
//    {
//
//        StringBuffer errorMessage = new StringBuffer();
//        errorMessage.append(e.getMessage());
//        Token errorToken = e.getErrorToken();
//        if (errorToken == null) errorToken = new Token();
//        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage.toString());
//        element.setIdPos(errorToken.getOffset(), errorToken.getLength());
//        element.setPos(errorToken.getOffset(), errorToken.getLength());
//        element.setLines(errorToken.getStartLine(), errorToken.getEndLine());
//
//        parent.addChild(element);
//        this.newElements.put(element, element.getElementInfo());
//        return element;
//
//    }

    /**
     * Called by the <code>FortranModelBuildingVisitor</code> to add nodes to the model
     * @param element
     * @return
     * @throws CModelException
     */
    public FortranElement addF90Element(FortranElement element) throws CModelException
    {
        if (element.getParent() == null) element.setParent(translationUnit);
            
        ICElement parent = element.getParent();
        if (parent instanceof Parent) ((Parent)parent).addChild(element);

        this.newElements.put(element, element.getElementInfo());

        return element;
    }

    private void addF90Elements(Map elts) throws CModelException
    {
        // Add all top-level elements (those with no parent) and recurse
        addF90ElementsFor(null, elts);
    }

    private void addF90ElementsFor(Parent parent, Map elts) throws CModelException
    {
        Iterator it = elts.keySet().iterator();
        while (it.hasNext())
        {
            FortranElement e = (FortranElement)it.next();
            if (e.getParent() == parent)
            {
                addF90Element(e);
                e.setIdentifier(e.getIdentifier()); // It seems to forget position info
                addF90ElementsFor(e, elts);
            }
        }
    }
}
