PROGRAM TstFmts

!   This program contains snippets of code that may cause the photran
!   outline parser grief.  The program provides no useful function.

    IMPLICIT NONE

          WRITE (6, 148)
          WRITE (6, 149)
          WRITE (6, 150)

148       FORMAT (/ ' *TestForBackup*  Backup due to too large a fractional ', 'change in core power.' / )
149       FORMAT ( / ' *TestForBackup*  Backup due to too large a fractional ', 'change in core power.' /)
150       FORMAT ( / ' *TestForBackup*  Backup due to too large a fractional change in core power.', // )

         ictl(1:6) = ( / ictl1, ictl2, 1, 0, 0, 0 / )
         ictl(1:6) = (/ ictl1, ictl2, 1, 0, 0, 0 /)


data radii/(12*(fcasts+1))*0/

       data radii / (12*(fcasts+1))*0 /


END PROGRAM
