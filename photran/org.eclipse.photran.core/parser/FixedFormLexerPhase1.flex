/*
 * First phase of fixed form lexical analysis for Fortran 95 parser
 *
 * FixedFormLexerPhase1 acts as a "token stream" feeding FreeFormLexerPhase2
 * and is feeded by FixedFormLexerPrepass
 * (See FixedFormLexerPrepass and FixedFormLexerPhase2.java and f95t.bnf)
 *
 * @author Jeffrey Overbey and Dirk Rossow
 * 
 * @see FixedFormLexerPrepass
 * @see FixedFormLexerPhase2
 * @see Parser
 *
 * NOTE: Get rid of (space out) "yybegin(YYSTANDARD);" in the lines
 * for any tokens that can appear in an IMPLICIT statement
 * (It was also omitted in the lines for T_SLASH so that
 * INTERFACE OPERATOR (/) would tokenize correctly.)
 *
 * Changes:
 * 29.06.2005 Jeff Overbey: Added Fortran INCLUDE and CPP directives
 */
 
package org.eclipse.photran.internal.core.lexer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;
import org.eclipse.photran.internal.core.parser.Terminal;

%%
 
%class FixedFormLexerPhase1
%throws Exception
%char
%line
%column
%ignorecase
//%8bit
%unicode 
%implements ILexer
%type Token

%{
	private FixedFormLexerPrepass prepass;
    
    public FixedFormLexerPhase1(InputStream in, FixedFormLexerPrepass _prepass) {
		this(new LineAppendingInputStream(in));
		prepass=_prepass;
    }
    
    //unset start of line state
    private void unsetSOL() {
    	if (yystate()==YYINITIAL) yybegin(YYSTANDARD); 
    }
    
    public int getLine() {
      return prepass.getLine(yychar);
    }
    public int getCol() {
      return prepass.getColumn(yychar);
    }
    
    private int lastTokenLine = 1, lastTokenCol = 1, lastTokenOffset = 0, lastTokenLength = 0;
    
	private Token token(Terminal terminal)
	{
		lastTokenLine = prepass.getLine(yychar)+1;
		lastTokenCol = prepass.getColumn(yychar)+1;
		lastTokenOffset = prepass.getOffset(yychar);
		lastTokenLength = prepass.getOffset(yychar+yylength()-1)-prepass.getOffset(yychar)+1;
		return new Token(terminal,
		                 "",
		                 terminal == Terminal.T_SCON || terminal == Terminal.T_HCON
		                     ? stringBuffer.toString()
		                     : yytext(),
		                 "");
	}

	/*
	private Token token(Terminal terminal)
	{
		Token t = new Token();
		t.setTerminal(terminal);
		t.setFilename(this.filename);
		t.setOffset(prepass.getOffset(yychar));
		t.setLength(prepass.getOffset(yychar+yylength()-1)-prepass.getOffset(yychar)+1);
		t.setText(terminal == Terminal.T_SCON || terminal == Terminal.T_HCON
		          ? stringBuffer.toString()
		          : yytext());
		t.setStartLine(prepass.getLine(yychar)+1);
		t.setStartCol(prepass.getColumn(yychar)+1);
		t.setEndLine(prepass.getLine(yychar+yylength()-1)+1);
		t.setEndCol(prepass.getColumn(yychar+yylength()-1)+1);

		return t;
	}
	*/

	private StringBuffer stringBuffer = null;
	private int hollerithLength = 0;

	private boolean wantEos = false;
	
	private String filename = "<stdin>";
    
	public FixedFormLexerPhase1(java.io.InputStream in, FixedFormLexerPrepass _prepass, String filename)
	{
	    this(in, _prepass);
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

//	private List/*<NonTreeToken>*/ nonTreeTokens = new LinkedList();
//    public List/*<NonTreeToken>*/ getNonTreeTokens()
//    {
//    	return nonTreeTokens;
//    }
//
	private void storeNonTreeToken()
	{
//		nonTreeTokens.add(new NonTreeToken(this.filename,
//				prepass.getOffset(yychar),   // int offset
//				prepass.getLine(yychar)+1,   // int row
//				prepass.getColumn(yychar)+1, // int col
//		        yytext()));
	}
%}

LineTerminator = \r\n|\n|\r


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
Hcon = {Dig}(H|h)

Dcon1 = {Dig}{DExp} | {Sig}{DExp} | {Sig1}
Dcon2 = {Sig2}
Fcon = [IiBbOoZzFfDd] {DdotD} | (E [NnSs]? | e [NnSs]? | G | g) {DdotD} {Efw}?
Icon = ( {Dig} | {Dig} ("E"|"e") {Dig} "." {Dig} )
Pcon = ("+"|"-")? {Dig} (P|p)
Rcon1 = {Dig}{EExp} | {Sig}{EExp} | {Sig1}
Rcon2 = {Sig2}
Xcon = {Dig} (X|x)
Xdop = \. [A-Za-z]+ \.
Ident = [A-Za-z][A-Za-z0-9_]*
xImpl = "(" {Range} ("," {Range})* ")"
xImplLkahead = (","|";"|{LineTerminator}|"!")

NumDotLkahead = [^A-Za-z]

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
%state HOLLERITH
%state YYSTANDARD
%state OPERATORorFORMAT
%state IDENT 

%%

/* Lexical rules */


/* ignore some tokens  on start of line*/
<YYSTANDARD,IMPLICIT,OPERATORorFORMAT> {
{Hcon}							{ stringBuffer = new StringBuffer();
								  String text = yytext();
								  stringBuffer.append(text);								  
								  hollerithLength=Integer.parseInt(text.substring(0,text.length()-1));
								  if (hollerithLength==0) throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): Invalid length of hollerith literal: 0"); 
								  yybegin(HOLLERITH); 
								 }
{Rcon1}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RCON); }
{Rcon2}/{NumDotLkahead}			{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RCON); }
{Dcon1}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DCON); }
{Dcon2}/{NumDotLkahead}			{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DCON); }
{Fcon}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FCON); }
{Pcon}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PCON); }
{Xcon}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_XCON); }
}

