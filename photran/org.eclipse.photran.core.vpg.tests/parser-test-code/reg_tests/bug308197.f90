      SUBROUTINE Get_Geom( IDEBUG, ROOTNAME, NC, COORD,                  &
     &                     NEIGHB1, NBBOUND,                             &
     &                     NHEXA, NPENTA, NHEPTA,                        &
     &                     IHEXA ,IPENTA, IHEPTA,                        &
     &                     NEIGHB2_OUT, NPOINT_OUT )
!--------------------------------------------------------------------
!  Function : this program builds several connectivity arrays :
! NEIGHB1(J,I) indices of Jth nearest neighbours of atom I
! NBBOUND(I) : Number of nearest neighbors of atom I
! optionally
! NEIGHB2_OUT(1..6,I) : indices of the secound nearest neighbours
! NPOINT_OUT(1..6,I : indices of the atoms between atoms I and NEIGHB2(J,I)
! and optionally writes several files to be used
! by an external program to display fullerene or nanotube molecules
!      - geom.xxx contains the indices of the nearest neighbours and
!                 the coordinates of the atoms of the fullerene
!      - hexagons.xxx contains the labels of the carbon atoms constituting
!                 the hexagons (1 line per hexagon)
!      - pentagons.xxx contains the labels of the carbon atoms constituting
!                 the pentagons (1 line per pentagon)
!      - heptagons.xxx contains the labels of the carbon atoms constituting
!                 the heptagons (1 line per heptagon)
!
! The problem here is that the tube is not necessarily closed !
! -> some carbon atoms only have 2 neighbours
!
! The files are needed by a Mathematica script
! such as showtub.m to display the molecules.
!
!  Date     : 06-06-1997
!  Author   : Michel Devel
!  Status   : Ready to run
!
!  Changes  : 23-11-98 : (MD) add automatic naming of all the files
!             25-02-04 : (MD) adapt geom90 as a subroutine
!--------------------------------------------------------------------
      use kinds

      IMPLICIT NONE

      Integer, intent(in) :: IDEBUG
      Character(LEN=*), intent(in) :: ROOTNAME
      Integer, intent(in) :: NC
      Real(kind=r_), intent(in) :: COORD(3,NC)

      Integer, intent(out) :: NEIGHB1(3,NC), NBBOUND(NC)
! NEIGHB1(1..3,I) : indices of the nearest neigbours of atom nb I
      Integer, intent(out) :: NHEXA, NPENTA, NHEPTA
      Integer, intent(out) :: IHEPTA(7,NC),IHEXA(6,NC),IPENTA(5,NC)

      Integer, intent(out), optional :: NEIGHB2_OUT(6,NC)
! NEIGHB2_OUT(1..6,I) : indices of the secound nearest neighbours
      Integer, intent(out), optional :: NPOINT_OUT(6,NC)
! NPOINT_OUT(1..6,I : indices of the atoms between atoms I and NEIGHB2(J,I)

      Real(kind=r_),parameter :: DISTMIN=1._r_, DISTMAX=2._r_

      INTEGER :: I, J, K, L, M, N, I1, I2, I3
      INTEGER :: IND, IND1, IND2, NIND1, NIND2, NPO1, NPO2, NVOIS
      Integer :: NEIGHB2(6,NC), NPOINT(6,NC)


      Real(kind=r_) :: DISTIJ, D12, D13, D23

      Real*8,external :: Norm
!
!------------------------------------------------------------------
! Find indices of the 3 nearest neighbours for each carbon atom
      NBBOUND = 0
      NEIGHB1 = 0

      DO I=1,NC
         DO J=I+1,NC
            DISTIJ = NORM( COORD(:,I)-COORD(:,J) )
!           PRINT *,I,J,SQRT(DISTIJ2)
            IF (DISTIJ.GT.DISTMIN.AND.DISTIJ.LT.DISTMAX) THEN
               IF (NBBOUND(I).GE.3.OR.NBBOUND(J).GE.3) THEN
                  PRINT *, "ERROR in FINDBOUND : more than 3 bounds"
                  PRINT *,"I,NBBOUND(I),J,NBBOUND(J)"
                  PRINT *,I,NBBOUND(I),J,NBBOUND(J)
                  STOP
               ENDIF
               NBBOUND(I)=NBBOUND(I)+1
               NBBOUND(J)=NBBOUND(J)+1
               NEIGHB1(NBBOUND(I),I)=J
               NEIGHB1(NBBOUND(J),J)=I
!              PRINT *,I,J,DISTIJ
            ENDIF
         END DO
      END DO

      IF ( IDEBUG < 1 .AND. .NOT. PRESENT(NEIGHB2_OUT) ) GO TO 9999

      IF (IDEBUG.GE.3) THEN
         DO I=1,NC
          I1=NEIGHB1(1,I)
          I2=NEIGHB1(2,I)
          I3=NEIGHB1(3,I)
          PRINT *,I,I1,I2,I3
          D12=NORM( COORD(:,I1) - COORD(:,I2) )
          D13=NORM( COORD(:,I1) - COORD(:,I3) )
          D23=NORM( COORD(:,I2) - COORD(:,I3) )
          IF (IDEBUG.GE.2) PRINT *, D12, D13, D23
         END DO
      ENDIF
!
! find the second nearest neighbours of atom i : they are
! the nearest neighbours of the nearest neighbours of i except i.
      DO I=1,NC
         NVOIS=0
         DO J=1,3
            IND=NEIGHB1(J,I)
            IF (IND.EQ.0) CYCLE
            DO K=1,3
               IF (NEIGHB1(K,IND).NE.I) THEN
                  NVOIS = NVOIS + 1
                  IF (NVOIS.GT.6) THEN
                     PRINT *,'error : too many 2nd nearest neighbours'
                     PRINT *,'for atom nb :',I
                     PRINT *,(NEIGHB1(L,I),L=1,3)
                     PRINT *,(NEIGHB2(L,I),L=1,6)
                     PRINT *,(NPOINT(L,I),L=1,6)
                     PRINT *,NVOIS
                  END IF
                  NEIGHB2(NVOIS,I)=NEIGHB1(K,IND)
                  NPOINT(NVOIS,I)=IND
               END IF
            END DO
         END DO
         DO J=NVOIS+1,6
            NEIGHB2(J,I)=0
            NPOINT(J,I)=0
         END DO
      END DO

      IF ( PRESENT(NEIGHB2_OUT) ) NEIGHB2_OUT = NEIGHB2
      IF ( PRESENT(NPOINT_OUT) ) NPOINT_OUT = NPOINT
!
! store coordinates
      OPEN(UNIT=1,FILE="geom." // TRIM(ROOTNAME) )
      IF (IDEBUG.GE.2) OPEN(UNIT=2,FILE="neighb2." // TRIM(ROOTNAME) )
      DO 150 I=1,NC
         WRITE(1,1070) NEIGHB1(1,I),NEIGHB1(2,I),NEIGHB1(3,I),COORD(:,I)
 1070    FORMAT(3I5,3F12.3)
       IF (IDEBUG.GE.2) WRITE(2,*) (NEIGHB2(J,I),J=1,6),  &
     &                               (NPOINT(J,I),J=1,6)
 150  CONTINUE
      PRINT *, "file geom." // TRIM(ROOTNAME) // " written with ",       &
     &            NC, " atoms"
      CLOSE(1)
      IF (IDEBUG.GE.2) CLOSE(2)
!
! Try to find the indices of the atoms composing the same
! pentagon or the same hexagon or the same heptagon
      NHEXA=0
      NPENTA=0
      NHEPTA=0
      DO 200 I=1,NC
         DO 210 J=1,6
! loop on the second nearest neighbours of atom i
! ind1 is the index of the j th second nearest neighbour of i
          IND1=NEIGHB2(J,I)
          IF (IND1.EQ.0) GOTO 210
! npo1 is the index of the atom which is between atoms i and ind1
          NPO1=NPOINT(J,I)
! if ind1 < i or npo1 < i then the polygon must have already been found
          IF (IND1.LE.I.OR.NPO1.LE.I) GOTO 210
            DO 220 K=J+1,6
! find between the other second nearest neighbours of atom i
! which one is also a second nearest neighbour of atom ind1 without
! having a nearest neighbour in common with atom ind1
             IND2=NEIGHB2(K,I)
             IF (IND2.EQ.0) GOTO 220
! npo2 is the index of the atom which is between atoms i and ind2
             NPO2=NPOINT(K,I)
!            PRINT *,'I,IND1,NPO1,IND2,NPO2',I,IND1,NPO1,IND2,NPO2
! if ind2 < i or npo2 < i then the polygon must have already been found
! if npo1=npo2 ind1,ind2 and i have a common nearest neighbour ->
! they belong to different polygons
             IF (IND2.LE.I.OR.NPO2.LE.I.OR.NPO1.EQ.NPO2) GOTO 220
! first deal with all the possible cases to get a pentagon, because
! when a pentagon is near an heptagon, it can be mismatched for
! the heptagon
             IF (NEIGHB1(1,IND1).EQ.IND2.OR.                             &
     &           NEIGHB1(2,IND1).EQ.IND2.OR.                             &
     &           NEIGHB1(3,IND1).EQ.IND2) THEN
! We have found a pentagon ! store it !
                NPENTA=NPENTA+1
                  IPENTA(1,NPENTA)=I
                IPENTA(2,NPENTA)=NPO1
            IPENTA(3,NPENTA)=IND1
            IPENTA(4,NPENTA)=IND2
            IPENTA(5,NPENTA)=NPO2
! the secound nearest neighbours belong only to one polygon
! in common with i
            GOTO 210
             ENDIF
               DO 230 L=1,3
            NIND1=NEIGHB1(L,IND1)
            IF (NIND1.EQ.0) GOTO 230
            IF (NIND1.EQ.NPO1.OR.NIND1.LE.I)  GOTO 230
! if it is not a pentagon it must be an hexagon or an heptagon ->
!     is ind1 a second nearest neighbour to ind2 ?
            DO 240 N=1,3
               NIND2=NEIGHB1(N,IND2)
               IF (NIND2.EQ.0.OR.NIND2.EQ.NPO2) GOTO 240
!              PRINT *,'NIND1,NIND2',NIND1,NIND2
               IF (NIND2.EQ.NIND1) THEN
! We have completed an hexagon ! Find the indices of the members
! of this hexagon.
                        NHEXA=NHEXA+1
                        IHEXA(1,NHEXA)=I
                        IHEXA(2,NHEXA)=NPO1
                  IHEXA(3,NHEXA)=IND1
                  IHEXA(4,NHEXA)=NIND2
                  IHEXA(5,NHEXA)=IND2
                  IHEXA(6,NHEXA)=NPO2
                  GOTO 210
               ELSE
! May be an heptagon ? Is NIND1 a nearest neighbour to NIND2
                  DO M=1,3
                 IF (NEIGHB1(M,NIND1).EQ.NIND2) THEN
! It IS an heptagon ! store it
                              NHEPTA=NHEPTA+1
                    IHEPTA(1,NHEPTA)=I
                    IHEPTA(2,NHEPTA)=NPO1
                    IHEPTA(3,NHEPTA)=IND1
                    IHEPTA(4,NHEPTA)=NIND1
                    IHEPTA(5,NHEPTA)=NIND2
                    IHEPTA(6,NHEPTA)=IND2
                    IHEPTA(7,NHEPTA)=NPO2
                        GOTO 210
                           ENDIF
                        ENDDO
               ENDIF
 240              CONTINUE
 230           CONTINUE
 220        CONTINUE
 210     CONTINUE
 200  CONTINUE
!
      IF (IDEBUG.GE.1) PRINT *,'Hexagons :'
      IF ( NHEXA .GT. 0 ) THEN
         OPEN(UNIT=16,FILE="hexa." // TRIM(ROOTNAME) )
         DO I=1,NHEXA
          IF (IDEBUG.GE.1) PRINT *,(IHEXA(J,I),J=1,6)
          WRITE(16,*) (IHEXA(J,I),J=1,6)
         ENDDO
         CLOSE(16)
       PRINT *, "file hexa." // TRIM(ROOTNAME) // " written with ",      &
     &            NHEXA, " hexagons"
      END IF
      IF ( NPENTA .GT. 0 ) THEN
         IF (IDEBUG.GE.1) PRINT *,'Pentagons :'
         OPEN(UNIT=15,FILE="penta." // TRIM(ROOTNAME) )
         DO I=1,NPENTA
          IF (IDEBUG.GE.1) PRINT *,(IPENTA(J,I),J=1,5)
          WRITE(15,*) (IPENTA(J,I),J=1,5)
         ENDDO
         CLOSE(15)
       PRINT *, "file penta." // TRIM(ROOTNAME) // " written with ",     &
     &            NPENTA, " pentagons"
      END IF
      IF ( NHEPTA .GT. 0 ) THEN
         IF (IDEBUG.GE.1) PRINT *,'heptagons :'
         OPEN(UNIT=17,FILE="hepta." // TRIM(ROOTNAME) )
         DO I=1,NHEPTA
          IF (IDEBUG.GE.1) PRINT *,(IHEPTA(J,I),J=1,7)
          WRITE(17,*) (IHEPTA(J,I),J=1,7)
         ENDDO
         CLOSE(17)
       PRINT *, "file hepta." // TRIM(ROOTNAME) // " written with ",     &
     &            NHEPTA, " heptagons"
      END IF

      Contains

       Real(kind=8) Function Norm(VECT)
         use kinds

         IMPLICIT NONE

         Real(kind=r_),intent(in) :: VECT(:)
         NORM = SQRT( DOT_PRODUCT(VECT,VECT) )
      END Function NORM

 9999 END SUBROUTINE Get_geom
