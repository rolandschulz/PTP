all: *.tex
	pdflatex dev-guide.tex
	pdflatex dev-guide.tex
	pdflatex cvs-instructions.tex
	rm -f dev-guide.aux  dev-guide.log  dev-guide.toc  dev-guide.dvi  dev-guide.ps
	rm -f cvs-instructions.aux  cvs-instructions.log  cvs-instructions.toc  cvs-instructions.dvi  cvs-instructions.ps
	if [ `echo $$HOSTNAME` == "mightymouse.cs.uiuc.edu" -a `echo $$LOGNAME` == "joverbey" ]; then rm -rf ~/public_html/docs/photran/*; cp *.pdf ~/public_html/docs/photran/; fi
	#if [ `echo $$LOGNAME` == "joverbey" ]; then acroread dev-guide.pdf; fi

clean:
	rm -f dev-guide.aux  dev-guide.log  dev-guide.toc  dev-guide.dvi  dev-guide.ps  dev-guide.pdf
	rm -f cvs-instructions.aux  cvs-instructions.log  cvs-instructions.toc  cvs-instructions.dvi  cvs-instructions.ps  cvs-instructions.pdf
	