<IDENT> {
{Ident}							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_IDENT); }
}


<YYSTANDARD,YYINITIAL,IMPLICIT,OPERATORorFORMAT> {
"ACCESS="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ACCESSEQ); }
"ACTION="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ACTIONEQ); }
"ADVANCE="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ADVANCEEQ); }
"ALLOCATABLE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ALLOCATABLE); }
"ALLOCATE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ALLOCATE); }
"ASSIGNMENT"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ASSIGNMENT); }
"BACKSPACE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_BACKSPACE); }
"BLANK="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_BLANKEQ); }
"BLOCK"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_BLOCK); }
"BLOCKDATA"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_BLOCKDATA); }
"CALL"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CALL); }
"CASE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CASE); }
"CHARACTER"{StarredType}		{ wantEos = true; unsetSOL();          ; return token(Terminal.T_CHARACTER); }
"CLOSE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CLOSE); }
"COMMON"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_COMMON); }
"COMPLEX"{StarredType}			{ wantEos = true; unsetSOL();          ; return token(Terminal.T_COMPLEX); }
"CONTAINS"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CONTAINS); }
"CONTINUE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CONTINUE); }
"CYCLE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_CYCLE); }
"DATA"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DATA); }
"DEALLOCATE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DEALLOCATE); }
"DEFAULT"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DEFAULT); }
"DELIM="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DELIMEQ); }
"DIMENSION"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DIMENSION); }
"DIRECT="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DIRECTEQ); }
"DO"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_DO); }
"DOUBLE"{StarredType}			{ wantEos = true; unsetSOL();          ; return token(Terminal.T_DOUBLE); }
"DOUBLEPRECISION"{StarredType}	{ wantEos = true; unsetSOL();          ; return token(Terminal.T_DOUBLEPRECISION); }
"ELEMENTAL"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ELEMENTAL); }
"ELSE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ELSE); }
"ELSEIF"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ELSEIF); }
"ELSEWHERE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ELSEWHERE); }
"END"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_END); }
"ENDBLOCK"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDBLOCK); }
"ENDBLOCKDATA"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDBLOCKDATA); }
"ENDDO"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDDO); }
"END="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDEQ); }
"ENDFILE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDFILE); }
"ENDFORALL"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDFORALL); }
"ENDFUNCTION"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDFUNCTION); }
"ENDIF"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDIF); }
"ENDINTERFACE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDINTERFACE); }
"ENDMODULE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDMODULE); }
"ENDPROGRAM"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDPROGRAM); }
"ENDSELECT"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDSELECT); }
"ENDSUBROUTINE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDSUBROUTINE); }
"ENDTYPE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDTYPE); }
"ENDWHERE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENDWHERE); }
"ENTRY"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ENTRY); }
"EQUIVALENCE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EQUIVALENCE); }
"ERR="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ERREQ); }
"EXIST="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EXISTEQ); }
"EXIT"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EXIT); }
"EXTERNAL"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EXTERNAL); }
"FILE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FILE); }
"FILE="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FILEEQ); }
"FMT="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FMTEQ); }
"FORALL"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FORALL); }
"FORMAT"						{ wantEos = true; yybegin(OPERATORorFORMAT); return token(Terminal.T_FORMAT); }
"FORMATTED="					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FORMATTEDEQ); }
"FORM="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FORMEQ); }
"FUNCTION"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_FUNCTION); }
"GO"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_GO); }
"GOTO"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_GOTO); }
"IF"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_IF); }
"IMPLICIT"						{ wantEos = true; yybegin(IMPLICIT); return token(Terminal.T_IMPLICIT); }
"IN"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_IN); }
"INOUT"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_INOUT); }
"INQUIRE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_INQUIRE); }
"INTEGER"{StarredType}			{ wantEos = true; unsetSOL();          return token(Terminal.T_INTEGER); }
"INTENT"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_INTENT); }
"INTERFACE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_INTERFACE); }
"INTRINSIC"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_INTRINSIC); }
"IOLENGTH="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_IOLENGTHEQ); }
"IOSTAT="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_IOSTATEQ); }
"KIND="							{ wantEos = true; unsetSOL();          return token(Terminal.T_KINDEQ); }
"LEN="							{ wantEos = true; unsetSOL();          return token(Terminal.T_LENEQ); }
"LOGICAL"{StarredType}			{ wantEos = true; unsetSOL();          return token(Terminal.T_LOGICAL); }
"MODULE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_MODULE); }
"NAMED="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NAMEDEQ); }
"NAME="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NAMEEQ); }
"NAMELIST"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NAMELIST); }
"NEXTREC="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NEXTRECEQ); }
"NML="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NMLEQ); }
"NONE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NONE); }
"NULL"							{ wantEos = true; unsetSOL();          return token(Terminal.T_NULL); }
"NULLIFY"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NULLIFY); }
"NUMBER="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_NUMBEREQ); }
"ONLY"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_ONLY); }
"OPEN"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_OPEN); }
"OPENED="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_OPENEDEQ); }
"OPERATOR"						{ wantEos = true; yybegin(OPERATORorFORMAT);  return token(Terminal.T_OPERATOR); }
"OPTIONAL"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_OPTIONAL); }
"OUT"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_OUT); }
"PAD="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PADEQ); }
"PARAMETER"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PARAMETER); }
"PAUSE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PAUSE); }
"POINTER"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_POINTER); }
"POSITION="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_POSITIONEQ); }
"PRECISION"						{ wantEos = true; unsetSOL();          return token(Terminal.T_PRECISION); }
"PRINT"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PRINT); }
"PRIVATE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PRIVATE); }
"PROCEDURE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PROCEDURE); }
"PROGRAM"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PROGRAM); }
"PUBLIC"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PUBLIC); }
"PURE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PURE); }
"READ"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_READ); }
"READ="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_READEQ); }
"READWRITE="					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_READWRITEEQ); }
"REAL"{StarredType}				{ wantEos = true; unsetSOL();          return token(Terminal.T_REAL); }
"REC="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RECEQ); }
"RECL="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RECLEQ); }
"RECURSIVE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RECURSIVE); }
"RESULT"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RESULT); }
"RETURN"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_RETURN); }
"REWIND"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_REWIND); }
"SAVE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SAVE); }
"SELECT"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SELECT); }
"SELECTCASE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SELECTCASE); }
"SEQUENCE"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SEQUENCE); }
"SEQUENTIAL="					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SEQUENTIALEQ); }
"SIZE="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SIZEEQ); }
"STAT="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_STATEQ); }
"STATUS="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_STATUSEQ); }
"STOP"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_STOP); }
"SUBROUTINE"					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SUBROUTINE); }
"TARGET"						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_TARGET); }
"THEN"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_THEN); }
"TO"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_TO); }
".TRUE."						{ wantEos = true; unsetSOL();          return token(Terminal.T_TRUE); }
"TYPE"							{ wantEos = true; unsetSOL();          return token(Terminal.T_TYPE); }
"UNFORMATTED="					{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_UNFORMATTEDEQ); }
"UNIT="							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_UNITEQ); }
"USE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_USE); }
"WHERE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_WHERE); }
"WHILE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_WHILE); }
"WRITE"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_WRITE); }
"WRITE="						{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_WRITEEQ); }
"*"								{ wantEos = true; unsetSOL();          return token(Terminal.T_ASTERISK); }
".AND."							{ wantEos = true; unsetSOL();          return token(Terminal.T_AND); }
":"								{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_COLON); }
","								{ wantEos = true; unsetSOL();          return token(Terminal.T_COMMA); }
".EQ."							{ wantEos = true; unsetSOL();          return token(Terminal.T_EQ); }
"=="							{ wantEos = true; unsetSOL();          return token(Terminal.T_EQEQ); }
"=>"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EQGREATERTHAN); }
"="								{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_EQUALS); }
".EQV."							{ wantEos = true; unsetSOL();          return token(Terminal.T_EQV); }
".FALSE."						{ wantEos = true; unsetSOL();          return token(Terminal.T_FALSE); }
".GE."							{ wantEos = true; unsetSOL();          return token(Terminal.T_GE); }
">"								{ wantEos = true; unsetSOL();          return token(Terminal.T_GREATERTHAN); }
">="							{ wantEos = true; unsetSOL();          return token(Terminal.T_GREATERTHANEQ); }
".GT."							{ wantEos = true; unsetSOL();          return token(Terminal.T_GT); }
".LE."							{ wantEos = true; unsetSOL();          return token(Terminal.T_LE); }
"<"								{ wantEos = true; unsetSOL();          return token(Terminal.T_LESSTHAN); }
"<="							{ wantEos = true; unsetSOL();          return token(Terminal.T_LESSTHANEQ); }
"("								{ wantEos = true; unsetSOL();          return token(Terminal.T_LPAREN); }
".LT."							{ wantEos = true; unsetSOL();          return token(Terminal.T_LT); }
"-"								{ wantEos = true; unsetSOL();          return token(Terminal.T_MINUS); }
".NE."							{ wantEos = true; unsetSOL();          return token(Terminal.T_NE); }
".NEQV."						{ wantEos = true; unsetSOL();          return token(Terminal.T_NEQV); }
".NOT."							{ wantEos = true; unsetSOL();          return token(Terminal.T_NOT); }
".OR."							{ wantEos = true; unsetSOL();          return token(Terminal.T_OR); }
"%"								{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_PERCENT); }
"+"								{ wantEos = true; unsetSOL();          return token(Terminal.T_PLUS); }
"**"							{ wantEos = true; unsetSOL();          return token(Terminal.T_POW); }
")"								{ wantEos = true; unsetSOL();          return token(Terminal.T_RPAREN); }
"/"								{ wantEos = true; unsetSOL();          return token(Terminal.T_SLASH); }
"/="							{ wantEos = true; unsetSOL();          return token(Terminal.T_SLASHEQ); }
"//"							{ wantEos = true; unsetSOL();          return token(Terminal.T_SLASHSLASH); }
"_"								{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_UNDERSCORE); }
{Bcon}							{ wantEos = true; unsetSOL();          return token(Terminal.T_BCON); }
{Icon}							{ wantEos = true; unsetSOL();          return token(Terminal.T_ICON); }
{Ocon}							{ wantEos = true; unsetSOL();          return token(Terminal.T_OCON); }
{Xdop}							{ wantEos = true; unsetSOL();          return token(Terminal.T_XDOP); }
{Zcon}							{ wantEos = true; unsetSOL();          return token(Terminal.T_ZCON); }
"'"								{ stringBuffer = new StringBuffer();
								  stringBuffer.append('\'');
								  yybegin(QUOTED); }
