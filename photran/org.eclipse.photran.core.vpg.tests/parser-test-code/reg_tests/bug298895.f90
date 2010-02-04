iunit=20
convert=17.0
open(iunit, file=filename, form=form, status=status, convert='BIG_ENDIAN', recl=65534, err=100)
print *, convert
end program test
