include ../makefile.define

ifeq ($(OS), AIX)
    CPPFLAGS = $(MY_FLAGS) -I../libsci
    PROG = scidv1
else
    CPPFLAGS = $(MY_FLAGS) -I../libsci -fPIC
    ifeq ($(ARCH), i686)
        PROG = scidv1
    else
        PROG = scidv1
    endif
endif

all : $(PROG)

$(PROG) : $(OBJS) ../common/libcmn.a
	@$(CC32) $(CPPFLAGS) -o $(PROG) $(OBJS) -L../common -lcmn -lrt

install : $(PROG)
ifeq ($(OS), AIX)
	stopsrc -g sci
	sleep 1
	cp -p $(PROG) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG)
	ln -sf $(BIN_DIR)/$(PROG) /usr/sbin/$(PROG)
	startsrc -g sci
	sleep 1
else
	/etc/init.d/sciv1 stop
	cp -p $(PROG) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG)
	ln -sf $(BIN_DIR)/$(PROG) /usr/sbin/$(PROG)
	cp sciv1 /etc/init.d
	chmod 775 /etc/init.d/sciv1
	/etc/init.d/sciv1 start
endif

clean :
	rm -f $(PROG) $(OBJS) core *.d

