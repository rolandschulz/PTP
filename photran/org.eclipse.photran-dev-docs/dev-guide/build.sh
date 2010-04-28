#!/bin/sh
# Use this if you have problems running the Ant build script (build.xml)
pdflatex dev-guide.ltx
pdflatex dev-guide.ltx
pdflatex cvs-instructions.ltx
rm -rf *.aux *.log *.toc *.dvi *.ps *.out
