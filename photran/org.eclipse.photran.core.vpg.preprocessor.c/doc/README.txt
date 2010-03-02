There are 3 folders in this zip file:

-scanner_mm: Contains code for the CPreprocessor.
    These classes should go in the project org.eclipse.photran.core.vpg.tests
    and in the package org.eclipse.photran.internal.core.tests.scanner_mm
-y_cpp_source_reproduction: Contains code for testing the CPreprocessor.
    These classes should go in the project org.eclipse.photran.core.vpg.tests
    and in the package 
    org.eclipse.photran.internal.core.tests.y_cpp_source_reproduction
-cpp-test-code: Contains test files to parse.
    This folder should go in the project org.eclipse.photran.core.vpg.tests
    (same as where parser-test-code is)


Most of the files in scanner_mm were originally copies of
the files in the package org.eclipse.cdt.internal.core.parser.scanner .
I named it scanner_mm early on because it is a copy of a package named
"scanner", and "mm" are my initials, and I didn't know what else to
call it. You can change the name and/or move the package if you want.
(For example, did you want to call the package x_cpreprocessor?)
The classes IToken and OffsetLimitReachedException were taken from
a different package: org.eclipse.cdt.core.parser . I made the classes
CppHelper and TokenTypeTranslator.


I made a helper class called CppHelper, which provides an interface
to the functionality of the CPreprocessor. Reading
through the code and comments of CppHelper should help to
understand how to use the CPreprocessor and the tokens that come from it.

I made a number of test files. These files can be found in
the folder cpp-test-code. These files all have the extension
.f90 so that they would be recognized by the test suite (even though
most do not contain Fortran code). I split up the code into four
different categories. The first, basic_test_files, contains fortran
code that I just copied from parser-test-code, to see if the
CPreprocessor could reproduce normal text with no directives or macros.
The second, token_test_files, tests a number of things unrelated
to directives, such as trigraphs, line-splices, comments, '@' characters,
etc. The third, macro_test_files, tests a number of different
things that can happen concerning macros. The fourth,
other_directive_test_files, tests some directives other than the #define
directive, especially #include directives. All of these tests are
successful.

The package y_cpp_source_reproduction contains functionality to
test the files in cpp-test-code to see if the source code can
be reproduced. The code is based on code from
the package org.eclipse.photran.internal.core.tests
and org.eclipse.photran.internal.core.tests.b_source_reproduction.
I edited this code to work with CppHelper, and I made a few other
modifications. Most notably, I added a "print" parameter to the
CppSourceReproductionTestSuite which, if it is true, will
print out details about all of the tokens. There are four files
that can be run as tests (which test the four corresponding
subdirectories of cpp-test-code): PrintBasicTestFiles,
PrintTokenTestFiles, PrintMacroTestFiles, and PrintOtherDirectiveTestFiles.


Suppose, for example, there was a file called "test.f90" with the
following code:

cout << "hello" << endl;
#define SQUARE(x) x*x
cout << SQUARE(17) << endl;
//no newline after this comment

If the test is set to print out details, this is the result:

----------------------------------------------------
file: test.f90
----------------------------------------------------
~cout
 ~<<
 ~"hello"
 ~<<
 ~endl
~;
~\n|~\r\n
~|~#define SQUARE(x) x*x
~\n|~\r\n
~cout
 ~<<
 ~17| ~SQUARE(17)
~*| ~SQUARE(17)
~17| ~SQUARE(17)
 ~<<
 ~endl
~;
~\n|~\r\n
//no newline after this comment~

Each line gives details about a token and that token's ancestors.
A '~' is placed between the preceding-white-space and the token-image
for each token. A '|' is placed between a token and its parent.
'\n' is replaced with "\\n", and '\r' is replaced with "\\r" so that
the details for a token (and its ancestors) remain on one line.

If you want, I added an option that will cause the token type
to be printed out for each token and its ancestors. This makes
use of the TokenTypeTranslator class that I made. Using this causes
the output to get a little cluttered, though. You can toggle this
feature, along with the characters used as separators (currently
'~' and '|') in CppSourceReproductionTestCase (in the package
y_cpp_source_reproduction) where it calls CppHelper.getTokenDetails(...).
I haven't done much testing concerning token types (since I just
made TokenTypeTranslator).


Concerning the package scanner_mm again:
The classes that I edited (other than changing import statements)
are Lexer, CPreprocessor, MacroExpander, MacroDefinitionParser,
IToken, Token, TokenForDigraph, and TokenWithImage. (I think that's
all of them...) I made the classes CppHelper and TokenTypeTranslator
from scratch.

In each of the classes that I edited, I added a comment right after the
cdt copyright notice. This comment starts by saying something like
"Class edited by Matthew Michelotti." After that I give an overview of the
changes that I made to the class, in roughly chronological order. I
also usually put a comment like "//added by MM" or "//edited by MM"
(or something like that) after each line that I changed in the file.

There are a number of functions and even classes that I copied
over which don't seem to be used. For example, I don't think
MultiMacroExpansionExplorer is called, nor MacroExpansionStep nor
MacroExpansionTracker. Eventually, we may want to get rid of these
if they are not used.

In case you were interested, looked up all of the external class
references that said "discouraged access" on them. They are:
org.eclipse.cdt.internal.core.dom.Linkage
org.eclipse.cdt.internal.core.dom.parser.ASTNode
org.eclipse.cdt.internal.core.dom.parser.ASTProblem
org.eclipse.cdt.internal.core.dom.parser.ASTNodeSpecification
I don't think any of these are really integral to the functionality.
