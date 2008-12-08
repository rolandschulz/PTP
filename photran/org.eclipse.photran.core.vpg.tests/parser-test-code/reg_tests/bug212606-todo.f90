USE SIZES; USE GLOBAL, ONLY: C3D, COMM; USE GLOBAL_IO


type (OutputDataDescript_t) :: descript
CHARACTER(LEN=4) :: type1
CHARACTER(LEN=4) :: type2
integer :: ns,type

type1     = type2
!type = 2   ! This line still has problems

     READ(IHOT,REC=IHOTSTP) IMHS         ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) TIME         ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) ITHS         ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) NP_G_IN      ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) NE_G_IN      ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) NP_A_IN      ; IHOTSTP = IHOTSTP + 1
     READ(IHOT,REC=IHOTSTP) NE_A_IN      ; IHOTSTP = IHOTSTP + 1

end
