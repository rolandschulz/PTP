package org.eclipse.photran.cdtinterface.core;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IScope2;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * GoF-style Adapter which turns a Fortran parse tree into a minimalist IASTTranslationUnit
 * with names and bindings
 * 
 * @author joverbey
 */
public class FortranIASTTranslationUnitAdapter implements IASTTranslationUnit
{
    protected String path;
    
    public FortranIASTTranslationUnitAdapter(String path)
    {
        this.path = path;
    }
    
    public IASTDeclaration[] getDeclarations()
    {
        System.out.println("getDeclarations");
        return new IASTDeclaration[] {};
    }

    public void addDeclaration(IASTDeclaration declaration)
    {
        System.out.println("addDeclaration");
    }

    public IScope getScope()
    {
        System.out.println("getScope");
        return null;
    }

    public IASTName[] getDeclarations(IBinding binding)
    {
        System.out.println("getDeclarations");
        return new IASTName[] {};
    }

    public IASTName[] getDefinitions(IBinding binding)
    {
        System.out.println("getDefinitions");
        return new IASTName[] {};
    }

    public IASTName[] getReferences(IBinding binding)
    {
        System.out.println("getReferences");
        return new IASTName[] {};
    }

    public IASTNodeLocation[] getLocationInfo(int offset, int length)
    {
        System.out.println("getLocationInfo");
        return new IASTNodeLocation[] {};
    }

    public IASTNode selectNodeForLocation(String path, int offset, int length)
    {
        System.out.println("selectNodeForLocation");
        return null;
    }

    public IASTPreprocessorMacroDefinition[] getMacroDefinitions()
    {
        System.out.println("getMacroDefinitions");
        return new IASTPreprocessorMacroDefinition[] {};
    }

    public IASTPreprocessorIncludeStatement[] getIncludeDirectives()
    {
        System.out.println("getIncludeDirectives");
        return new IASTPreprocessorIncludeStatement[] {};
    }

    public IASTPreprocessorStatement[] getAllPreprocessorStatements()
    {
        System.out.println("getAll");
        return new IASTPreprocessorStatement[] {};
    }

    public IASTProblem[] getPreprocessorProblems()
    {
        System.out.println("getPreprocessorProblems");
        return new IASTProblem[] {};
    }

    public String getUnpreprocessedSignature(IASTNodeLocation[] locations)
    {
        System.out.println("getUnpreprocessedSignature");
        return null;
    }

    public String getFilePath()
    {
        System.out.println("getFilePath");
        return null;
    }

    public IASTFileLocation flattenLocationsToFile(
                                                   IASTNodeLocation[] nodeLocations)
    {
        System.out.println("flattenLocationsToFile");
        return null;
    }

    public IDependencyTree getDependencyTree()
    {
        System.out.println("get");
        return null;
    }

    public String getContainingFilename(int offset)
    {
        System.out.println("getContainingFilename");
        return null;
    }

    public ParserLanguage getParserLanguage()
    {
        System.out.println("getParserLanguage");
        return null;
    }

    protected IPDOM index = null;

    public IPDOM getIndex()
    {
        System.out.println("getIndex");
        return index;
    }

    public void setIndex(IPDOM index)
    {
        System.out.println("setIndex");
        this.index = index;
    }

    public ILanguage getLanguage()
    {
        System.out.println("getLanguage");
        return new FortranLanguage();
    }

    public IASTTranslationUnit getTranslationUnit()
    {
        return this;
    }

    public IASTNodeLocation[] getNodeLocations()
    {
        System.out.println("getNodeLocations");
        return new IASTNodeLocation[] {};
    }

    public IASTFileLocation getFileLocation()
    {
        System.out.println("getFileLocation");
        return null;
    }

    public String getContainingFilename()
    {
        System.out.println("getContainingFilename");
        return null;
    }

    public IASTNode getParent()
    {
        System.out.println("getParent");
        return null;
    }

    public void setParent(IASTNode node)
    {
        System.out.println("setParent");
    }

    public ASTNodeProperty getPropertyInParent()
    {
        System.out.println("getPropertyInParent");
        return null;
    }

    public void setPropertyInParent(ASTNodeProperty property)
    {
        System.out.println("setPropertyInParent");
    }

    public boolean accept(ASTVisitor visitor)
    {
        System.out.println("accept");
        return false;
    }

    public String getRawSignature()
    {
        System.out.println("getRawSignature");
        return null;
    }

    public IScope2 getScope(IASTNode child, ASTNodeProperty childProperty)
    {
        System.out.println("getScope");
        return null;
    }
}
