include ../makefile.define

ifeq ($(OS), AIX)
    CPPFLAGS = $(MY_FLAGS) -I../libsci -qmkshrobj
    LIBRARY = libsci_r.o
    LIBRARY64 = libsci64_r.o
else
    CPPFLAGS = $(MY_FLAGS) -I../libsci -fPIC -shared
    ifeq ($(ARCH), i686)
        LIBRARY = libsci.so.32
        LIBRARY64 = 
    else
        LIBRARY = libsci.so.32
        LIBRARY64 = libsci.so.64
    endif
endif

all : $(LIBRARY) $(LIBRARY64)

$(LIBRARY) : $(OBJS) ../common/libcmn.a
	@$(CC32) $(CPPFLAGS) -o $(LIBRARY) $(OBJS) -L../common -lcmn -lrt
ifeq ($(OS), AIX)
	rm -f libsci_r.a
	@$(AR) $(ARFLAGS) libsci_r.a $(LIBRARY)
endif

$(LIBRARY64) : $(OBJS64) ../common/libcmn64.a
	@$(CC64) $(CPPFLAGS) -o $(LIBRARY64) $(OBJS64) -L../common -lcmn64 -lrt 
ifeq ($(OS), AIX)
	@$(AR) $(AR64FLAGS) libsci_r.a $(LIBRARY64)
endif

install : $(LIBRARY) $(LIBRARY64)
ifeq ($(OS), AIX)
	cp -p libsci_r.a $(LIB_DIR)
	chmod 755 $(LIB_DIR)/libsci_r.a
	ln -sf $(LIB_DIR)/libsci_r.a /usr/lib/libsci_r.a
else
	cp -p $(LIBRARY) $(LIB_DIR)
	chmod 775 $(LIB_DIR)/$(LIBRARY)
	ln -sf $(LIB_DIR)/$(LIBRARY) /usr/lib/libsci.so
ifneq ($(ARCH), i686)
	cp -p $(LIBRARY64) $(LIB_DIR)
	chmod 775 $(LIB_DIR)/$(LIBRARY64)
	ln -sf $(LIB_DIR)/$(LIBRARY64) /usr/lib64/libsci.so
endif
endif

clean :
	rm -f $(LIBRARY) $(LIBRARY64) libsci_r.a $(OBJS) $(OBJS64) core *.d

