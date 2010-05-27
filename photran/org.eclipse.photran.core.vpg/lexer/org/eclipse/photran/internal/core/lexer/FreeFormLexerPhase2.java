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
package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Phase 2 of lexical analysis: Distinguishes keywords and identifiers.
 * <p>
 * Phase 1 is a "dumb" JFlex lexer ({@link FreeFormLexerPhase1}) which tokenizates the input
 * assuming that every entry in its keyword/keyphrase list is NOT an identifier.  Unfortunately,
 * this isn't correct: Fortran has no problem with variables named "if" and arrays named "function".
 * Additionally, a token like "len=" will be recognized in an assignment statement like "len=3", in
 * which case it should be split into two tokens (T_IDENT T_EQUALS).
 * <p>
 * <code>FreeFormLexerPhase2</code> is responsible for deciding which keywords should really be
 * identifiers.  It reads an entire statement from the JFlex lexer, then uses a combination of
 * Sale's algorithm and some additional analyses to make these decisions.  It then passes on the
 * (modified) tokens to the parser.
 *
 * @author Jeff Overbey
 *
 * @see FreeFormLexerPhase1
 */
/*
 * This class dates back to about 2004 or 2005, before Java 5 was available.  I added type
 * parameters and @SuppressWarnings to mollify Java 5 compilers, but the use of Vectors and
 * Iterators is clearly outdated, and it is still littered with superfluous downcasts.
 */
public class FreeFormLexerPhase2 implements ILexer
{
    private ILexer yylex;

    private Vector<IToken> tokenStream = new Vector<IToken>();
    private Vector<Integer> lineNumbers = new Vector<Integer>();
    private Vector<Integer> colNumbers = new Vector<Integer>();
    private Vector<FileOrIFile> files = new Vector<FileOrIFile>();
    private Vector<Integer> fileOffsets = new Vector<Integer>();
    private Vector<Integer> streamOffsets = new Vector<Integer>();
    private Vector<Integer> lengths = new Vector<Integer>();
    private ListIterator<IToken> tokenIt;
    private ListIterator<Integer> lineIt;
    private ListIterator<Integer> colIt;
    private ListIterator<FileOrIFile> fileIt;
    private ListIterator<Integer> fileOffsetIt;
    private ListIterator<Integer> streamOffsetIt;
    private ListIterator<Integer> lengthIt;

    private IToken lastToken = null;
    private FileOrIFile lastTokenFile = null;
    private int lastTokenLine = 1, lastTokenCol = 1, lastTokenFileOffset = 0, lastTokenStreamOffset = 0, lastTokenLength = 0;

    private int firstTokenPos = 0;
    private int idPos = -1;
    private VarDeclIdPosPair varDeclIdPosPair = null;
    private boolean openContextComma = false;
    private boolean openContextEquals = false;
    private boolean letterFollowsParenthetical = false;
    private int tokenFollowingParentheticalPos = -1;
    private int[] parenDepth;
    private boolean[] retainAsKeyword;

    /**
     * Map token IDs to rules that determine (based on context) the contexts
     * in which those tokens should be keywords.
     */
    HashMap<Terminal, LinkedList<Rule>> rules = new HashMap<Terminal, LinkedList<Rule>>();

//    /**
//     * Creates a new scanner.
//     * There is also java.io.Reader version of this constructor.
//     *
//     * @param   in  the java.io.Inputstream to read input from.
//     * @param filename  the name of the file being read
//     */
//    public FreeFormLexerPhase2(PreprocessingReader reader, String filename)
//    {
//      yylex = new FreeFormLexerPhase1(reader);
//      this.filename = filename;
//      yylex.setFilename(filename);  // Phase 1 should know the filename too
//      buildAdditionalRules();
//    }

    /**
     * Creates a new scanner.
     *
     * @param phase1Lexer  the phase 1 lexer to read tokens from
     * @param filename  the name of the file being read
     */
    public FreeFormLexerPhase2(ILexer phase1Lexer)
    {
      this.yylex = phase1Lexer;
      buildAdditionalRules();
    }

    /**
     * Resumes scanning until the next regular expression is matched,
     * the end of input is encountered or an I/O-Error occurs.
     *
     * @return      the next token
     * @exception   Exception  if any I/O-Error or lexer exceptions occur
     */
    public IToken yylex() throws IOException, LexerException
    {
        if (tokenIt == null || !tokenIt.hasNext())
            processNextStatement();

        if (tokenIt.hasNext())
        {
            lastToken = (IToken)tokenIt.next();

            lastTokenLine = ((Integer)lineIt.next()).intValue();
            lastTokenCol = ((Integer)colIt.next()).intValue();
            lastTokenFile = (FileOrIFile)fileIt.next();
            lastTokenFileOffset = ((Integer)fileOffsetIt.next()).intValue();
            lastTokenStreamOffset = ((Integer)streamOffsetIt.next()).intValue();
            lastTokenLength = ((Integer)lengthIt.next()).intValue();

            lastToken.setLine(lastTokenLine);
            lastToken.setCol(lastTokenCol);
            lastToken.setPhysicalFile(lastTokenFile);
            lastToken.setFileOffset(lastTokenFileOffset);
            lastToken.setStreamOffset(lastTokenStreamOffset);
            lastToken.setLength(lastTokenLength);

            //System.out.println(lastToken);
            return lastToken;
        }
        else return null;
    }

    /**
     * Read a statement into <code>tokenStream</code> using the JFlex lexer,
     * and replace keywords with identifiers as necessary
     */
    private void processNextStatement() throws IOException, LexerException
    {
        readNextStatement();

        retainAsKeyword = new boolean[tokenStream.size()];
        for (int i = 0; i < retainAsKeyword.length; i++)
            retainAsKeyword[i] = false;

        firstTokenPos = stmtBeginsWithLabel() ? 1 : 0;
        if (stmtBeginsWithNameColon()) firstTokenPos += 2;

        parenDepth = new int[tokenStream.size()];
        for (int i = 0; i < parenDepth.length; i++)
            parenDepth[i] = 0;

        salesScan();
        markSalesChanges();

        // Find the identifier naming the subprogram
        idPos = getSubprogramDeclIdPos();

        // Find the identifier naming the variable
        varDeclIdPosPair = getVarDeclIdPos();

//        if (((IToken)tokenStream.get(firstTokenPos)).getTerminal() == Terminal.T_INQUIRE)
//            System.err.println("!");

        markAdditionalChanges();    // Must have idPos set

        markSubprogramChanges();    // Must have idPos set

        markFortran2003Changes();

        applyChanges();

//        for (int i = 0; i < tokenStream.size(); i++)
//        {
//          IToken t = (IToken)tokenStream.elementAt(i);
//          System.out.print(t.getTerminal());
//          System.out.print("/");
//          //System.out.print(", " + parenDepth[i]);
//          System.out.print(t.getText());
//          System.out.print(retainAsKeyword[i] ? "(keyword)" : "(ident)");
//          System.out.print("   ");
//        }
//        System.out.println();

        tokenIt = tokenStream.listIterator();
        lineIt = lineNumbers.listIterator();
        colIt = colNumbers.listIterator();
        fileIt = files.listIterator();
        fileOffsetIt = fileOffsets.listIterator();
        streamOffsetIt = streamOffsets.listIterator();
        lengthIt = lengths.listIterator();
    }

    /**
     * Read an entire statement into tokenStream using the JFlex lexer
     */
    private void readNextStatement() throws IOException, LexerException
    {
        IToken t;
        tokenStream.clear();
        lineNumbers.clear();
        colNumbers.clear();
        files.clear();
        fileOffsets.clear();
        streamOffsets.clear();
        lengths.clear();
        do
        {
            t = yylex.yylex();
            if (t != null)
            {
                tokenStream.add(t);
                lineNumbers.add(new Integer(yylex.getLastTokenLine()));
                colNumbers.add(new Integer(yylex.getLastTokenCol()));
                files.add(yylex.getLastTokenFile());
                fileOffsets.add(new Integer(yylex.getLastTokenFileOffset()));
                streamOffsets.add(new Integer(yylex.getLastTokenStreamOffset()));
                lengths.add(new Integer(yylex.getLastTokenLength()));
            }
            // if (t != null) { System.out.print(t.id + ":"); }
        }
        while (t != null && t.getTerminal() != Terminal.T_EOS && t.getTerminal() != Terminal.END_OF_INPUT);
    }

    /* ----- IDENTIFY CHANGES -----------------------------------------------*/

