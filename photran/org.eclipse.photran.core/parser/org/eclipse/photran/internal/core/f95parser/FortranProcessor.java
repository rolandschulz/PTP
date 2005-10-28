package org.eclipse.photran.internal.core.f95parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.preferences.FortranEnableParserDebuggingPreference;
import org.eclipse.photran.internal.core.preferences.FortranFixedFormExtensionListPreference;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * The entrypoint for lexing and parsing Fortran source code inside the Eclipse application. This
 * class should not be used in JUnit test cases.
 * 
 * @author joverbey
 */
public class FortranProcessor
{
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

    private boolean lastParseWasFixedForm = false;

    /**
     * Parses the given input stream as a Fortran 95 source file. The filename parameter should
     * indicate the filename of the file being parsed; it is saved in each <code>Token</code>
     * produced and used in error messages, although no one actually checks to see if it's a legal
     * filename or not.
     * @param inputStream
     * @param filename
     * @return <code>ParseResult</code>
     */
    public ParseTreeNode parse(InputStream inputStream, String filename) throws Exception
    {
        boolean wasFixedForm = hasFixedFormFileExtension(filename);

        ILexer lexer = Lexer.createLexer(inputStream, filename, wasFixedForm);
        Parser parser = new Parser();

        try
        {
            ParseTreeNode result = parser.parse(lexer);
            lastParseWasFixedForm = wasFixedForm; // In case we processed other files while
            // parsing
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
        String[] fixedFormExtensions;

        if (overrideFixedFormExtensions != null) // Used by JUnit tests
            fixedFormExtensions = overrideFixedFormExtensions;
        else
        {
            FortranFixedFormExtensionListPreference pref = FortranPreferences.FIXED_FORM_EXTENSION_LIST;
            fixedFormExtensions = pref.parseCurrentValue();
        }

        for (int i = 0; i < fixedFormExtensions.length; i++)
            if (filename.endsWith(fixedFormExtensions[i])) return true;

        return false;
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
