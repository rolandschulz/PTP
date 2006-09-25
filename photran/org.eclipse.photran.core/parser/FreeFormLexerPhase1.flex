/*
 * First phase of free form lexical analysis for Fortran 95 parser
 *
 * FreeFormLexerPhase1 acts as a "token stream" feeding FreeFormLexerPhase2
 * (See FreeFormLexerPhase2.java and f95t.bnf)
 *
 * @author Jeffrey Overbey
 * 
 * @see FreeFormLexerPhase2
 * @see Parser
 *
 * NOTE: Get rid of (space out) "yybegin(YYINITIAL);" in the lines
 * for any tokens that can appear in an IMPLICIT statement
 * (It was also omitted in the lines for T_SLASH so that
 * INTERFACE OPERATOR (/) would tokenize correctly.)
 */
 
package org.eclipse.photran.internal.core.lexer;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.photran.internal.core.parser.Terminal;

%%
 
%class FreeFormLexerPhase1
%throws Exception
%char
%line
%column
%unicode
%implements ILexer
%ignorecase
%type Token
%{
	private StringBuffer whiteBeforeSB = new StringBuffer();
	private int lastTokenLine = 1, lastTokenCol = 1, lastTokenOffset = 0, lastTokenLength = 0;
	private Token lastToken = null;
	private StringBuffer whiteAfterSB = new StringBuffer();
	
	private void storeNonTreeToken()
	{
		whiteBeforeSB.append(yytext());
	}
	
	private Token token(Terminal terminal)
	{
		if (terminal == Terminal.END_OF_INPUT && lastToken != null)
		{
			lastToken.setWhiteAfter(lastToken.getWhiteAfter() + whiteBeforeSB.toString());
			whiteBeforeSB = new StringBuffer();
		}
		
		if (terminal == Terminal.T_SCON)
		{
			lastTokenLine = sbLine;
			lastTokenCol = sbCol;
			lastTokenOffset = sbOffset;
			lastTokenLength = stringBuffer.toString().length();
		}
		else
		{
			lastTokenLine = yyline+1;
			lastTokenCol = yycolumn+1;
			lastTokenOffset = yychar;
			lastTokenLength = yylength();
		}
		lastToken = new Token(terminal,
		                 whiteBeforeSB.toString(),
		                 terminal == Terminal.T_SCON ? stringBuffer.toString() : yytext(),
		                 whiteAfterSB.toString());
		whiteBeforeSB = new StringBuffer();
		whiteAfterSB = new StringBuffer();
		return lastToken;
	}

	//public static void main(String[] args) throws Exception
	//{
	//	FreeFormLexerPhase2 l = new FreeFormLexerPhase2(System.in);
	//	Parser.parse(l);
	//}
	
	private String filename = "<stdin>";
    
	public FreeFormLexerPhase1(java.io.InputStream in, String filename)
	{
	    this(new LineAppendingInputStream(in));
	    this.filename = filename;
	}

    public String getFilename()
    {
        return filename;
    }

    public int getLastTokenLine()
    {
        return lastTokenLine;
    }

    public int getLastTokenCol()
    {
        return lastTokenCol;
    }
    
    public int getLastTokenOffset()
    {
    	return lastTokenOffset;
    }
    
    public int getLastTokenLength()
    {
        return lastTokenLength;
    }
	
	private void startInclude() throws FileNotFoundException
	{
//	  //GRRRRR, yypushStream is in the JFlex docs but is not actually implemented in v1.4.1!
//	  
//	  if (!(this.zzReader instanceof PreprocessingReader))
//	    throw new Error("The reader passed to a Fortran lexer must be a PreprocessingReader");
//
//	  ((PreprocessingReader)zzReader).pushReader(extractFilenameFromFortranInclude(yytext()));
//	}
//	
//	private String extractFilenameFromFortranInclude(String fortranInclude)
//	{
//	  String removeInclude = fortranInclude.substring(7);
//	  return removeInclude.trim().replaceAll("\"", "");
	}
	
	private String getCurrentFilename()
	{
//	  if (!(this.zzReader instanceof PreprocessingReader))
//	    throw new Error("The reader passed to a Fortran lexer must be a PreprocessingReader");
//
//	  return ((PreprocessingReader)zzReader).currentFilename();
	  return filename;
	}

	private StringBuffer stringBuffer = null;

	private boolean wantEos = false;
	
	private int sbOffset = -1;
	private int sbLine = -1;
	private int sbCol = -1;
%}