    /**
     * To apply Sale's algorithm, we must identify whether there is
     * <ul>
     * <li>an open context comma,
     * <li>an open context equals, and/or
     * <li>a letter immediately following a right parenthesis
     * </ul>
     *
     * For convenience, we also set the parenDepth[] array here.  E.g.,
     * <pre>
     * Token stream:   a ( b ( c ) ( d ) e ) f
     * Paren depth:    0 1 1 2 2 1 2 2 1 1 0 0
     * </pre>
     */
    private void salesScan()
    {
        openContextComma = false;
        openContextEquals = false;
        letterFollowsParenthetical = false;

        int currentParenDepth = 0;          // Track nested parentheses
        boolean justClosedParen = false;    // Does this token immediately follow T_RPAREN?
        boolean firstParenthetical = true;  // Is this the first non-nested T_RPAREN?
        boolean inArrayLiteral = false;     // Are we inside an (/ array literal /)
        for (int i = 0; i < tokenStream.size(); i++)
        {
            IToken t = (IToken)tokenStream.elementAt(i);

            if (currentParenDepth == 0)
            {
                if (t.getTerminal() == Terminal.T_LPARENSLASH)
                    inArrayLiteral = true;
                else if (t.getTerminal() == Terminal.T_SLASHRPAREN)
                    inArrayLiteral = false;

                if (!inArrayLiteral)
                {
                    if (t.getTerminal() == Terminal.T_COMMA)
                        openContextComma = true;
                    else if (t.getTerminal() == Terminal.T_EQUALS || t.getTerminal() == Terminal.T_EQGREATERTHAN)
                        openContextEquals = true;
                    else if (endsWithEquals(t))
                        openContextEquals = true;

                    if (justClosedParen && firstParenthetical)
                    {
                        firstParenthetical = false;
                        tokenFollowingParentheticalPos = i;

                        letterFollowsParenthetical =
                            t.getText().trim().length() > 0
                            && Character.isLetter(t.getText().trim().charAt(0));
                    }
                }
            }
            justClosedParen = false;
            if (t.getTerminal() == Terminal.T_LPAREN)
            {
                currentParenDepth++;
            }
            else if (t.getTerminal() == Terminal.T_RPAREN)
            {
                currentParenDepth--;
                justClosedParen = true;
            }

            parenDepth[i] = currentParenDepth;
        }
    }

