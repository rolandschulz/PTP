#!/bin/sh
# Use this if you have problems running the Ant build script (build.xml)
pdflatex dev-guide-general.ltx
pdflatex dev-guide-specialized.ltx
pdflatex cvs-instructions.ltx
rm -rf *.aux *.log *.toc *.dvi *.ps *.out