LineTerminator = \r\n|\n|\r
WhiteSpace     = [ \t\f]+

Comment = "!" [^\r\n]*

StarredType = ("*"[0-9]+)?

Dig = [0-9]+
EExp = ("E"|"e")("+"|"-")?{Dig}
DExp = ("D"|"d")("+"|"-")?{Dig}
Sig1 = ({Dig} "." [0-9]+ | "." {Dig})
Sig2 = {Dig} "."
Sig = {Sig1} | {Sig2}
DdotD = {Dig} "." {Dig}
Efw = [Ee] {Dig}
Range = [a-zA-Z](-[a-zA-Z])?

Bcon = ( B ' [0-1]+ ' | B \" [0-1]+ \" )
Ocon = ( O ' [0-7]+ ' | O \" [0-7]+ \" )
Zcon = ( Z ' [0-9A-Fa-f]+ ' | Z \" [0-9A-Fa-f]+ \" )

Dcon1 = {Dig}{DExp} | {Sig}{DExp} | {Sig1}
Dcon2 = {Sig2}
Fcon = [IiBbOoZzFfDd] {WhiteSpace}* {DdotD} | (E [NnSs]? | e [NnSs]? | G | g) {WhiteSpace}* {DdotD} {Efw}?
Icon = ( {Dig} | {Dig} ("E"|"e") {Dig} "." {Dig} )
Pcon = ("+"|"-")? {Dig} (P|p)
Rcon1 = {Dig}{EExp} | {Sig}{EExp} | {Sig1}
Rcon2 = {Sig2}
Xcon = {Dig} (X|x)
Xdop = \. [A-Za-z]+ \.
Ident = [A-Za-z][A-Za-z0-9_]*
xImpl = "(" [ \t]* {Range} ([ \t]* "," [ \t]* {Range})* ")"
xImplLkahead = [ \t\f]*(","|";"|{LineTerminator}|"!")

NumDotLkahead = [^A-Za-z]

LineContinuation = "&"(({WhiteSpace}|{Comment})*{LineTerminator})+({WhiteSpace}*"&")?

CppIfdef="#ifdef"[^\r\n]*{LineTerminator}
CppIfndef="#ifndef"[^\r\n]*{LineTerminator}
CppIf="#if"[^\r\n]*{LineTerminator}
CppElse="#else"[^\r\n]*{LineTerminator}
CppElif="#elif"[^\r\n]*{LineTerminator}
CppEndIf="#endif"[^\r\n]*{LineTerminator}
CppInclude="#include"[^\r\n]*{LineTerminator}
CppDefine="#define"[^\r\n]*{LineTerminator}
CppUndef="#undef"[^\r\n]*{LineTerminator}
CppLine="#line"[^\r\n]*{LineTerminator}
CppError="#error"[^\r\n]*{LineTerminator}
CppPragma="#pragma"[^\r\n]*{LineTerminator}
CppDirective={CppIfdef}|{CppIfndef}|{CppIf}|{CppElse}|{CppElif}|{CppEndIf}|{CppInclude}|{CppDefine}|{CppUndef}|{CppLine}|{CppError}|{CppPragma}

FortranInclude="INCLUDE"[^=(%\r\n]*{LineTerminator}

%state IMPLICIT
%state QUOTED
%state DBLQUOTED
%state OPERATORorFORMAT

%%

/* Lexical rules */

<YYINITIAL,IMPLICIT,OPERATORorFORMAT> {
"ACCESS"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ACCESSEQ); }
"ACTION"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ACTIONEQ); }
"ADVANCE"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ADVANCEEQ); }
"ALLOCATABLE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ALLOCATABLE); }
"ALLOCATE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ALLOCATE); }
".AND."							{ wantEos = true;                     return token(Terminal.T_AND); }
"ASSIGN"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ASSIGN); }
"ASSIGNMENT"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ASSIGNMENT); }
"*"								{ wantEos = true;                     return token(Terminal.T_ASTERISK); }
"BACKSPACE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_BACKSPACE); }
"BLANK"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_BLANKEQ); }
"BLOCK"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_BLOCK); }
"BLOCK"[ \t]*"DATA"				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_BLOCKDATA); }
"CALL"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CALL); }
"CASE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CASE); }
"CHARACTER"{StarredType}		{ wantEos = true;                     return token(Terminal.T_CHARACTER); }
"CLOSE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CLOSE); }
":"								{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_COLON); }
","								{ wantEos = true;                     return token(Terminal.T_COMMA); }
"COMMON"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_COMMON); }
"COMPLEX"{StarredType}			{ wantEos = true;                     return token(Terminal.T_COMPLEX); }
"CONTAINS"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CONTAINS); }
"CONTINUE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CONTINUE); }
"CYCLE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_CYCLE); }
"DATA"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DATA); }
"DEALLOCATE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DEALLOCATE); }
"DEFAULT"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DEFAULT); }
"DELIM"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DELIMEQ); }
"DIMENSION"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DIMENSION); }
"DIRECT"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DIRECTEQ); }
"DO"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_DO); }
"DOUBLE"{StarredType}			{ wantEos = true;                     return token(Terminal.T_DOUBLE); }
"DOUBLEPRECISION"{StarredType}	{ wantEos = true;                     return token(Terminal.T_DOUBLEPRECISION); }
"ELEMENTAL"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ELEMENTAL); }
"ELSE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ELSE); }
"ELSEIF"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ELSEIF); }
"ELSEWHERE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ELSEWHERE); }
"END"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_END); }
"ENDBLOCK"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDBLOCK); }
"ENDBLOCKDATA"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDBLOCKDATA); }
"ENDDO"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDDO); }
"END"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDEQ); }
"ENDFILE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDFILE); }
"ENDFORALL"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDFORALL); }
"ENDFUNCTION"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDFUNCTION); }
"ENDIF"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDIF); }
"ENDINTERFACE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDINTERFACE); }
"ENDMODULE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDMODULE); }
"ENDPROGRAM"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDPROGRAM); }
"ENDSELECT"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDSELECT); }
"ENDSUBROUTINE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDSUBROUTINE); }
"ENDTYPE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDTYPE); }
"ENDWHERE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENDWHERE); }
"ENTRY"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ENTRY); }
"EOR"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EOREQ); }
".EQ."							{ wantEos = true;                     return token(Terminal.T_EQ); }
"=="							{ wantEos = true;                     return token(Terminal.T_EQEQ); }
"=>"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EQGREATERTHAN); }
"="								{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EQUALS); }
"EQUIVALENCE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EQUIVALENCE); }
".EQV."							{ wantEos = true;                     return token(Terminal.T_EQV); }
"ERR"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ERREQ); }
"EXIST"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EXISTEQ); }
"EXIT"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EXIT); }
"EXTERNAL"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_EXTERNAL); }
".FALSE."						{ wantEos = true;                     return token(Terminal.T_FALSE); }
"FILE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FILE); }
"FILE"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FILEEQ); }
"FMT"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FMTEQ); }
"FORALL"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FORALL); }
"FORMAT"						{ wantEos = true; yybegin(OPERATORorFORMAT);    return token(Terminal.T_FORMAT); }
"FORMATTED"[ \t]*"="			{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FORMATTEDEQ); }
"FORM"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FORMEQ); }
"FUNCTION"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_FUNCTION); }
".GE."							{ wantEos = true;                     return token(Terminal.T_GE); }
"GO"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_GO); }
"GOTO"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_GOTO); }
">"								{ wantEos = true;                     return token(Terminal.T_GREATERTHAN); }
">="							{ wantEos = true;                     return token(Terminal.T_GREATERTHANEQ); }
".GT."							{ wantEos = true;                     return token(Terminal.T_GT); }
"IF"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_IF); }
"IMPLICIT"						{ wantEos = true; yybegin(IMPLICIT); return token(Terminal.T_IMPLICIT); }
"IN"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_IN); }
"INOUT"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_INOUT); }
"INQUIRE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_INQUIRE); }
"INTEGER"{StarredType}			{ wantEos = true;                     return token(Terminal.T_INTEGER); }
"INTENT"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_INTENT); }
"INTERFACE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_INTERFACE); }
"INTRINSIC"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_INTRINSIC); }
"IOLENGTH"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_IOLENGTHEQ); }
"IOSTAT"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_IOSTATEQ); }
"KIND"[ \t]*"="					{ wantEos = true;                     return token(Terminal.T_KINDEQ); }
".LE."							{ wantEos = true;                     return token(Terminal.T_LE); }
"LEN"[ \t]*"="					{ wantEos = true;                     return token(Terminal.T_LENEQ); }
"<"								{ wantEos = true;                     return token(Terminal.T_LESSTHAN); }
"<="							{ wantEos = true;                     return token(Terminal.T_LESSTHANEQ); }
"LOGICAL"{StarredType}			{ wantEos = true;                     return token(Terminal.T_LOGICAL); }
"("								{ wantEos = true;                     return token(Terminal.T_LPAREN); }
".LT."							{ wantEos = true;                     return token(Terminal.T_LT); }
"-"								{ wantEos = true;                     return token(Terminal.T_MINUS); }
"MODULE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_MODULE); }
"NAMED"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NAMEDEQ); }
"NAME"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NAMEEQ); }
"NAMELIST"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NAMELIST); }
".NE."							{ wantEos = true;                     return token(Terminal.T_NE); }
".NEQV."						{ wantEos = true;                     return token(Terminal.T_NEQV); }
"NEXTREC"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NEXTRECEQ); }
"NML"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NMLEQ); }
"NONE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NONE); }
".NOT."							{ wantEos = true;                     return token(Terminal.T_NOT); }
"NULL"							{ wantEos = true;                     return token(Terminal.T_NULL); }
"NULLIFY"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NULLIFY); }
"NUMBER"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_NUMBEREQ); }
"ONLY"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_ONLY); }
"OPEN"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_OPEN); }
"OPENED"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_OPENEDEQ); }
"OPERATOR"						{ wantEos = true; yybegin(OPERATORorFORMAT);  return token(Terminal.T_OPERATOR); }
"OPTIONAL"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_OPTIONAL); }
".OR."							{ wantEos = true;                     return token(Terminal.T_OR); }
"OUT"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_OUT); }
"PAD"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PADEQ); }
"PARAMETER"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PARAMETER); }
"PAUSE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PAUSE); }
"%"								{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PERCENT); }
"+"								{ wantEos = true;                     return token(Terminal.T_PLUS); }
"POINTER"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_POINTER); }
"POSITION"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_POSITIONEQ); }
"**"							{ wantEos = true;                     return token(Terminal.T_POW); }
"PRECISION"						{ wantEos = true;                     return token(Terminal.T_PRECISION); }
"PRINT"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PRINT); }
"PRIVATE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PRIVATE); }
"PROCEDURE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PROCEDURE); }
"PROGRAM"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PROGRAM); }
"PUBLIC"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PUBLIC); }
"PURE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_PURE); }
"READ"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_READ); }
"READ"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_READEQ); }
"READWRITE"[ \t]*"="			{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_READWRITEEQ); }
"REAL"{StarredType}				{ wantEos = true;                     return token(Terminal.T_REAL); }
"REC"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_RECEQ); }
"RECL"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_RECLEQ); }
"RECURSIVE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_RECURSIVE); }
"RESULT"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_RESULT); }
"RETURN"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_RETURN); }
"REWIND"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_REWIND); }
")"								{ wantEos = true;                     return token(Terminal.T_RPAREN); }
"SAVE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SAVE); }
"SELECT"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SELECT); }
"SELECTCASE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SELECTCASE); }
"SEQUENCE"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SEQUENCE); }
"SEQUENTIAL"[ \t]*"="			{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SEQUENTIALEQ); }
"SIZE"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SIZEEQ); }
"/"								{ wantEos = true;                     return token(Terminal.T_SLASH); }
"/="							{ wantEos = true;                     return token(Terminal.T_SLASHEQ); }
"//"							{ wantEos = true;                     return token(Terminal.T_SLASHSLASH); }
"STAT"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_STATEQ); }
"STATUS"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_STATUSEQ); }
"STOP"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_STOP); }
"SUBROUTINE"					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SUBROUTINE); }
"TARGET"						{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_TARGET); }
"THEN"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_THEN); }
"TO"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_TO); }
".TRUE."						{ wantEos = true;                     return token(Terminal.T_TRUE); }
"TYPE"							{ wantEos = true;                     return token(Terminal.T_TYPE); }
"_"								{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_UNDERSCORE); }
"UNFORMATTED"[ \t]*"="			{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_UNFORMATTEDEQ); }
"UNIT"[ \t]*"="					{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_UNITEQ); }
"USE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_USE); }
"WHERE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_WHERE); }
"WHILE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_WHILE); }
"WRITE"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_WRITE); }
"WRITE"[ \t]*"="				{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_WRITEEQ); }
{Bcon}							{ wantEos = true;                     return token(Terminal.T_BCON); }
{Rcon1}							{ wantEos = true;                     return token(Terminal.T_RCON); }
{Rcon2}/{NumDotLkahead}			{ wantEos = true;                     return token(Terminal.T_RCON); }
{Dcon1}							{ wantEos = true;                     return token(Terminal.T_DCON); }
{Dcon2}/{NumDotLkahead}			{ wantEos = true;                     return token(Terminal.T_DCON); }
{Fcon}							{ wantEos = true;                     return token(Terminal.T_FCON); }
{Icon}							{ wantEos = true;                     return token(Terminal.T_ICON); }
{Ident}							{ wantEos = true;                     return token(Terminal.T_IDENT); }
{Ocon}							{ wantEos = true;                     return token(Terminal.T_OCON); }
{Pcon}							{ wantEos = true;                     return token(Terminal.T_PCON); }
{Xcon}							{ wantEos = true;                     return token(Terminal.T_XCON); }
{Xdop}							{ wantEos = true;                     return token(Terminal.T_XDOP); }
{Zcon}							{ wantEos = true;                     return token(Terminal.T_ZCON); }
"'"								{ stringBuffer = new StringBuffer();
								  stringBuffer.append('\'');
								  sbOffset = yychar;
								  sbLine = yyline+1;
								  sbCol = yycolumn+1;
								  yybegin(QUOTED); }
