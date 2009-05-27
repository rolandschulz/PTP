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

/**
 * Terminal symbols used by the Fortran lexer and parser.
 * 
 * @author Jeff Overbey
 */
public final class Terminal
{
    public static final Terminal SKIP = new Terminal("(skip)");
    public static final Terminal T_IMPORT = new Terminal("import");
    public static final Terminal T_NON_INTRINSIC = new Terminal("non_intrinsic");
    public static final Terminal T_WAIT = new Terminal("wait");
    public static final Terminal T_LBRACKET = new Terminal("[");
    public static final Terminal T_RBRACKET = new Terminal("]");
    public static final Terminal T_ENDBEFORESELECT = new Terminal("end");
    public static final Terminal T_STREAMEQ = new Terminal("stream=");
    public static final Terminal T_PENDINGEQ = new Terminal("pending=");
    public static final Terminal T_POSEQ = new Terminal("pos=");
    public static final Terminal T_IDEQ = new Terminal("id=");
    public static final Terminal T_SIGNEQ = new Terminal("sign=");
    public static final Terminal T_ROUNDEQ = new Terminal("round=");
    public static final Terminal T_IOMSGEQ = new Terminal("iomsg=");
    public static final Terminal T_ENCODINGEQ = new Terminal("encoding=");
    public static final Terminal T_DECIMALEQ = new Terminal("decimal=");
    public static final Terminal T_ASYNCHRONOUSEQ = new Terminal("asynchronous=");
    public static final Terminal T_IS = new Terminal("is");
    public static final Terminal T_ASSOCIATE = new Terminal("associate");
    public static final Terminal T_WRITEEQ = new Terminal("write=");
    public static final Terminal T_RESULT = new Terminal("result");
    public static final Terminal T_ENDSUBROUTINE = new Terminal("endsubroutine");
    public static final Terminal T_ENDBLOCKDATA = new Terminal("endblockdata");
    public static final Terminal T_DOUBLE = new Terminal("double");
    public static final Terminal T_FILE = new Terminal("file");
    public static final Terminal T_LESSTHANEQ = new Terminal("<=");
    public static final Terminal T_ENDFILE = new Terminal("endfile");
    public static final Terminal T_BACKSPACE = new Terminal("backspace");
    public static final Terminal T_PCON = new Terminal("constant");
    public static final Terminal T_FORALL = new Terminal("forall");
    public static final Terminal T_SELECTCASE = new Terminal("selectcase");
    public static final Terminal T_MINUS = new Terminal("minus");
    public static final Terminal T_WRITE = new Terminal("write");
    public static final Terminal T_NONE = new Terminal("none");
    public static final Terminal T_COMMON = new Terminal("common");
    public static final Terminal T_CYCLE = new Terminal("cycle");
    public static final Terminal T_IOSTATEQ = new Terminal("iostat=");
    public static final Terminal T_EQEQ = new Terminal("==");
    public static final Terminal T_ASTERISK = new Terminal("*");
    public static final Terminal T_ENDEQ = new Terminal("end=");
    public static final Terminal T_PUBLIC = new Terminal("public");
    public static final Terminal T_FCON = new Terminal("constant");
    public static final Terminal T_ELSE = new Terminal("else");
    public static final Terminal T_ENDMODULE = new Terminal("endmodule");
    public static final Terminal T_PAUSE = new Terminal("pause");
    public static final Terminal T_XDOP = new Terminal("user-defined operator");
    public static final Terminal T_END = new Terminal("end");
    public static final Terminal T_PURE = new Terminal("pure");
    public static final Terminal T_WHERE = new Terminal("where");
    public static final Terminal T_ENTRY = new Terminal("entry");
    public static final Terminal T_CONTAINS = new Terminal("contains");
    public static final Terminal T_OPTIONAL = new Terminal("optional");
    public static final Terminal T_TO = new Terminal("to");
    public static final Terminal T_ALLOCATABLE = new Terminal("allocatable");
    public static final Terminal T_COMMA = new Terminal("comma");
    public static final Terminal T_SIZEEQ = new Terminal("size=");
    public static final Terminal T_BLANKEQ = new Terminal("blank=");
    public static final Terminal T_ASSIGN = new Terminal("assign");
    public static final Terminal T_ENDSELECT = new Terminal("endselect");
    public static final Terminal T_GO = new Terminal("go");
    public static final Terminal T_POSITIONEQ = new Terminal("position=");
    public static final Terminal T_REWIND = new Terminal("rewind");
    public static final Terminal T_CLOSE = new Terminal("close");
    public static final Terminal T_BLOCK = new Terminal("block");
    public static final Terminal T_CONTINUE = new Terminal("continue");
    public static final Terminal T_DEALLOCATE = new Terminal("deallocate");
    public static final Terminal T_NAMEDEQ = new Terminal("named=");
    public static final Terminal T_EOS = new Terminal("end of statement");
    public static final Terminal T_STATEQ = new Terminal("stat=");
    public static final Terminal T_SLASH = new Terminal("/");
    public static final Terminal T_ENDTYPE = new Terminal("endtype");
    public static final Terminal T_LESSTHAN = new Terminal("<");
    public static final Terminal T_DIRECTEQ = new Terminal("direct=");
    public static final Terminal T_CHARACTER = new Terminal("character");
    public static final Terminal T_SLASHRPAREN = new Terminal("/)");
    public static final Terminal T_NAMEEQ = new Terminal("name=");
    public static final Terminal T_FUNCTION = new Terminal("function");
    public static final Terminal T_INQUIRE = new Terminal("inquire");
    public static final Terminal T_NUMBEREQ = new Terminal("number=");
    public static final Terminal T_BCON = new Terminal("bcon");
    public static final Terminal T_RPAREN = new Terminal(")");
    public static final Terminal T_ENDFORALL = new Terminal("endforall");
    public static final Terminal T_LOGICAL = new Terminal("logical");
    public static final Terminal T_NE = new Terminal(".NE.");
    public static final Terminal T_PADEQ = new Terminal("pad=");
    public static final Terminal T_EQV = new Terminal(".EQV.");
    public static final Terminal T_ENDPROGRAM = new Terminal("endprogram");
    public static final Terminal T_RECLEQ = new Terminal("recl=");
    public static final Terminal T_THEN = new Terminal("then");
    public static final Terminal T_DIMENSION = new Terminal("dimension");
    public static final Terminal T_DELIMEQ = new Terminal("delim=");
    public static final Terminal T_X_IMPL = new Terminal("implicit specification");
    public static final Terminal T_LT = new Terminal(".LT.");
    public static final Terminal T_SUBROUTINE = new Terminal("subroutine");
    public static final Terminal T_ENDWHERE = new Terminal("endwhere");
    public static final Terminal T_CALL = new Terminal("call");
    public static final Terminal T_USE = new Terminal("use");
    public static final Terminal T_RCON = new Terminal("real constant");
    public static final Terminal T_FORMEQ = new Terminal("form=");
    public static final Terminal T_FMTEQ = new Terminal("fmt=");
    public static final Terminal T_DATA = new Terminal("data");
    public static final Terminal T_OPEN = new Terminal("open");
    public static final Terminal T_CASE = new Terminal("case");
    public static final Terminal T_ASSIGNMENT = new Terminal("assignment");
    public static final Terminal T_RECEQ = new Terminal("rec=");
    public static final Terminal T_ICON = new Terminal("integer constant");
    public static final Terminal T_MODULE = new Terminal("module");
    public static final Terminal T_REAL = new Terminal("real");
    public static final Terminal T_FORMAT = new Terminal("format");
    public static final Terminal T_BLOCKDATA = new Terminal("blockdata");
    public static final Terminal T_ZCON = new Terminal("hexadecimal constant");
    public static final Terminal T_UNITEQ = new Terminal("uniteq");
    public static final Terminal T_PRECISION = new Terminal("precision");
    public static final Terminal T_INOUT = new Terminal("inout");
    public static final Terminal T_ELEMENTAL = new Terminal("elemental");
    public static final Terminal T_OR = new Terminal("or");
    public static final Terminal T_EOREQ = new Terminal("eor=");
    public static final Terminal T_FALSE = new Terminal("false");
    public static final Terminal T_INTEGER = new Terminal("integer");
    public static final Terminal T_EQUIVALENCE = new Terminal("equivalence");
    public static final Terminal T_STATUSEQ = new Terminal("status=");
    public static final Terminal T_TYPE = new Terminal("type");
    public static final Terminal T_RETURN = new Terminal("return");
    public static final Terminal T_SELECT = new Terminal("select");
    public static final Terminal T_ELSEIF = new Terminal("elseif");
    public static final Terminal T_IDENT = new Terminal("identifier");
    public static final Terminal T_GE = new Terminal(".GE.");
    public static final Terminal T_POW = new Terminal("**");
    public static final Terminal T_PARAMETER = new Terminal("parameter");
    public static final Terminal T_ENDINTERFACE = new Terminal("endinterface");
    public static final Terminal T_OUT = new Terminal("out");
    public static final Terminal T_INTENT = new Terminal("intent");
    public static final Terminal T_EXISTEQ = new Terminal("exist=");
    public static final Terminal T_NULLIFY = new Terminal("nullify");
    public static final Terminal T_PRINT = new Terminal("print");
    public static final Terminal T_EQ = new Terminal("=");
    public static final Terminal T_STOP = new Terminal("stop");
    public static final Terminal T_DEFAULT = new Terminal("default");
    public static final Terminal T_SEQUENCE = new Terminal("sequence");
    public static final Terminal T_UNFORMATTEDEQ = new Terminal("unformatted=");
    public static final Terminal T_OPERATOR = new Terminal("operator");
    public static final Terminal T_SCON = new Terminal("character constant");
    public static final Terminal T_ERREQ = new Terminal("err=");
    public static final Terminal T_IF = new Terminal("if");
    public static final Terminal T_ADVANCEEQ = new Terminal("advance=");
    public static final Terminal T_EXTERNAL = new Terminal("external");
    public static final Terminal T_PRIVATE = new Terminal("private");
    public static final Terminal T_NEXTRECEQ = new Terminal("nextrec=");
    public static final Terminal T_SLASHSLASH = new Terminal("//");
    public static final Terminal T_PLUS = new Terminal("+");
    public static final Terminal T_EQGREATERTHAN = new Terminal("=>");
    public static final Terminal END_OF_INPUT = new Terminal("end of input");
    public static final Terminal T_ENDFUNCTION = new Terminal("endfunction");
    public static final Terminal T_TARGET = new Terminal("target");
    public static final Terminal T_PERCENT = new Terminal("%");
    public static final Terminal T_READWRITEEQ = new Terminal("readwrite=");
    public static final Terminal T_POINTER = new Terminal("pointer");
    public static final Terminal T_IOLENGTHEQ = new Terminal("iolength=");
    public static final Terminal T_OPENEDEQ = new Terminal("opened=");
    public static final Terminal T_LE = new Terminal("<=");
    public static final Terminal T_ENDDO = new Terminal("enddo");
    public static final Terminal T_IN = new Terminal("in");
    public static final Terminal T_GOTO = new Terminal("goto");
    public static final Terminal T_COLON = new Terminal(":");
    public static final Terminal T_READ = new Terminal("read");
    public static final Terminal T_LENEQ = new Terminal("len=");
    public static final Terminal T_NOT = new Terminal("not");
    public static final Terminal T_DCON = new Terminal("constant");
    public static final Terminal T_ALLOCATE = new Terminal("allocate");
    public static final Terminal T_EQUALS = new Terminal("equals");
    public static final Terminal T_ENDIF = new Terminal("endif");
    public static final Terminal T_TRUE = new Terminal("true");
    public static final Terminal T_UNDERSCORE = new Terminal("underscore");
    public static final Terminal T_XCON = new Terminal("constant");
    public static final Terminal T_IMPLICIT = new Terminal("implicit");
    public static final Terminal T_NAMELIST = new Terminal("namelist");
    public static final Terminal T_RECURSIVE = new Terminal("recursive");
    public static final Terminal T_OCON = new Terminal("octal constant");
    public static final Terminal T_ENDBLOCK = new Terminal("endblock");
    public static final Terminal T_ACCESSEQ = new Terminal("access=");
    public static final Terminal T_SLASHEQ = new Terminal("slash=");
    public static final Terminal T_COMPLEX = new Terminal("complex");
    public static final Terminal T_ONLY = new Terminal("only");
    public static final Terminal T_PROCEDURE = new Terminal("procedure");
    public static final Terminal T_INTRINSIC = new Terminal("intrinsic");
    public static final Terminal T_KINDEQ = new Terminal("kind=");
    public static final Terminal T_FORMATTEDEQ = new Terminal("formatted=");
    public static final Terminal T_SEQUENTIALEQ = new Terminal("sequential=");
    public static final Terminal T_ELSEWHERE = new Terminal("elsewhere");
    public static final Terminal T_DOUBLEPRECISION = new Terminal("doubleprecision");
    public static final Terminal T_PROGRAM = new Terminal("program");
    public static final Terminal T_SAVE = new Terminal("save");
    public static final Terminal T_DO = new Terminal("do");
    public static final Terminal T_FILEEQ = new Terminal("file=");
    public static final Terminal T_WHILE = new Terminal("while");
    public static final Terminal T_ACTIONEQ = new Terminal("action=");
    public static final Terminal T_LPARENSLASH = new Terminal("(/");
    public static final Terminal T_INTERFACE = new Terminal("interface");
    public static final Terminal T_READEQ = new Terminal("read=");
    public static final Terminal T_GREATERTHANEQ = new Terminal(">=");
    public static final Terminal T_NEQV = new Terminal("neqv");
    public static final Terminal T_NULL = new Terminal("null");
    public static final Terminal T_LPAREN = new Terminal("(");
    public static final Terminal T_NMLEQ = new Terminal("nml=");
    public static final Terminal T_GT = new Terminal("gt");
    public static final Terminal T_GREATERTHAN = new Terminal(">");
    public static final Terminal T_AND = new Terminal("and");
    public static final Terminal T_HCON = new Terminal("Hollerith constant");
    public static final Terminal T_EXIT = new Terminal("exit");
    public static final Terminal T_UNEXPECTED_CHARACTER = new Terminal("extraneous character");
    
