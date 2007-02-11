     PA => PB

     PB => D

     ALLOCATE (PB)

     DEALLOCATE (PB)

end

subroutine inserted

     REAL, TARGET :: T
     REAL, POINTER :: P

     P=> T

     DEALLOCATE (P) ! Not allowed: P's target was not allocated
end