    /**
     * Sale's algorithm determines whether the first token is a keyword
     * or an identifier as well as the first token after the first
     * closing parenthesis at the top level.
     */
    private void markSalesChanges()
    {
        if (!openContextComma && !openContextEquals)
            retainAsKeyword[firstTokenPos] = true;

        if (openContextEquals && !openContextComma)
            retainAsKeyword[firstTokenPos] = false;

        if (openContextComma)
            retainAsKeyword[firstTokenPos] = true;

        if (letterFollowsParenthetical)
        {
            retainAsKeyword[firstTokenPos] = true;
            retainAsKeyword[tokenFollowingParentheticalPos] =
                ((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_IF && !openContextEquals;
        }
    }

    /**
     * Sale's algorithm doesn't take care of some of our tokens, such as
     * T_INOUT or T_LENEQ.  We define additional rules to identify where
     * these should occur as keywords rather than identifiers.
     */
    private void buildAdditionalRules()
    {
        // R421
        shouldAlwaysBeKeyword(Terminal.T_TRUE);
        shouldAlwaysBeKeyword(Terminal.T_FALSE);

        // R429, R540
        addRule(Terminal.T_NULL,
                new MustBeFollowedBy(Terminal.T_LPAREN, Terminal.T_RPAREN));

        // R430, also R542/R502 (Types in IMPLICIT statement) -- MODIFIED FOR FORTRAN 2003 (see below)
//        addRules(Terminal.T_TYPE,
//                new StmtMustStartWithOneOf(Terminal.T_END, Terminal.T_IMPLICIT),
//                new MustBePrecededByOneOf(Terminal.T_END, Terminal.T_IMPLICIT, Terminal.T_COMMA));

        // R502
        addRule(Terminal.T_PRECISION,
                new MustBePrecededBy(Terminal.T_DOUBLE));

        // R426, R503, R511
        addRules(Terminal.T_POINTER,
                new MustBePartOfTypeDecl(),
                new MustBePrecededBy(Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_PARAMETER);
        applySameRulesTo(Terminal.T_PUBLIC);    // R511
        applySameRulesTo(Terminal.T_PRIVATE);   // R511
        applySameRulesTo(Terminal.T_ALLOCATABLE);
        applySameRulesTo(Terminal.T_DIMENSION);
        applySameRulesTo(Terminal.T_EXTERNAL);
        applySameRulesTo(Terminal.T_INTENT);
        applySameRulesTo(Terminal.T_OPTIONAL);
        applySameRulesTo(Terminal.T_SAVE);
        applySameRulesTo(Terminal.T_TARGET);
        // BEGIN FORTRAN 2008 /////////////////////////////////////////////////////////////////////
        applySameRulesTo(Terminal.T_CODIMENSION); // F08
        applySameRulesTo(Terminal.T_CONTIGUOUS); // F08
        // END FORTRAN 2008 /////////////////////////////////////////////////////////////////////
        // BEGIN FORTRAN 2003 /////////////////////////////////////////////////////////////////////
        applySameRulesTo(Terminal.T_ASYNCHRONOUS);
        applySameRulesTo(Terminal.T_PROTECTED);
        applySameRulesTo(Terminal.T_VALUE);
        applySameRulesTo(Terminal.T_VOLATILE);
        applySameRulesTo(Terminal.T_BIND);

        addRules(Terminal.T_INTRINSIC,
            new MustBePartOfTypeDeclOrStmtMustStartWith(Terminal.T_USE),
            new MustBePrecededBy(Terminal.T_COMMA));

        addRule(Terminal.T_IS,
            new MustBePrecededByOneOf(Terminal.T_TYPE, Terminal.T_CLASS));
        addRule(Terminal.T_ASSOCIATE,
            new MustBePrecededBy(Terminal.T_END));

        addRules(Terminal.T_TYPE,
            new StmtMustStartWithOneOf(Terminal.T_END, Terminal.T_IMPLICIT, Terminal.T_SELECT),
            new MustBePrecededByOneOf(Terminal.T_END, Terminal.T_IMPLICIT, Terminal.T_COMMA, Terminal.T_SELECT));

        addRule(Terminal.T_ABSTRACT,
            new MustBeFollowedBy(Terminal.T_INTERFACE));

        addRules(Terminal.T_INTERFACE,
            new StmtMustStartWithOneOf(Terminal.T_END, Terminal.T_ABSTRACT),
            new MustBePrecededByOneOf(Terminal.T_END, Terminal.T_ABSTRACT));
        // END FORTRAN 2003 ///////////////////////////////////////////////////////////////////////

        // R506, R507
        addRules(Terminal.T_KINDEQ,
                new MustBePartOfTypeDeclOrStmtMustStartWith(Terminal.T_IMPLICIT),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_LENEQ);

        // R512
        addRules(Terminal.T_IN,
                new MustBePartOfTypeDeclOrStmtMustStartWith(Terminal.T_INTENT),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_IN, Terminal.T_OUT));
        applySameRulesTo(Terminal.T_OUT);
        applySameRulesTo(Terminal.T_INOUT);

        // R541
        addRules(Terminal.T_NONE,
                new MustBePrecededBy(Terminal.T_IMPLICIT),
                new MustBeFollowedBy(Terminal.T_EOS));

        // R542/R502 (Types in IMPLICIT statement)
        addRules(Terminal.T_INTEGER,
                 new StmtMustStartWith(Terminal.T_IMPLICIT),
                 new MustBePrecededByOneOf(Terminal.T_IMPLICIT, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_REAL);
        applySameRulesTo(Terminal.T_DOUBLEPRECISION);
        applySameRulesTo(Terminal.T_COMPLEX);
        applySameRulesTo(Terminal.T_LOGICAL);
        applySameRulesTo(Terminal.T_CHARACTER);
        applySameRulesTo(Terminal.T_DOUBLE);

        // R622
        addRules(Terminal.T_STATEQ,
                new StmtMustStartWithOneOf(Terminal.T_ALLOCATE,
                                           Terminal.T_DEALLOCATE),
                new MustBePrecededBy(Terminal.T_COMMA),
                new ParenDepthMustBe(1));

        // R746
        addRules(Terminal.T_WHERE,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededByOneOf(Terminal.T_END, Terminal.T_ELSE));

        // R748
        addRules(Terminal.T_FORALL,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededBy(Terminal.T_END));

        // R803, R804
        addRule(Terminal.T_THEN,
                new StmtMustStartWithOneOf(Terminal.T_IF, Terminal.T_ELSE, Terminal.T_ELSEIF));

        // R804, R806
        addRules(Terminal.T_IF,
                new StmtMustStartWithOneOf(Terminal.T_ELSE, Terminal.T_END),
                new MustBePrecededByOneOf(Terminal.T_ELSE, Terminal.T_END));

        // R809
        addRules(Terminal.T_CASE,
                new StmtMustStartWith(Terminal.T_SELECT),
                new MustBePrecededBy(Terminal.T_SELECT));

        // R811
        addRules(Terminal.T_SELECT,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededBy(Terminal.T_END));

        // R813
        addRule(Terminal.T_DEFAULT,                        // FORTRAN 2003
                new MustBePrecededByOneOf(Terminal.T_CASE, Terminal.T_CLASS));

        // R821
        addRule(Terminal.T_WHILE,
                new MustBePrecededByOneOf(Terminal.T_DO, Terminal.T_COMMA, Terminal.T_ICON));

        // R825
        addRules(Terminal.T_DO,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededBy(Terminal.T_END));

        // R836
        addRules(Terminal.T_TO,
                new StmtMustStartWithOneOf(Terminal.T_GO, Terminal.T_ASSIGN),
                new MustBePrecededByOneOf(Terminal.T_GO, Terminal.T_ICON));

        // R905, R924
        addRules(Terminal.T_FILEEQ,
                new StmtMustStartWithOneOf(Terminal.T_OPEN, Terminal.T_INQUIRE),
                new MustBeInSpecList(),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_ACCESSEQ);
        applySameRulesTo(Terminal.T_FORMEQ);
        applySameRulesTo(Terminal.T_RECLEQ);
        applySameRulesTo(Terminal.T_BLANKEQ);
        applySameRulesTo(Terminal.T_POSITIONEQ);
        applySameRulesTo(Terminal.T_ACTIONEQ);
        applySameRulesTo(Terminal.T_DELIMEQ);
        applySameRulesTo(Terminal.T_PADEQ);

        // R905, R908
        addRules(Terminal.T_STATUSEQ,
                new StmtMustStartWithOneOf(Terminal.T_OPEN, Terminal.T_CLOSE),
                new MustBeInSpecList(),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));

        // R905, R908, R912, R922, R924
        addRules(Terminal.T_UNITEQ,
                // Statement start token is checked in MustBeInSpecList()
                new MustBeInSpecList(),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_ERREQ);
        applySameRulesTo(Terminal.T_IOSTATEQ);

        // BEGIN FORTRAN 2003 /////////////////////////////////////////////////////////////////////
        applySameRulesTo(Terminal.T_STREAMEQ);
        applySameRulesTo(Terminal.T_PENDINGEQ);
        applySameRulesTo(Terminal.T_POSEQ);
        applySameRulesTo(Terminal.T_IDEQ);
        applySameRulesTo(Terminal.T_SIGNEQ);
        applySameRulesTo(Terminal.T_ROUNDEQ);
        applySameRulesTo(Terminal.T_IOMSGEQ);
        applySameRulesTo(Terminal.T_ENCODINGEQ);
        applySameRulesTo(Terminal.T_DECIMALEQ);
        applySameRulesTo(Terminal.T_ASYNCHRONOUSEQ);
        // END FORTRAN 2003 ///////////////////////////////////////////////////////////////////////

        applySameRulesTo(Terminal.T_CONVERTEQ); // Intel Extension

        // R912
        addRules(Terminal.T_FMTEQ,
                new StmtMustStartWithOneOf(Terminal.T_READ, Terminal.T_WRITE),
                new MustBeInSpecList(),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_RECEQ);
        applySameRulesTo(Terminal.T_ENDEQ);
        applySameRulesTo(Terminal.T_EOREQ);
        applySameRulesTo(Terminal.T_NMLEQ);
        applySameRulesTo(Terminal.T_ADVANCEEQ);
        applySameRulesTo(Terminal.T_SIZEEQ);
        applySameRulesTo(Terminal.T_OR);

        // R923, R924
        addRules(Terminal.T_IOLENGTHEQ,
                new StmtMustStartWith(Terminal.T_INQUIRE),
                new MustBeInSpecList(),
                new MustBePrecededByOneOf(Terminal.T_LPAREN, Terminal.T_COMMA));
        applySameRulesTo(Terminal.T_EXISTEQ);
        applySameRulesTo(Terminal.T_OPENEDEQ);
        applySameRulesTo(Terminal.T_NUMBEREQ);
        applySameRulesTo(Terminal.T_NAMEDEQ);
        applySameRulesTo(Terminal.T_NAMEEQ);
        applySameRulesTo(Terminal.T_SEQUENTIALEQ);
        applySameRulesTo(Terminal.T_DIRECTEQ);
        applySameRulesTo(Terminal.T_FORMATTEDEQ);
        applySameRulesTo(Terminal.T_UNFORMATTEDEQ);
        applySameRulesTo(Terminal.T_NEXTRECEQ);
        applySameRulesTo(Terminal.T_READEQ);
        applySameRulesTo(Terminal.T_WRITEEQ);
        applySameRulesTo(Terminal.T_READWRITEEQ);

        // R1103, R1106, R1114, R1204, R1220, R1224
        addRules(Terminal.T_PROGRAM,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededBy(Terminal.T_END));
        applySameRulesTo(Terminal.T_MODULE);
        applySameRulesTo(Terminal.T_BLOCK);
        applySameRulesTo(Terminal.T_BLOCKDATA);
        //applySameRulesTo(Terminal.T_INTERFACE);
        applySameRulesTo(Terminal.T_FUNCTION);
        applySameRulesTo(Terminal.T_SUBROUTINE);
        applySameRulesTo(Terminal.T_SUBMODULE); // Fortran 2008

        // R1114
        addRules(Terminal.T_DATA,
                new StmtMustStartWithOneOf(Terminal.T_BLOCK, Terminal.T_END),
                new MustBePrecededBy(Terminal.T_BLOCK));

        // R1107
        addRules(Terminal.T_ONLY,
                new StmtMustStartWith(Terminal.T_USE),
                new MustBePrecededBy(Terminal.T_COMMA),
                new MustBeFollowedBy(Terminal.T_COLON));

        // 1206
        addRules(Terminal.T_PROCEDURE,
                //new StmtMustStartWith(Terminal.T_MODULE),
                //new MustBePrecededBy(Terminal.T_MODULE));
                new StmtMustStartWithOneOf(Terminal.T_MODULE, Terminal.T_END), // F08
                new MustBePrecededByOneOf(Terminal.T_MODULE, Terminal.T_END)); // F08

        // R1207
        addRule(Terminal.T_OPERATOR,
                new MustBeFollowedBy(Terminal.T_LPAREN, ANY_DEFINED_OPERATOR, Terminal.T_RPAREN));
        addRule(Terminal.T_ASSIGNMENT,
                new MustBeFollowedBy(Terminal.T_LPAREN, Terminal.T_EQUALS, Terminal.T_RPAREN));

        // Function and subroutine declarations are handled separately

        // BEGIN FORTRAN 2008 /////////////////////////////////////////////////////////////////////

        addRules(Terminal.T_CRITICAL,
                new StmtMustStartWith(Terminal.T_END),
                new MustBePrecededBy(Terminal.T_END));

        addRule(Terminal.T_ALL,
            new Or(
                new And(
                    new StmtMustStartWith(Terminal.T_SYNC),
                    new MustBePrecededBy(Terminal.T_SYNC)
                ),
                new And(
                    new StmtMustStartWith(Terminal.T_ALL),
                    new MustBeFollowedBy(Terminal.T_STOP)
                )
            ));

        addRules(Terminal.T_IMAGES,
                new StmtMustStartWith(Terminal.T_SYNC),
                new MustBePrecededBy(Terminal.T_SYNC));
        applySameRulesTo(Terminal.T_MEMORY);

        addRules(Terminal.T_STOP,
            new StmtMustStartWith(Terminal.T_ALL),
            new MustBePrecededBy(Terminal.T_ALL));

        // END FORTRAN 2008 /////////////////////////////////////////////////////////////////////

    }

    /**
     * Apply the rules built in buildAdditionalRules.  These rules are only
     * used to determine when a token should NOT be an identifier.  E.g.,
     * if T_PUBLIC has already been marked as a keyword by Sale's algorithm
     * (because it starts a statement according to R522), the "additional
     * rules" above will not match, but markAdditionalChanges() will leave it
     * alone, so it will still be a keyword.
     *
     * Expects idPos to be set appropriately.
     */
    private void markAdditionalChanges()
    {
        if (varDeclIdPosPair != null)
            retainAsKeyword[firstTokenPos] = true;

        for (int tokenPos = firstTokenPos; tokenPos < tokenStream.size(); tokenPos++)
        {
            Terminal thistokenTerminal = ((IToken)tokenStream.elementAt(tokenPos)).getTerminal();
            if (rules.containsKey(thistokenTerminal))
            {
                LinkedList<Rule> ruleList = (LinkedList<Rule>)rules.get(thistokenTerminal);
                if (allRulesApplyToTokenAtPosition(ruleList, tokenPos))
                    retainAsKeyword[tokenPos] = true;
            }
        }
    }

    /**
     * @param ruleList
     * @param tokenPos
     */
    private boolean allRulesApplyToTokenAtPosition(LinkedList<Rule> ruleList, int tokenPos) {
        ListIterator<Rule> it = ruleList.listIterator();
        while (it.hasNext())
        {
            Rule r = (Rule)it.next();
            if (!r.appliesToTokenAt(tokenPos))
                return false;
        }
        return true;
    }

    /**
     * buildAdditionalRules does not contain rules for subprogram declarations.
     * It's easier to detect whether a statement is a subprogram declaration
     * and make the changes here than to try to build up rules (since the context
     * is a bit more difficult to specify using our Rule classes).
     *
     * Expects idPos to be set appropriately.
     */
    private void markSubprogramChanges()
    {
        if (idPos == -1) return;    // Not a subprogram declaration

        // Everything at paren nesting level 0 is a keyword except the identifier
        for (int i = 0; i < tokenStream.size(); i++)
            if (parenDepth[i] == 0)
                retainAsKeyword[i] = (i != idPos);
    }

    private void markFortran2003Changes()
    {
        if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_END
            && tokenStream.size() > 1
            && ((IToken)tokenStream.elementAt(firstTokenPos+1)).getTerminal() == Terminal.T_SELECT)
        {
            ((IToken)tokenStream.elementAt(firstTokenPos)).setTerminal(Terminal.T_ENDBEFORESELECT);
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_TYPE)
        {
            if (firstTokenPos+1 < tokenStream.size()
                && ((IToken)tokenStream.elementAt(firstTokenPos+1)).getTerminal() == Terminal.T_EQUALS)
            {
                retainAsKeyword[firstTokenPos] = false;
            }
            else
            {
                for (int i = firstTokenPos+1; i < tokenStream.size(); i++)
                {
                    Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                    if (t == Terminal.T_EXTENDS
                        || t == Terminal.T_ABSTRACT
                        || t == Terminal.T_BIND
                        || t == Terminal.T_KIND
                        || t == Terminal.T_LEN
                        || t == Terminal.T_IS)
                    {
                        retainAsKeyword[i] = (parenDepth[i] == 0 && i != idPos);
                    }
                    else if (t == Terminal.T_COLON)
                    {
                        break;
                    }
                }
            }
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_INTEGER)
        {
            for (int i = firstTokenPos+1; i < tokenStream.size()-2; i++)
            {
                Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                Terminal la1 = ((IToken)tokenStream.elementAt(i+1)).getTerminal();
                Terminal la2 = ((IToken)tokenStream.elementAt(i+2)).getTerminal();
                if ((t == Terminal.T_KIND || t == Terminal.T_LEN)
                    && la1 == Terminal.T_COLON
                    && la2 == Terminal.T_COLON)
                {
                    retainAsKeyword[i] = (parenDepth[i] == 0 && i != idPos);
                }
                else if (t == Terminal.T_COLON)
                {
                    break;
                }
            }
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_PROCEDURE)
        {
            retainAsKeyword[firstTokenPos] = true;
            for (int i = firstTokenPos+1; i < tokenStream.size(); i++)
            {
                Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                if (t == Terminal.T_PASS
                    || t == Terminal.T_NOPASS
                    || t == Terminal.T_NON_OVERRIDABLE
                    || t == Terminal.T_DEFERRED)
                {
                    retainAsKeyword[i] = (parenDepth[i] == 0 && i != idPos);
                }
            }
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_ENUM)
        {
            retainAsKeyword[firstTokenPos] = true;
            for (int i = firstTokenPos+1; i < tokenStream.size(); i++)
            {
                Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                if (t == Terminal.T_BIND)
                {
                    retainAsKeyword[i] = (parenDepth[i] == 0 && i != idPos);
                }
            }
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_END)
        {
            for (int i = firstTokenPos+1; i < tokenStream.size(); i++)
            {
                Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                if (t == Terminal.T_ENUM)
                {
                    retainAsKeyword[i] = (parenDepth[i] == 0 && i != idPos);
                }
            }
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_IMPORT)
        {
            retainAsKeyword[firstTokenPos] = true;
        }
        else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_GENERIC
            && tokenStream.size() > 1
            && ((IToken)tokenStream.elementAt(firstTokenPos+1)).getTerminal() == Terminal.T_COLON)
        {
            retainAsKeyword[firstTokenPos] = true;
            for (int i = firstTokenPos+1; i < tokenStream.size(); i++)
            {
                Terminal t = ((IToken)tokenStream.elementAt(i)).getTerminal();
                if (t == Terminal.T_READ || t == Terminal.T_WRITE)
                {
                    if (i+1 < tokenStream.size() && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_LPAREN)
                        retainAsKeyword[i] = true;
                }
            }
        }
    }

