package org.eclipse.photran.internal.core.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.addl_langs.IModelBuilder;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.f95parser.FortranProcessor;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.SemanticError;
import org.eclipse.photran.internal.core.f95parser.SyntaxError;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranShowParseTreePreference;

/**
 * The Fortran model builder calls a <code>FortranModelBuildingVisitor</code> to create the model
 * you see in the (normal) Outline view.
 * 
 * All CDT extension languages are expected to supply a model builder.
 * @see IModelBuilder
 * 
 * @author joverbey
 */
public final class FortranModelBuilder implements IModelBuilder
{
    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;

    private Map newElements;

    public FortranModelBuilder(org.eclipse.cdt.internal.core.model.TranslationUnit tu)
    {
        this.translationUnit = tu;
        this.newElements = new HashMap();
    }

    public Map parse(boolean quickParseMode) throws Exception
    {
        String input = translationUnit.getBuffer().getContents();
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        String filename = translationUnit.getFile().getName();
        boolean wasSuccessful = true;

        try
        {
            FortranProcessor processor = new FortranProcessor();

            ParseTreeNode parseTree = processor.parse(inputStream, filename);

            FortranElement note = new FortranElement.UnknownNode(translationUnit, processor
                .lastParseWasFixedForm() ? "<Fixed Form Source>" : "<Free Form Source>");
            this.addF90Element(note);

            // Build a model (for our purposes, it's just to populate the Outline view)
            if (isParseTreeModelEnabled())
            {
                // Show full parse tree rather than Outline view
                parseTree
                    .visitUsing(new FortranParseTreeModelBuildingVisitor(translationUnit, this));
            }
            else
            {
                // Show normal Outline view
                parseTree.visitUsing(new FortranModelBuildingVisitor(translationUnit, this));
            }

            // If parser debugging is enabled, try creating a symbol table
            if (FortranProcessor.isParserDebuggingEnabled())
            {
                try
                {
                    // processor.createSymbolTableFromParseTree(parseTree);
                }
                catch (SemanticError e)
                {
                    createSemanticFailureNode(translationUnit, e);
                    wasSuccessful = false;
                }
            }
        }
        catch (SyntaxError e)
        {
            createParseFailureNode(translationUnit, e.getErrorToken());
            wasSuccessful = false;
        }
        catch (Exception e)
        {
            createParseFailureNode(translationUnit, e.getMessage());
            wasSuccessful = false;
        }

        // From CDT: important to know if the unit has parse errors or not
        translationUnit.getElementInfo().setIsStructureKnown(wasSuccessful);

        return this.newElements;
    }

    private boolean isParseTreeModelEnabled()
    {
        FortranShowParseTreePreference pref = FortranPreferences.SHOW_PARSE_TREE;
        return pref.getValue(FortranCorePlugin.getDefault().getPluginPreferences());
    }

    // --NODE CREATION METHODS-------------------------------------------

    private FortranElement createParseFailureNode(Parent parent, Token errorToken)
        throws CModelException
    {

        StringBuffer errorMessage = new StringBuffer();
        errorMessage.append("Error: Unexpected ");
        errorMessage.append(errorToken.getTerminal().getDescription());
        errorMessage.append(" \"");
        errorMessage.append(errorToken.getText());
        errorMessage.append("\"");
        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage.toString());
        element.setIdPos(errorToken.getOffset(), errorToken.getLength());
        element.setPos(errorToken.getOffset(), errorToken.getLength());
        element.setLines(errorToken.getStartLine(), errorToken.getEndLine());

        parent.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

    private FortranElement createParseFailureNode(Parent parent, String errorMessage)
        throws CModelException
    {

        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage);
        parent.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

    private FortranElement createSemanticFailureNode(Parent parent, SemanticError e)
        throws CModelException
    {

        StringBuffer errorMessage = new StringBuffer();
        errorMessage.append(e.getMessage());
        Token errorToken = e.getErrorToken();
        if (errorToken == null) errorToken = new Token();
        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage.toString());
        element.setIdPos(errorToken.getOffset(), errorToken.getLength());
        element.setPos(errorToken.getOffset(), errorToken.getLength());
        element.setLines(errorToken.getStartLine(), errorToken.getEndLine());

        parent.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

    /**
     * Called by the <code>FortranModelBuildingVisitor</code> to add nodes to the model
     * @param element
     * @return
     * @throws CModelException
     */
    FortranElement addF90Element(FortranElement element) throws CModelException
    {
        ICElement parent = element.getParent();
        if (parent instanceof Parent) ((Parent)parent).addChild(element);

        this.newElements.put(element, element.getElementInfo());

        return element;
    }
}