    // New for Fortran 2003
    public static final Terminal T_EXTENDS = new Terminal("extends");
    public static final Terminal T_ABSTRACT = new Terminal("abstract");
    public static final Terminal T_BIND = new Terminal("bind");
    public static final Terminal T_PASS = new Terminal("pass");
    public static final Terminal T_NOPASS = new Terminal("nopass");
    public static final Terminal T_GENERIC = new Terminal("generic");
    public static final Terminal T_NON_OVERRIDABLE = new Terminal("non_overridable");
    public static final Terminal T_DEFERRED = new Terminal("deferred");
    public static final Terminal T_FINAL = new Terminal("final");
    public static final Terminal T_KIND = new Terminal("kind");
    public static final Terminal T_LEN = new Terminal("len");
    public static final Terminal T_ENUM = new Terminal("enum");
    public static final Terminal T_ENUMERATOR = new Terminal("enumerator");
    public static final Terminal T_CLASS = new Terminal("class");
    public static final Terminal T_ASYNCHRONOUS = new Terminal("asynchronous");
    public static final Terminal T_PROTECTED = new Terminal("protected");
    public static final Terminal T_VALUE = new Terminal("value");
    public static final Terminal T_VOLATILE = new Terminal("volatile");

    private String description;

    public Terminal(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