    /**
     * Determines whether the current statement is a subprogram declaration and,
     * if so, returns the position of the identifier that is the subprogram name.
     *
     * Subprogram declarations must start with a series of
     * TYPE(...), RECURSIVE, PURE, and ELEMENTAL keywords,
     * followed immediately by FUNCTION or SUBROUTINE and an identifier.
     * A function declaration must then be followed by a left parenthesis,
     * an optional list of parameters, a right parenthesis, and possibly
     * a RESULT(name) clause.  A subroutine does not have a result clause,
     * the parameter list is optional.
     *
     * @return the position of the identifier that names the subprogram (i.e.,
     * its index in tokenStream) if the statement is a subprogram declaration,
     * and -1 otherwise.
     */
    private int getSubprogramDeclIdPos() {
        int idPos = -1, i;

        // Find keywords up through the FUNCTION or SUBROUTINE keyword
        for (i = 0; i < tokenStream.size(); i++)
        {
            // Ignore tokens in parenthesis for this scan
            if (parenDepth[i] == 0)
            {
                IToken pret = (IToken)tokenStream.elementAt(i > 0 ? i-1 : 0);
                IToken t = (IToken)tokenStream.elementAt(i);

                if (!isType(t)
                        && !(t.getTerminal() == Terminal.T_PRECISION && pret.getTerminal() == Terminal.T_DOUBLE)
                        && t.getTerminal() != Terminal.T_RECURSIVE
                        && t.getTerminal() != Terminal.T_PURE
                        && t.getTerminal() != Terminal.T_IMPURE // F08
                        && t.getTerminal() != Terminal.T_MODULE // F08
                        && t.getTerminal() != Terminal.T_ELEMENTAL
                        && t.getTerminal() != Terminal.T_FUNCTION
                        && t.getTerminal() != Terminal.T_SUBROUTINE
                        && t.getTerminal() != Terminal.T_RPAREN)
                    return -1;

                if (t.getTerminal() == Terminal.T_FUNCTION
                        || t.getTerminal() == Terminal.T_SUBROUTINE)
                {
                    idPos = i;
                    break;
                }
            }
        }

        // Make sure we bailed on the FUNCTION or SUBROUTINE keyword
        if (idPos != i) return -1;

        // The identifier is immediately after the FUNCTION or SUBROUTINE keyword
        idPos++; i++;
        if (idPos >= tokenStream.size()) return -1;

        // Now make sure the only thing left at this level is RESULT
        // Fortran 2003 -- or BIND
        for (++i; i < tokenStream.size(); i++)
        {
            // Ignore tokens in parenthesis for this scan
            if (parenDepth[i] == 0)
            {
                IToken t = (IToken)tokenStream.elementAt(i);

                if (t.getTerminal() != Terminal.T_RPAREN
                    && t.getTerminal() != Terminal.T_RESULT
                    && t.getTerminal() != Terminal.T_UNDERSCORE // < These are to handle identifiers containing
                    && t.getTerminal() != Terminal.T_IDENT      // < underscores in fixed form (Bug 305134)
                    && t.getTerminal() != Terminal.T_EOS
                    // Fortran 2003
                    && t.getTerminal() != Terminal.T_BIND
                    )
                    return -1;
            }
        }

        return idPos;
    }

    private class VarDeclIdPosPair
    {
        public int declKeywordsStartAt = -1;
        public int declIdentifierPos = -1;
    }

