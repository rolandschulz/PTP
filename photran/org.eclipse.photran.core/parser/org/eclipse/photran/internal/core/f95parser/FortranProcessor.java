package org.eclipse.photran.internal.core.f95parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.runtime.Platform;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.preferences.FortranEnableParserDebuggingPreference;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * The entrypoint for lexing and parsing Fortran source code inside the Eclipse application. This
 * class should not be used in JUnit test cases.
 * 
 * @author joverbey
 */
public class FortranProcessor
{
    private boolean lastParseWasFixedForm = false;
    
    /**
     * Determines whether or not the given file is a fixed-format file (based on its filename
     * extension) and returns a lexer capable of processing that file. Does <i>not</i> alter the
     * value returned by <code>lastParseWasFixedForm</code>.
     * @param inputStream the file's contents
     * @param filename the name of the file (with extension)
     * @return <code>ILexer</code>
     * @throws Exception
     */
    public static ILexer createLexerFor(InputStream inputStream, String filename) throws Exception
    {
        boolean isFixedForm = hasFixedFormFileExtension(filename);

        return Lexer.createLexer(inputStream, filename, isFixedForm);
    }
    
    /**
     * Parses the given input stream as a Fortran 95 source file. The filename parameter should
     * indicate the filename of the file being parsed; it is saved in each <code>Token</code>
     * produced and used in error messages, although no one actually checks to see if it's a legal
     * filename or not.  Fixed/free format is determined automatically based on the filename.
     * @param inputStream
     * @param filename
     * @return <code>ParseResult</code>
     */
    public ParseTreeNode parse(InputStream inputStream, String filename) throws Exception
    {
        return parse(inputStream, filename, hasFixedFormFileExtension(filename));
    }

    /**
     * Parses the given input stream as a Fortran 95 source file. The filename parameter should
     * indicate the filename of the file being parsed; it is saved in each <code>Token</code>
     * produced and used in error messages, although no one actually checks to see if it's a legal
     * filename or not.
     * @param inputStream
     * @param filename
     * @param isFixedForm
     * @return <code>ParseResult</code>
     */
    public ParseTreeNode parse(InputStream inputStream, String filename, boolean isFixedForm) throws Exception
    {
        Parser parser = new Parser();
        return (ParseTreeNode)parse(inputStream, filename, isFixedForm, parser, new BuildParseTreeProductionReductions(new BuildParseTreeParserAction(parser)));
    }

    /**
     * Parses the given input stream as a Fortran 95 source file. The filename parameter should
     * indicate the filename of the file being parsed; it is saved in each <code>Token</code>
     * produced and used in error messages, although no one actually checks to see if it's a legal
     * filename or not.
     * @param inputStream
     * @param filename
     * @param isFixedForm
     * @return <code>ParseResult</code>
     */
    public Map parseForModel(InputStream inputStream, String filename, Boolean isFixedForm, TranslationUnit tu) throws Exception
    {
        boolean isFixedFormB = isFixedForm == null ? hasFixedFormFileExtension(filename) : isFixedForm.booleanValue();
        Parser parser = new Parser();
        Map/*<FortranElement, FortranElementInfo>*/ newElements = (Map)parse(inputStream, filename, isFixedFormB, parser, new BuildModelProductionReductions(new BuildModelParserAction(parser, tu)));
        return newElements;
    }

    private Object parse(InputStream inputStream, String filename, boolean isFixedForm, Parser parser, AbstractProductionReductions productionReductions) throws Exception
    {
        ILexer lexer = Lexer.createLexer(inputStream, filename, isFixedForm);

        try
        {
            Object result = parser.parse(lexer, new ParsingTable(productionReductions));
            lastParseWasFixedForm = isFixedForm; // In case we processed other files while parsing
            return result;
        }
        catch (Exception e)
        {
            if (isParserDebuggingEnabled()) e.printStackTrace();
            throw e;
        }
    }

    /**
     * Parses the given file as a Fortran 95 source file.
     * @param filename
     * @return <code>ParseResult</code>
     */
    public ParseTreeNode parse(String filename) throws Exception
    {
        return parse(new BufferedInputStream(new FileInputStream(new File(filename))), filename);
    }

    /**
     * Takes a parse tree (from the <code>parse</code> method) and builds a symbol table hierarchy
     * from it, returning the global symbol table.
     * 
     * @param parseTreeRoot
     * @return <code>SymbolTable</code>
     */
    public SymbolTable createSymbolTableFromParseTree(ParseTreeNode parseTreeRoot) throws Exception
    {
        try
        {
            SymbolTable result = SymbolTable.createSymbolTableFor(parseTreeRoot);

            if (isParserDebuggingEnabled())
            ;// System.out.println(result);

            return result;
        }
        catch (Exception e)
        {
            if (isParserDebuggingEnabled()) e.printStackTrace();

            throw e;
        }
    }

    public SymbolTable parseAndCreateSymbolTableFor(String filename) throws Exception
    {
        return createSymbolTableFromParseTree(parse(filename));
    }

    public SymbolTable parseAndCreateSymbolTableFor(InputStream in, String filename) throws Exception
    {
        return createSymbolTableFromParseTree(parse(in, filename));
    }

    public boolean lastParseWasFixedForm()
    {
        return lastParseWasFixedForm;
    }

    // -- USER PREFERENCE LOOKUPS ----------------------------------------

    /**
     * Ordinarily, we grab the list of fixed form file extensions from the user's preferences. In
     * test cases, however, the plugin doesn't exist, so we can't do that. So if you're parsing
     * outside the plugin, you must set the fixed form extensions this way, and this value will be
     * used rather than trying to load preferences from the user.
     */
    public static String[] overrideFixedFormExtensions = null;

    /**
     * Ordinarily, we grab determine whether parser debugging is enabled or not from the user's
     * preferences. In test cases, however, the plugin doesn't exist, so we can't do that. So if
     * you're parsing outside the plugin, you must set the property this way, and this value will be
     * used rather than trying to load preferences from the user. Typically, JUnit tests will set it
     * to false, since it causes a bunch of output on stdout/stderr that is useless to unit tests.
     */
    public static Boolean overrideIsParserDebuggingEnabled = null;

    /**
     * Looks at the user's preferences to determine whether the given filename has an extension
     * corresponding to fixed source form.
     * 
     * @param filename
     * @return <code>true</code> iff fixed form filename
     */
    public static boolean hasFixedFormFileExtension(String filename)
    {
        if (overrideFixedFormExtensions != null) // Used by JUnit tests
        {
            for (int i = 0; i < overrideFixedFormExtensions.length; i++)
                if (filename.endsWith(overrideFixedFormExtensions[i]))
                    return true;
            return false;
        }
        else
        {
            return Platform.getContentTypeManager().findContentTypeFor(filename).getId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE);
        }
    }

    /**
     * Looks at the user's preferences to determine whether parser debugging has been enabled.
     * 
     * @param filename
     * @return <code>true</code> iff parser debugging enabled
     */
    public static boolean isParserDebuggingEnabled()
    {
        if (overrideIsParserDebuggingEnabled != null)
            return overrideIsParserDebuggingEnabled.booleanValue();

        FortranEnableParserDebuggingPreference pref = FortranPreferences.ENABLE_PARSER_DEBUGGING;
        return pref.getValue(FortranCorePlugin.getDefault().getPluginPreferences());
    }

    /**
     * Prints the given string to standard output iff the user's preferences indicate that parser
     * debugging has been enabled.
     * 
     * @param filename
     */
    public static void printDebug(String message)
    {
        if (isParserDebuggingEnabled()) System.out.println(message);
    }
}