\"								{ stringBuffer = new StringBuffer();
								  stringBuffer.append('\"');
								  yybegin(DBLQUOTED); 
								}
{CppDirective}					{ storeNonTreeToken(); }
{FortranInclude}				{ storeNonTreeToken(); }
{LineTerminator}				{ yybegin(YYINITIAL); boolean b = wantEos; wantEos = false; if (b) return token(Terminal.T_EOS); else storeNonTreeToken(); }
<<EOF>>							{ wantEos = false; yybegin(YYSTANDARD); return token(Terminal.END_OF_INPUT); }
.								{ 	yypushback(1); 
									int state=yystate();
									yybegin(IDENT);
									Token token = yylex();
									yybegin(state);
									return token;
								}
}


<IMPLICIT> {
{xImpl}/{xImplLkahead}			{ wantEos = true;                     return token(Terminal.T_XIMPL); }
{xImpl}$						{ wantEos = true;                     return token(Terminal.T_XIMPL); }
}

<YYSTANDARD,IMPLICIT> {
"(/"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_LPARENSLASH); }
"/)"							{ wantEos = true; yybegin(YYSTANDARD); return token(Terminal.T_SLASHRPAREN); }
}

<QUOTED> {
"''"							{ stringBuffer.append('\''); }
"'"								{ stringBuffer.append('\'');
								  yybegin(YYSTANDARD);
								  wantEos = true;
								  return token(Terminal.T_SCON); }
[^\n\r']+						{ stringBuffer.append( yytext() ); }
[\n\r]							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): String literal spans multiple lines without continuation"); }
<<EOF>>							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): End of file encountered before string literal terminated"); }
}

<DBLQUOTED> {
\"\"							{ stringBuffer.append('\"'); }
\"								{ stringBuffer.append('\"');
								  yybegin(YYSTANDARD);
								  wantEos = true;
								  return token(Terminal.T_SCON); }
[^\n\r\"]+						{ stringBuffer.append( yytext() ); }
[\n\r]							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): String literal spans multiple lines without continuation"); }
<<EOF>>							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): End of file encountered before string literal terminated"); }
}

<HOLLERITH> {
[^\n\r]	 					    {	hollerithLength--;
									stringBuffer.append(yytext());
									if (hollerithLength==0) {
									  	yybegin(YYSTANDARD);
									  	wantEos = true;
									  	return token(Terminal.T_HCON);
									 }
								}
[\n\r]							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): Hollerith literal spans multiple lines without continuation"); }
<<EOF>>							{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): End of file encountered before hollerith literal terminated"); }
}


/* Error */
.								{ throw new Exception("Lexer Error (line " + (getLine()+1) + ", col " + (getCol()+1) + "): Unexpected character " + yytext()); }