    private VarDeclIdPosPair getVarDeclIdPos()
    {
        // If this looks like a subprogram declaration, this rule doesn't apply
        // (yeah, this doesn't catch arrays named FUNCTION... somehow I doubt
        // that's really a problem...)
        if (idPos != -1) return null;

        // Start i at the first token of the statement
        int i = firstTokenPos;
        if (!isType((IToken)tokenStream.elementAt(i))) return null;

        // Declaration keywords start just after the type name, unless
        // the first four tokens are TYPE(name) ...
        if (i+3 < tokenStream.size()
                        && ((IToken)tokenStream.elementAt(i)).getTerminal() == Terminal.T_TYPE
                        && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_LPAREN
                        && ((IToken)tokenStream.elementAt(i+3)).getTerminal() == Terminal.T_RPAREN)
            i += 4;
        // ...or the first four tokens are DOUBLE PRECISION * <kind>
        else if (i+3 < tokenStream.size()
                        && ((IToken)tokenStream.elementAt(i)).getTerminal() == Terminal.T_DOUBLE
                        && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_PRECISION
                        && ((IToken)tokenStream.elementAt(i+2)).getTerminal() == Terminal.T_ASTERISK
                        && ((IToken)tokenStream.elementAt(i+3)).getTerminal() == Terminal.T_ICON)
            i += 4;
        // ...or the first three tokens are <type> * <kind>
        else if (i+2 < tokenStream.size()
                        && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_ASTERISK
                        && ((IToken)tokenStream.elementAt(i+2)).getTerminal() == Terminal.T_ICON)
            i += 3;
        // ...or the first three tokens are <type> * (, as in character*(*)
        else if (i+2 < tokenStream.size()
                        && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_ASTERISK
                        && ((IToken)tokenStream.elementAt(i+2)).getTerminal() == Terminal.T_LPAREN)
            i += 3;
        // ...or the first two tokens are DOUBLE PRECISION
        else if (i+1 < tokenStream.size()
                        && ((IToken)tokenStream.elementAt(i)).getTerminal() == Terminal.T_DOUBLE
                        && ((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_PRECISION)
            i += 2;
        else
            ++i;

        int declKeywordsStartAt = i;
        int declIdentifierPos = -1;

        boolean lastWasComma = false;

        // Expect ", keyword, keyword identifier" or
        // Expect ", keyword, keyword :: identifier" with possible parenthesized
        // expressions after keywords
        for ( ; i < tokenStream.size() && declIdentifierPos == -1; i++)
        {
            IToken t = ((IToken)tokenStream.elementAt(i));

            // Skip parenthesized parts...
            if (parenDepth[i] > 0 || t.getTerminal() == Terminal.T_RPAREN)
                continue;

            // Is this a comma?  Note that and keep searching...
            else if (t.getTerminal() == Terminal.T_COMMA)
            {
                lastWasComma = true;
                continue;
            }

            // Did we just follow a comma?  Keep searching...
            else if (lastWasComma)
            {
                lastWasComma = false;
                continue;
            }

            // We didn't follow a comma... is this ::?
            else if (t.getTerminal() == Terminal.T_COLON)
            {
                if (i+2 >= tokenStream.size()) return null;
                if (((IToken)tokenStream.elementAt(i+1)).getTerminal() == Terminal.T_COLON)
                {
                    declIdentifierPos = i+2;
                    break;
                }
                else
                    return null;
            }

            // We didn't follow a comma or :: and we're at paren level 0:
            // This is the identifier!
            else
            {
                declIdentifierPos = i;
                break;
            }
        }

        VarDeclIdPosPair ret = new VarDeclIdPosPair();
        ret.declKeywordsStartAt = declKeywordsStartAt;
        ret.declIdentifierPos = declIdentifierPos;
        return ret;
    }

    /* ----- RULES ----------------------------------------------------------*/

    private LinkedList<Rule> ruleList;

    private void shouldAlwaysBeKeyword(Terminal tokenTerminal)
    {
        ruleList = new LinkedList<Rule>();
        applyRulesTo(tokenTerminal);
    }

    private void addRule(Terminal tokenTerminal, Rule rule)
    {
        ruleList = new LinkedList<Rule>();
        ruleList.add(rule);
        applyRulesTo(tokenTerminal);
    }

    private void addRules(Terminal tokenTerminal, Rule rule1, Rule rule2)
    {
        ruleList = new LinkedList<Rule>();
        ruleList.add(rule1);
        ruleList.add(rule2);
        applyRulesTo(tokenTerminal);
    }

    private void addRules(Terminal tokenTerminal, Rule rule1, Rule rule2, Rule rule3)
    {
        ruleList = new LinkedList<Rule>();
        ruleList.add(rule1);
        ruleList.add(rule2);
        ruleList.add(rule3);
        applyRulesTo(tokenTerminal);
    }

    private void applySameRulesTo(Terminal tokenTerminal)
    {
        applyRulesTo(tokenTerminal);
    }

    private void applyRulesTo(Terminal tokenTerminal)
    {
        if (rules.containsKey(tokenTerminal))
            throw new Error("Multiple rule lists specified for token " + tokenTerminal);
        rules.put(tokenTerminal, ruleList);
    }

    /* ----- RULES ----------------------------------------------------------*/

    private abstract class Rule
    {
        public abstract boolean appliesToTokenAt(int tokenPos);
    }

    private final class Or extends Rule
    {
        private Rule[] rules;
        
        public Or(Rule rule1, Rule rule2)
        {
            rules = new Rule[] { rule1, rule2 };
        }
        
        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            for (int i = 0; i < rules.length; i++)
                if (rules[i].appliesToTokenAt(tokenPos))
                    return true;
            return false;
        }
    }

    private final class And extends Rule
    {
        private Rule[] rules;
        
        public And(Rule rule1, Rule rule2)
        {
            rules = new Rule[] { rule1, rule2 };
        }
        
        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            for (int i = 0; i < rules.length; i++)
                if (!rules[i].appliesToTokenAt(tokenPos))
                    return false;
            return true;
        }
    }

    /**
     * The statement must be a type declaration statement, and the token
     * under investigation must appear before the name of the variable being
     * declared (i.e., it must be part of the type declaration and not part
     * of the initialization expression, if any).
     *
     * Expects varDeclIdPosPair and idPos to be set.
     */
    private final class MustBePartOfTypeDecl extends Rule
    {
        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            int declKeywordsStartAt = -1;
            int declIdentifierPos = -1;

