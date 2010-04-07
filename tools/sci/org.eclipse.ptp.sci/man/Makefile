# Makefile for MMSC common library
include ../makefile.define
#=========================== DISPLAY SETTING ===================================
esc	=	\033[
fore 	= 	33
back	=	41

#light:1m non-light:m
light	=	1m

#			echo "${esc}${back};${fore};${light}making all in $$i${esc}m";
#			echo "${esc}${back};${fore};${light}installing all in $$i${esc}m";
#			echo "${esc}${back};${fore};${light}cleaning all in $$i${esc}m";
#			echo "${esc}${back};${fore};${light}verycleaningall in $$i${esc}m";
#================================================================================
ifeq ($(OS), AIX)
SUBDIRS =	aix
else
SUBDIRS =	linux
endif

all:
	@for i in $(SUBDIRS); do\
	    echo -e "${esc}0;37;44;1mmaking ${esc}0;32;43;1m all ${esc}0;33;41;1m in $$i ${esc}0m";\
	    (cd $$i && $(MAKE) all) || exit 1; \
	done

install:
	@for i in $(SUBDIRS); do \
	    echo -e "${esc}0;37;44;1mmaking ${esc}0;32;43;1m installing ${esc}0;33;41;1m in $$i ${esc}0m";\
	    (cd $$i && $(MAKE) install) || exit 1; \
	done

clean:
	@for i in $(SUBDIRS); do \
	    echo -e "${esc}0;37;44;1mmaking ${esc}0;32;43;1m cleaning ${esc}0;33;41;1m in $$i ${esc}0m";\
	    (cd $$i && $(MAKE) clean) || exit 1;\
	done

