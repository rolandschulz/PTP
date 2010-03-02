@ @@ @ @ @  @ @ @@  @

At signs are considered unknown tokens by the Lexer. The Lexer will
return unknown tokens like this with the proper image. However,
the CPreprocessor is designed (or was designed) to not return tokens
with an unknown type that it receives from the Lexer. If this is not
handled, at signs will be lost.


@@ @ @  @ @ @  @ @ @ @@  @

