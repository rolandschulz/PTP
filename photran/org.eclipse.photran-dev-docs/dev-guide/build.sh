#!/bin/sh
pdflatex dev-guide.ltx
pdflatex dev-guide.ltx
pdflatex cvs-instructions.ltx
rm -rf *.aux *.log *.toc *.dvi *.ps *.out
