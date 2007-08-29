#!/bin/sh
RESULT=`which pdflatex 2>/dev/null | grep -v "no pdflatex in"`
if [ "$RESULT" == "" ]; then
  OS=`uname -s`
  if [ "$OS" == "Darwin" ]; then
    HARDWARE=`uname -m`
    if [ "$HARDWARE" == "i386" ]; then
      RESULT=`find /usr/local/texlive -name pdflatex | grep i386`
    else
      RESULT=`find /usr/local/texlive -name pdflatex | grep ower`
    fi
  else
    RESULT=`find /usr/local/tex* -name pdflatex | head -1`
  fi
fi
if [ "$RESULT" == "" ]; then
  exit 1
else
  echo $RESULT
  exit 0
fi
