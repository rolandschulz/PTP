package org.eclipse.photran.internal.core.model;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.FixedFormReplacement;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.sourceform.SourceForm;

/**
 * This is a Fortran model builder which uses the Fortran parser to construct the model.
 * See {@link IFortranModelBuilder}.
 * <p>
 * This is Photran's default model builder, assuming the VPG plug-ins are available, so the model
 * you see in the Outline view is probably constructed by this model builder.  There is also
 * {@link org.eclipse.photran.internal.core.model.SimpleFortranModelBuilder}, a simpler model
 * builder which is based solely on the Fortran lexer.
 * <p>
 * Editors can force the model builder to use fixed or free format for a given file by calling
 * {@link #setIsFixedForm(boolean)}.  Otherwise, the format is determined by content type (i.e., by
 * the filename extension and the user's workspace preferences).
 * <p>
 * Internally, this model builder uses a {@link FortranModelBuildingVisitor} to construct the model
 * by visiting the Fortran AST.
 *
 * @author Jeff Ooverbey
 *
 * @see IFortranModelBuilder
 * @see IContributedModelBuilder
 */
@SuppressWarnings("restriction")
public class FortranModelBuilder implements IFortranModelBuilder
{
    private TranslationUnit translationUnit;
    private Map<ICElement, Object /*CElementInfo*/> newElements;

    public void setTranslationUnit(ITranslationUnit tu)
    {
        if (!(tu instanceof TranslationUnit)) throw new Error("Unexpected subclass of ITranslationUnit"); //$NON-NLS-1$

        this.translationUnit = (TranslationUnit)tu;
    }

    public void parse(boolean quickParseMode) throws Exception
    {
        this.newElements = new HashMap<ICElement, Object /*CElementInfo*/>();
        boolean wasSuccessful = true;

        IAccumulatingLexer lexer = null;
        try
        {
            IFile file = translationUnit.getFile();
            ISourceForm sourceForm = determineSourceForm(file);
            String filename = determineFilename(file);
            lexer = new ASTLexerFactory().createLexer(
                new StringReader(translationUnit.getBuffer().getContents()),
                file,
                filename,
                sourceForm);
            // There may be more than one FortranModelBuilder running at once, so, unfortunately, we have to
            // create a new parser each time
            IFortranAST ast = new FortranAST(file, new Parser().parse(lexer), lexer.getTokenList());

            createSourceFormNode(SourceForm.descriptionFor(file));

            if (isParseTreeModelEnabled())
            {
                 LoopReplacer.replaceAllLoopsIn(ast.getRoot());
                 ast.accept(new FortranParseTreeModelBuildingVisitor(translationUnit, this));
            }
            else
            {
                 ast.accept(new FortranModelBuildingVisitor(translationUnit, this));
            }

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
            String message = e.getMessage();
            if (!e.getClass().equals(Exception.class))
                message = e.getClass().getSimpleName() + ": " + message; // Not a legit parser error //$NON-NLS-1$
            FortranElement elt = createParseFailureNode(translationUnit, message);
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

    private String determineFilename(IFile file)
    {
        if (file == null)
            return ""; //$NON-NLS-1$
        else if (isLocal(file)) // C preprocessor requires absolute path
            return file.getLocation().toFile().getAbsolutePath();
        else
            return file.getName();
    }

    private ISourceForm determineSourceForm(IFile file)
    {
        ISourceForm sourceForm = SourceForm.of(file);
        if (isLocal(file) && file.getProject() != null)
            return sourceForm.configuredWith(new IncludeLoaderCallback(file.getProject()));
        else
            return sourceForm;
    }

    private boolean isLocal(IFile file)
    {
        return file != null && file.getLocation() != null;
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

    private FortranElement createSourceFormNode(String desc) throws CModelException
    {
        desc = "<" + desc + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        FortranElement element = new FortranElement.UnknownNode(translationUnit, desc);
        translationUnit.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

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
     * Callback method invoked by the {@link FortranModelBuildingVisitor} to add nodes to the model.
     * 
     * @param element
     * @return element
     * 
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

    /**
     * Callback method invoked by the {@link FortranModelBuildingVisitor} to set file position
     * information for a node.
     */
    public void configureElement(FortranElement elt, Token nameToken)
    {
        if (nameToken != null)
        {
            elt.setElementName(nameToken.getText());
            
            // Don't attempt to highlight identifiers that come from INCLUDE files or
            // macro expansions.  We can't highlight something that's in another file,
            // and likewise we'll punt on trying to highlight the macro.
            if (nameToken.getPreprocessorDirective() == null
                || nameToken.getPreprocessorDirective() instanceof FixedFormReplacement)
            {
                int fileOffset = nameToken.getFileOffset();
                int length     = nameToken.getLength();
                int line       = nameToken.getLine();
                
                elt.setIdPos(fileOffset, length);
                elt.setPos(fileOffset, length);
                elt.setLines(line, line);
            }
        }
        else
        {
            elt.setElementName("(anonymous)"); //$NON-NLS-1$
        }
    }
}