            if (varDeclIdPosPair != null)
            {
                // Part of a variable declaration
                declKeywordsStartAt = varDeclIdPosPair.declKeywordsStartAt;
                declIdentifierPos = varDeclIdPosPair.declIdentifierPos;
            }
            else if (idPos > 0)
            {
                // Part of the return type in a function declaration
                declKeywordsStartAt = 1;
                declIdentifierPos = idPos;
            }
            // Fortran 2003
            else if (((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_PROCEDURE
                || ((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_GENERIC
                || ((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_CLASS)
            {
                // Part of the return type in a function declaration
                declKeywordsStartAt = 1;
                declIdentifierPos = tokenStream.size()-1; // TODO: Not quite right
            }
            else return false;

            // Return whether this token is somewhere in the declaration keywords
            // and at paren depth 0 or 1
            return tokenPos >= declKeywordsStartAt
                && tokenPos < declIdentifierPos
                && parenDepth[tokenPos] <= 1;
        }
    }

    /**
     * Either MustBePartOfTypeDecl or StmtMustStartWith should hold
     *
     * Expects varDeclIdPosPair and idPos to be set.
     */
    private final class MustBePartOfTypeDeclOrStmtMustStartWith extends Rule
    {
        private Terminal tokenTerminal = ALWAYS_RETURN_TRUE;

        public MustBePartOfTypeDeclOrStmtMustStartWith(Terminal tokenTerminal)
        {
            this.tokenTerminal = tokenTerminal;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return (new MustBePartOfTypeDecl().appliesToTokenAt(tokenPos))
                || (new StmtMustStartWith(tokenTerminal).appliesToTokenAt(tokenPos));
        }
    }

    /**
     * A particular token or sequence of tokens must appear immediately before
     * the token being investigated.
     */
    private final class MustBePrecededBy extends Rule
    {
        Terminal secondTokenPriorId = ALWAYS_RETURN_TRUE;
        Terminal firstTokenPriorId = ALWAYS_RETURN_TRUE;

        public MustBePrecededBy(Terminal tokenTerminal)
        {
            firstTokenPriorId = tokenTerminal;
        }

        @SuppressWarnings("unused")
        public MustBePrecededBy(Terminal tokenTerminal1, Terminal tokenTerminal2)
        {
            secondTokenPriorId = tokenTerminal1;
            firstTokenPriorId = tokenTerminal2;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return matchToken(tokenPos-2, secondTokenPriorId)
                && matchToken(tokenPos-1, firstTokenPriorId);
        }
    }

    /**
     * The token immediately before the token being investigated must be one
     * of a given set.
     */
    private final class MustBePrecededByOneOf extends Rule
    {
        Terminal possibility1 = ALWAYS_RETURN_FALSE,
            possibility2 = ALWAYS_RETURN_FALSE,
            possibility3 = ALWAYS_RETURN_FALSE,
            possibility4 = ALWAYS_RETURN_FALSE;

        public MustBePrecededByOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2)
        {
            possibility1 = tokenTerminal1;
            possibility2 = tokenTerminal2;
        }

        public MustBePrecededByOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2, Terminal tokenTerminal3)
        {
            possibility1 = tokenTerminal1;
            possibility2 = tokenTerminal2;
            possibility3 = tokenTerminal3;
        }

        public MustBePrecededByOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2, Terminal tokenTerminal3, Terminal tokenTerminal4)
        {
            possibility1 = tokenTerminal1;
            possibility2 = tokenTerminal2;
            possibility3 = tokenTerminal3;
            possibility4 = tokenTerminal4;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return matchToken(tokenPos-1, possibility1)
                || matchToken(tokenPos-1, possibility2)
                || matchToken(tokenPos-1, possibility3)
                || matchToken(tokenPos-1, possibility4);
        }
    }

    /**
     * Wildcard referring to any token.
     *
     * A particular token or sequence of tokens must appear immediately after
     * the token being investigated.  The wildcard ANY_DEFINED_OPERATOR can
     * also be used.
     */
    private final Terminal ANY_TOKEN = new Terminal("any token");

    /**
     * Wildcard referring to any defined operator (obviously)
     */
    private final Terminal ANY_DEFINED_OPERATOR = new Terminal("any defined operator");

    /**
     * This is used when we need a reasonable default in some places...
     */
    private final Terminal ALWAYS_RETURN_TRUE = new Terminal("always return true");

    /**
     * This is used when we need a reasonable default in some places...
     */
    private final Terminal ALWAYS_RETURN_FALSE = new Terminal("always return false");

    private final class MustBeFollowedBy extends Rule
    {
        Terminal firstTokenAfterId = ALWAYS_RETURN_TRUE;
        Terminal secondTokenAfterId = ALWAYS_RETURN_TRUE;
        Terminal thirdTokenAfterId = ALWAYS_RETURN_TRUE;

        public MustBeFollowedBy(Terminal tokenTerminal)
        {
            firstTokenAfterId = tokenTerminal;
        }

        public MustBeFollowedBy(Terminal tokenTerminal1, Terminal tokenTerminal2)
        {
            firstTokenAfterId = tokenTerminal1;
            secondTokenAfterId = tokenTerminal2;
        }

        public MustBeFollowedBy(Terminal tokenTerminal1, Terminal tokenTerminal2, Terminal tokenTerminal3)
        {
            firstTokenAfterId = tokenTerminal1;
            secondTokenAfterId = tokenTerminal2;
            thirdTokenAfterId = tokenTerminal3;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return matchToken(tokenPos+1, firstTokenAfterId)
                && matchToken(tokenPos+2, secondTokenAfterId)
                && matchToken(tokenPos+3, thirdTokenAfterId);
        }
    }

    /**
     * The statement must start with the given token, and that token must have
     * been identified by Sale's algorithm as being a keyword (not an
     * identifier).
     */
    private final class StmtMustStartWith extends Rule
    {
        private Terminal tokenTerminal = ALWAYS_RETURN_TRUE;

        public StmtMustStartWith(Terminal tokenTerminal)
        {
            this.tokenTerminal = tokenTerminal;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return (matchToken(firstTokenPos, tokenTerminal)
                    || matchToken(tokenFollowingParentheticalPos, tokenTerminal))
                && retainAsKeyword[firstTokenPos];
        }
    }

    /**
     * The statement must start a token from a given set, and that token must
     * have been identified by Sale's algorithm as being a keyword (not an
     * identifier).
     */
    private final class StmtMustStartWithOneOf extends Rule
    {
        private Terminal[] possibilities;

        public StmtMustStartWithOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2)
        {
            possibilities = new Terminal[2];
            possibilities[0] = tokenTerminal1;
            possibilities[1] = tokenTerminal2;
        }

        public StmtMustStartWithOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2, Terminal tokenTerminal3)
        {
            possibilities = new Terminal[3];
            possibilities[0] = tokenTerminal1;
            possibilities[1] = tokenTerminal2;
            possibilities[2] = tokenTerminal3;
        }

        @SuppressWarnings("unused")
        public StmtMustStartWithOneOf(Terminal tokenTerminal1, Terminal tokenTerminal2, Terminal tokenTerminal3, Terminal tokenTerminal4)
        {
            possibilities = new Terminal[4];
            possibilities[0] = tokenTerminal1;
            possibilities[1] = tokenTerminal2;
            possibilities[2] = tokenTerminal3;
            possibilities[3] = tokenTerminal4;
        }

        @SuppressWarnings("unused")
        public StmtMustStartWithOneOf(Terminal tokenTerminal1,
                                      Terminal tokenTerminal2,
                                      Terminal tokenTerminal3,
                                      Terminal tokenTerminal4,
                                      Terminal tokenTerminal5,
                                      Terminal tokenTerminal6,
                                      Terminal tokenTerminal7,
                                      Terminal tokenTerminal8)
        {
            possibilities = new Terminal[8];
            possibilities[0] = tokenTerminal1;
            possibilities[1] = tokenTerminal2;
            possibilities[2] = tokenTerminal3;
            possibilities[3] = tokenTerminal4;
            possibilities[4] = tokenTerminal5;
            possibilities[5] = tokenTerminal6;
            possibilities[6] = tokenTerminal7;
            possibilities[7] = tokenTerminal8;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            if (!retainAsKeyword[firstTokenPos]) return false;

            boolean match = false;
            for (int i = 0; i < possibilities.length && !match; i++)
                match = match
                        || matchToken(firstTokenPos, possibilities[i]) && retainAsKeyword[firstTokenPos]
                        || matchToken(tokenFollowingParentheticalPos, possibilities[i]) && retainAsKeyword[tokenFollowingParentheticalPos];
            return match;
        }
    }

    /**
     * The token must be nested in parentheses of a given depth.
     * In the string a(b(c)(d)e)f, a and f are at level 0, b and
     * e are at level 1, and c and d are at level 2.
     */
    private final class ParenDepthMustBe extends Rule
    {
        private int depth = -1;