\"								{ stringBuffer = new StringBuffer();
								  stringBuffer.append('\"');
								  sbOffset = yychar;
								  sbLine = yyline+1;
								  sbCol = yycolumn+1;
								  yybegin(DBLQUOTED); }
{Comment}						{ storeNonTreeToken(); }
{WhiteSpace}					{ storeNonTreeToken(); }
{CppDirective}					{ storeNonTreeToken(); }
{FortranInclude}				{ storeNonTreeToken(); startInclude(); }
{LineContinuation}				{ storeNonTreeToken(); }
{LineTerminator}|";"			{ boolean b = wantEos; wantEos = false; if (b) return token(Terminal.T_EOS); else storeNonTreeToken(); }
<<EOF>>							{ wantEos = false; yybegin(YYINITIAL); return token(Terminal.END_OF_INPUT); }
}

<IMPLICIT> {
{xImpl}/{xImplLkahead}			{ wantEos = true;                     return token(Terminal.T_XIMPL); }
{xImpl}$						{ wantEos = true;                     return token(Terminal.T_XIMPL); }
}

<YYINITIAL,IMPLICIT> {
"(/"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_LPARENSLASH); }
"/)"							{ wantEos = true; yybegin(YYINITIAL); return token(Terminal.T_SLASHRPAREN); }
}