        public ParenDepthMustBe(int depth)
        {
            this.depth = depth;
        }

        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            return parenDepth[tokenPos] == depth;
        }
    }
    
    protected void modifyPreprocessorDirective(IToken t)
    {
        
    }

    /**
     * The token must appear in the spec list for an OPEN, CLOSE, READ,
     * WRITE, BACKSPACE, END FILE, REWIND, or INQUIRE statement.  I.e., the statement
     * must begin as such, the next token must be a left parenthesis, and the
     * token being investigated must appear before the corresponding closing
     * parenthesis.
     */
    private final class MustBeInSpecList extends Rule
    {
        @Override public boolean appliesToTokenAt(int tokenPos)
        {
            IToken t = (IToken)tokenStream.elementAt(firstTokenPos);
            int specListStartsAt = firstTokenPos + 1;

            // Check statement start token
            if (t.getTerminal() != Terminal.T_OPEN
                && t.getTerminal() != Terminal.T_CLOSE
                && t.getTerminal() != Terminal.T_READ
                && t.getTerminal() != Terminal.T_WRITE
                && t.getTerminal() != Terminal.T_BACKSPACE
                && t.getTerminal() != Terminal.T_ENDFILE
                && t.getTerminal() != Terminal.T_END
                && t.getTerminal() != Terminal.T_REWIND
                && t.getTerminal() != Terminal.T_INQUIRE)
            {
                if (t.getTerminal() != Terminal.T_IF || tokenFollowingParentheticalPos < 0)
                    return false;

                t = (IToken)tokenStream.elementAt(tokenFollowingParentheticalPos);

                if (t.getTerminal() != Terminal.T_OPEN
                        && t.getTerminal() != Terminal.T_CLOSE
                        && t.getTerminal() != Terminal.T_READ
                        && t.getTerminal() != Terminal.T_WRITE
                        && t.getTerminal() != Terminal.T_BACKSPACE
                        && t.getTerminal() != Terminal.T_ENDFILE
                        && t.getTerminal() != Terminal.T_END
                        && t.getTerminal() != Terminal.T_REWIND
                        && t.getTerminal() != Terminal.T_INQUIRE)
                    return false;

                specListStartsAt = tokenFollowingParentheticalPos + 1;
            }

            // If first token is END, must be followed by FILE
            if (t.getTerminal() == Terminal.T_END)
            {
                t = (firstTokenPos+1 >= tokenStream.size()
                    ? null
                    : (IToken)tokenStream.elementAt(firstTokenPos+1));
                if (t == null)
                    return false;
                else if (t.getTerminal() != Terminal.T_FILE)
                    return false;
                else
                    specListStartsAt++;
            }

            // Match a left parenthesis
            t = (specListStartsAt >= tokenStream.size()
                    ? null
                    : (IToken)tokenStream.elementAt(specListStartsAt));
            if (t == null || t.getTerminal() != Terminal.T_LPAREN) return false;

            int specListEndsAt = specListStartsAt;

            // Skip stuff in parenthesis
            for (specListEndsAt++;
                specListEndsAt < tokenStream.size() && parenDepth[specListEndsAt] > 0;
                specListEndsAt++)
                ;

            // Match a right parenthesis
            t = (specListEndsAt >= tokenStream.size()
                    ? null
                    : (IToken)tokenStream.elementAt(specListEndsAt));
            if (t == null || t.getTerminal() != Terminal.T_RPAREN) return false;

            return tokenPos >= specListStartsAt
                && tokenPos <= specListEndsAt
                && parenDepth[tokenPos] == 1;
        }
    }

    /**
     * Determines whether a token in a given position represents a given terminal
     * or falls into a certain terminal class.
     * @param tokenPos the position of the token to match (in tokenStream)
     * @param targetTerminal the Terminal the token should have, -1 to always return
     * true, ANY_TOKEN to match any token at all, and ANY_DEFINED_OPERATOR
     * to match any Fortran defined operator (.and., .eq., .whatever.).
     * @return true iff the token in the given position matches the target ID
     */
    private boolean matchToken(int tokenPos, Terminal targetTerminal)
    {
        if (targetTerminal == ALWAYS_RETURN_TRUE) return true;

        if (targetTerminal == ALWAYS_RETURN_FALSE) return false;

        if (tokenPos < 0 || tokenPos >= tokenStream.size()) return false;

        IToken actualToken = (tokenPos >= 0 && tokenPos <= tokenStream.size()
                ? (IToken)tokenStream.elementAt(tokenPos)
                : null);

        if (targetTerminal == ANY_TOKEN)
            return actualToken != null;
        else if (targetTerminal == ANY_DEFINED_OPERATOR)
            return actualToken != null
            &&(
                    ( actualToken.getText().startsWith(".")
                            && actualToken.getText().endsWith(".") )

                    ||(
                            actualToken.getTerminal() == Terminal.T_SLASHSLASH||
                            actualToken.getTerminal() == Terminal.T_ASTERISK    ||
                            actualToken.getTerminal() == Terminal.T_POW     ||
                            actualToken.getTerminal() == Terminal.T_SLASH       ||
                            actualToken.getTerminal() == Terminal.T_PLUS        ||
                            actualToken.getTerminal() == Terminal.T_MINUS       ||
                            actualToken.getTerminal() == Terminal.T_EQ      ||
                            actualToken.getTerminal() == Terminal.T_LE      ||
                            actualToken.getTerminal() == Terminal.T_LT      ||
                            actualToken.getTerminal() == Terminal.T_NE      ||
                            actualToken.getTerminal() == Terminal.T_GE      ||
                            actualToken.getTerminal() == Terminal.T_GT      ||
                            actualToken.getTerminal() == Terminal.T_EQEQ        ||
                            actualToken.getTerminal() == Terminal.T_SLASHEQ ||
                            actualToken.getTerminal() == Terminal.T_LESSTHAN    ||
                            actualToken.getTerminal() == Terminal.T_LESSTHANEQ||
                            actualToken.getTerminal() == Terminal.T_GREATERTHAN||
                            actualToken.getTerminal() == Terminal.T_GREATERTHANEQ||
                            actualToken.getTerminal() == Terminal.T_NOT     ||
                            actualToken.getTerminal() == Terminal.T_AND     ||
                            actualToken.getTerminal() == Terminal.T_OR      ||
                            actualToken.getTerminal() == Terminal.T_EQV     ||
                            actualToken.getTerminal() == Terminal.T_NEQV
                    )
            );

        else
            return actualToken.getTerminal() == targetTerminal;
    }

    /* ----- APPLY CHANGES --------------------------------------------------*/

    /**
     * Iterate through the tokens in the statement.  If a token can be
     * an identifier, make it one UNLESS the corresponding entry in
     * retainAsKeyword is set.
     */
    private void applyChanges()
    {
        Vector<Integer> identifiersContainingEqualSigns = new Vector<Integer>();
        Vector<Integer> identifiersStarred = new Vector<Integer>();
        for (int i = 0; i < tokenStream.size(); i++)
        {
            IToken t = (IToken)tokenStream.elementAt(i);
            if (canBeIdentifier(t) && !retainAsKeyword[i])
            {
                t.setTerminal(Terminal.T_IDENT);
                if (endsWithEquals(t))
                    identifiersContainingEqualSigns.add(new Integer(i));
            }
            else if (isStarredType(t) && !retainAsKeyword[i])
            {
                t.setTerminal(Terminal.T_IDENT);
                identifiersStarred.add(new Integer(i));
            }

        }

        // If, say, "len=" was changed into an identifier, split it into
        // two tokens: the identifier "len" and the equals token
        for (int j = 0; j < identifiersContainingEqualSigns.size(); j++)
        {
            // i is the position of the "xyz=" token in tokenStream
            int i = ((Integer)identifiersContainingEqualSigns.elementAt(j)).intValue();

            // t is the "xyz=" token: split it
            IToken t = (IToken)tokenStream.elementAt(i+j);
            IToken afterT = (i < tokenStream.size()-1 ? (IToken)tokenStream.elementAt(i+j+1) : null);
            String textWithoutEquals = t.getText().substring(0, t.getText().length()-1);
            String tokenText = textWithoutEquals.trim();
            String whiteAfter = textWithoutEquals.substring(tokenText.length());
            //int numCharsRemoved = t.getTokenText().length() - textWithoutEquals.length();

            //if (afterT != null && afterT.getTerminal() == Terminal.T_EQUALS && afterT.getStartCol() == t.getEndCol()+1)
            if (afterT != null && afterT.getTerminal() == Terminal.T_EQUALS && t.getWhiteAfter().equals("") && afterT.getWhiteBefore().equals(""))
            {
                // split "xyz=" "=" into "xyz" "=="
                afterT.setTerminal(Terminal.T_EQEQ);
                //afterT.setStartCol(afterT.getStartCol()-1);
                //afterT.setOffset(afterT.getOffset()-1);
                //afterT.setLength(afterT.getLength()+1);
                afterT.setText("==");

                t.setText(tokenText);
                t.setWhiteAfter(whiteAfter);
                //t.setEndCol(t.getEndCol() - numCharsRemoved);
                //t.setLength(t.getLength() - numCharsRemoved);
            }
            else
            {
                // split "xyz=" into "xyz" "="
                IToken eq = new Token(Terminal.T_EQUALS, "=");
                //eq.setTerminal(Terminal.T_EQUALS);
                //eq.setFilename(t.getFilename());
                //eq.setStartLine(t.getEndLine());
                //eq.setStartCol(t.getEndCol());
                //eq.setEndLine(t.getEndLine());
                //eq.setEndCol(t.getEndCol());
                //eq.setOffset(t.getOffset() + t.getLength() - 1);
                //eq.setLength(1);
                //eq.setText("=");

                t.setText(tokenText);
                t.setWhiteAfter(whiteAfter);
                //t.setEndCol(t.getEndCol() - numCharsRemoved);
                //t.setLength(t.getLength() - numCharsRemoved);

                lengths.setElementAt(new Integer(tokenText.length()), i+j);

                // i+j since we might have inserted some equal signs earlier, 1 to insert after
                tokenStream.insertElementAt(eq, i+j+1);
                lineNumbers.insertElementAt(lineNumbers.get(i+j), i+j+1);
                colNumbers.insertElementAt(new Integer(((Integer)colNumbers.get(i+j)).intValue()+textWithoutEquals.length()), i+j+1);
                files.insertElementAt((FileOrIFile)files.get(i+j), i+j+1);
                fileOffsets.insertElementAt(new Integer(((Integer)fileOffsets.get(i+j)).intValue()+textWithoutEquals.length()), i+j+1);
                streamOffsets.insertElementAt(new Integer(((Integer)streamOffsets.get(i+j)).intValue()+textWithoutEquals.length()), i+j+1);
                lengths.insertElementAt(new Integer(1), i+j+1);
            }
            modifyPreprocessorDirective(t);
        }

        
        // If, say, "integer*3" was changed into an identifier, split it into
        // three tokens: the identifier "integer", the asterisk, and the number 3
        for (int j = 0; j < identifiersStarred.size(); j++)
        {
            // i is the position of the "integer*3" token in tokenStream
            int i = ((Integer)identifiersStarred.elementAt(j)).intValue();

            // t is the "integer*3" token: split it
            IToken t = (IToken)tokenStream.elementAt(i);
            int starPos = t.getText().indexOf("*");
            String textBeforeStar = t.getText().substring(0, starPos);
            String textAfterStar = t.getText().substring(starPos+1, t.getText().length());
            //int numCharsRemoved = t.getTokenText().length() - textAfterStar.length() - 1;

            // split "integer*3" into "integer" "*" "3"
            IToken star = new Token(Terminal.T_ASTERISK, "*");
            //star.setTerminal(Terminal.T_ASTERISK);
            //star.setFilename(t.getFilename());
            //star.setStartLine(t.getEndLine());
            //star.setEndLine(t.getEndLine());
            //star.setStartCol(t.getEndCol() - textAfterStar.length());
            //star.setEndCol(t.getEndCol() - textAfterStar.length());
            //star.setOffset(t.getOffset() + textBeforeStar.length());
            //star.setLength(1);
            //star.setText("*");

            IToken num = new Token(Terminal.T_ICON, textAfterStar);
            //num.setTerminal(Terminal.T_ICON);
            //num.setFilename(t.getFilename());
            //num.setStartLine(star.getEndLine());
            //num.setStartCol(star.getEndCol() + 1);
            //num.setEndLine(num.getStartLine());
            //num.setEndCol(num.getStartCol() + textAfterStar.length() - 1);
            //num.setOffset(star.getOffset() + 1);
            //num.setLength(textAfterStar.length());
            //num.setText(textAfterStar);

            t.setText(textBeforeStar);
            //t.setEndCol(t.getEndCol() - numCharsRemoved);
            //t.setLength(t.getLength() - numCharsRemoved);

            lengths.setElementAt(new Integer(textBeforeStar.length()), i+(2*j));

            // i+(2*j) since we might have inserted some asterisks and integers earlier, 1 to insert after
            tokenStream.insertElementAt(star, i+(2*j)+1);
            lineNumbers.insertElementAt(lineNumbers.get(i+(2*j)), i+(2*j)+1);
            colNumbers.insertElementAt(new Integer(((Integer)colNumbers.get(i+(2*j))).intValue()+textBeforeStar.length()), i+(2*j)+1);
            files.insertElementAt((FileOrIFile)files.get(i+(2*j)), i+(2*j)+1);
            fileOffsets.insertElementAt(new Integer(((Integer)fileOffsets.get(i+(2*j))).intValue()+textBeforeStar.length()), i+(2*j)+1);
            streamOffsets.insertElementAt(new Integer(((Integer)streamOffsets.get(i+(2*j))).intValue()+textBeforeStar.length()), i+(2*j)+1);
            lengths.insertElementAt(new Integer(1), i+(2*j)+1);

            tokenStream.insertElementAt(num, i+(2*j)+2);
            lineNumbers.insertElementAt(lineNumbers.get(i+(2*j)+1), i+(2*j)+2);
            colNumbers.insertElementAt(new Integer(((Integer)colNumbers.get(i+(2*j)+1)).intValue()+textBeforeStar.length()+1), i+(2*j)+2);
            files.insertElementAt((FileOrIFile)files.get(i+(2*j)), i+(2*j)+1);
            fileOffsets.insertElementAt(new Integer(((Integer)fileOffsets.get(i+(2*j)+1)).intValue()+textBeforeStar.length()+1), i+(2*j)+2);
            streamOffsets.insertElementAt(new Integer(((Integer)streamOffsets.get(i+(2*j)+1)).intValue()+textBeforeStar.length()+1), i+(2*j)+2);
            lengths.insertElementAt(new Integer(1), i+(2*j)+2);
        }
    }

    /* ----- UTILITY FUNCTIONS ----------------------------------------------*/

    private Pattern idPattern = Pattern.compile("[A-Za-z][A-Za-z0-9_]*([ \t]*=)?");
    private Pattern starredTypePattern = Pattern.compile("[A-Za-z]+\\*[0-9]+");
    private Pattern iconPattern = Pattern.compile("[0-9]+|[0-9]+(E|e)[0-9]+\\.[0-9]+");

    /**
     * @param t
     * @return true iff the given token can be an identifier
     */
    private boolean canBeIdentifier(IToken t)
    {
        return idPattern.matcher(t.getText()).matches();
    }

    /**
     * @param t
     * @return true iff the given token is a starred type
     */
    private boolean isStarredType(IToken t)
    {
        return starredTypePattern.matcher(t.getText()).matches();
    }

    /**
     * @param t
     * @return true iff the given token is a keyword followed by an equal sign
     */
    private boolean endsWithEquals(IToken t)
    {
        if (t.getText().length() < 2) return false;
        if (!canBeIdentifier(t)) return false;

        char lastChar = t.getText().charAt(t.getText().length()-1);
        if (lastChar != '=') return false;

        char nextToLastChar = t.getText().charAt(t.getText().length()-2);
        return Character.isLetter(nextToLastChar) || Character.isWhitespace(nextToLastChar);
    }

//    /**
//     * @param id
//     * @return true iff the given token ID corresponds to an "end" token
//     */
//    private boolean isEndToken(int id)
//    {
//        return id == Terminal.T_ENDMODULE
//          || id == Terminal.T_ENDFILE
//          || id == Terminal.T_ENDSUBROUTINE
//          || id == Terminal.T_ENDFUNCTION
//          || id == Terminal.T_END
//          || id == Terminal.T_ENDIF
//          || id == Terminal.T_ENDFORALL
//          || id == Terminal.T_ENDEQ
//          || id == Terminal.T_ENDBLOCKDATA
//          || id == Terminal.T_ENDTYPE
//          || id == Terminal.T_ENDDO
//          || id == Terminal.T_ENDPROGRAM
//          || id == Terminal.T_ENDINTERFACE
//          || id == Terminal.T_ENDBLOCK
//          || id == Terminal.T_ENDWHERE
//          || id == Terminal.T_ENDSELECT;
//    }

    /**
     * @return true iff the token can represent a type
     */
    private boolean isType(IToken type)
    {
        return type.getTerminal() == Terminal.T_CHARACTER
            || type.getTerminal() == Terminal.T_COMPLEX
            || type.getTerminal() == Terminal.T_DOUBLE
            || type.getTerminal() == Terminal.T_DOUBLEPRECISION
            || type.getTerminal() == Terminal.T_INTEGER
            || type.getTerminal() == Terminal.T_LOGICAL
            || type.getTerminal() == Terminal.T_REAL
            || type.getTerminal() == Terminal.T_TYPE
        ;
    }

    /**
     * @return true iff the current statement begins with a label
     * (and, thus, the first "real" token is in the second position)
     */
    private boolean stmtBeginsWithLabel()
    {
        // Is the first token an integer constant?
        return iconPattern.matcher(((IToken)tokenStream.elementAt(0)).getText()).matches();
    }

    /**
     * @return true iff the tokens starting at firstTokenPos
     * are T_IDENT T_COLON.
     *
     * R740, R748, and several other productions allow an optional label,
     * a name, and a colon to precede the statement.
     */
    private boolean stmtBeginsWithNameColon()
    {
        if (firstTokenPos+1 >= tokenStream.size()) return false;

        return ((IToken)tokenStream.elementAt(firstTokenPos)).getTerminal() == Terminal.T_IDENT
            && ((IToken)tokenStream.elementAt(firstTokenPos+1)).getTerminal() == Terminal.T_COLON;
    }

    public void printCurrentStatementOn(PrintStream s)
    {
      for (int i = 0; i < tokenStream.size(); i++)
      {
        IToken t = (IToken)tokenStream.elementAt(i);
        s.print(t.getText().replaceAll("\\n", "<end of line>"));
        s.print(" ");
      }
      s.println();
    }

    public void printCurrentStatementTokensOn(PrintStream s)
    {
      for (int i = 0; i < tokenStream.size(); i++)
      {
        IToken t = (IToken)tokenStream.elementAt(i);
        s.print(t.getTerminal());
        s.print("(" + t.getText().replaceAll("\\n", "\\\\n") + ")");
        //THESE DON'T WORK: The stream gets bigger than the arrays if tokens
        //                  are split, so these will be inaccurate and may
        //                  cause array bounds exceptions
        //s.print(", " + parenDepth[i]);
        //s.print("," + retainAsKeyword[i]);
        s.print("   ");
      }
      s.println();
    }

    public String getFilename()
    {
        return yylex.getFilename();
    }

    public int getLastTokenLine()
    {
        return lastTokenLine;
    }

    public int getLastTokenCol()
    {
        return lastTokenCol;
    }

    public FileOrIFile getLastTokenFile()
    {
        return lastTokenFile;
    }

    public int getLastTokenFileOffset()
    {
        return lastTokenFileOffset;
    }

    public int getLastTokenStreamOffset()
    {
        return lastTokenStreamOffset;
    }

    public int getLastTokenLength()
    {
        return lastTokenLength;
    }

    public void setTokenAsCurrent(IToken token)
    {
        throw new UnsupportedOperationException();
    }
}