<QUOTED> {
"''"							{ stringBuffer.append("''"); }
"'"								{ stringBuffer.append('\'');
								  yybegin(YYINITIAL);
								  wantEos = true;
								  return token(Terminal.T_SCON); }
[^\n\r&']+						{ stringBuffer.append( yytext() ); }
{LineContinuation}				{ stringBuffer.append( yytext() ); }
"&"								{ stringBuffer.append( yytext() ); }
[\n\r]							{ throw new Exception("Lexer Error (" + getCurrentFilename() + ", line " + (yyline+1) + ", col " + (yycolumn+1) + "): String literal spans multiple lines without continuation"); }
<<EOF>>							{ throw new Exception("Lexer Error (" + getCurrentFilename() + ", line " + (yyline+1) + ", col " + (yycolumn+1) + "): End of file encountered before string literal terminated"); }
}

<DBLQUOTED> {
\"\"							{ stringBuffer.append("\"\""); }
\"								{ stringBuffer.append('\"');
								  yybegin(YYINITIAL);
								  wantEos = true;
								  return token(Terminal.T_SCON); }
[^\n\r&\"]+						{ stringBuffer.append( yytext() ); }
{LineContinuation}				{ stringBuffer.append( yytext() ); }
"&"								{ stringBuffer.append( yytext() ); }
[\n\r]							{ throw new Exception("Lexer Error (" + getCurrentFilename() + ", line " + (yyline+1) + ", col " + (yycolumn+1) + "): String literal spans multiple lines without continuation"); }
<<EOF>>							{ throw new Exception("Lexer Error (" + getCurrentFilename() + ", line " + (yyline+1) + ", col " + (yycolumn+1) + "): End of file encountered before string literal terminated"); }
}

/* Error */
.								{ throw new Exception("Lexer Error (" + getCurrentFilename() + ", line " + (yyline+1) + ", col " + (yycolumn+1) + "): Unexpected character " + yytext()); }
