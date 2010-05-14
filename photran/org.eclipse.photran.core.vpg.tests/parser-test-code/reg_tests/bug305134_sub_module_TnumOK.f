      MODULE mod_Tnum
      USE mod_system
      USE mod_dnSVM
      IMPLICIT NONE
        TYPE Type_BFTransfo
          !----------------------------------------------------------------
          logical                     :: init0,notinit0
          logical                     :: allo ! If allocated => T

          integer :: nb_vect,nb_BF
          integer :: nb_var,nb_var_Rot
          integer :: num_Frame
          character (len=Name_len) :: name_Frame

          logical :: Frame

          integer, pointer                  :: type_Q(:)
          character (len=Name_len), pointer :: name_Q(:)



          TYPE (Type_BFTransfo), pointer :: tab_BFTransfo(:) ! dim: nb_vect or nb_BF

        END TYPE Type_BFTransfo
      CONTAINS
c=======================================================================
c
c     Read BF transfo: BF and vectors
c
c=======================================================================
      SUBROUTINE init0_BFTransfo(BFTransfo)
        TYPE (Type_BFTransfo),intent(out) :: BFTransfo

        BFTransfo%init0     = .TRUE.
        BFTransfo%notinit0  = .FALSE.

        BFTransfo%allo      = .FALSE.

        BFTransfo%nb_vect    = 0
        BFTransfo%nb_BF      = 0
        BFTransfo%nb_var     = 0
        BFTransfo%nb_var_Rot = 0
        BFTransfo%num_Frame  = 0
        BFTransfo%name_Frame = "F0"

        BFTransfo%Frame      = .FALSE.

        nullify(BFTransfo%type_Q)
        nullify(BFTransfo%name_Q)
        nullify(BFTransfo%tab_BFTransfo)

      END SUBROUTINE init0_BFTransfo
      SUBROUTINE check_init0_BFTransfo(A,name_A,name_sub)
      TYPE (Type_BFTransfo), intent(in) :: A
      character (len=*), intent(in) :: name_A
      character (len=*), intent(in) :: name_sub

      IF ( (A%init0 .EQV. A%notinit0) .OR.
     *       (A%notinit0 .AND. .NOT. A%init0) ) THEN
        write(6,*) ' ERROR in ',name_sub
        write(6,*) name_A,
     *  ' has NOT been initiated with "init0_BFTransfo"'
        write(6,*) ' CHECK the source!!!!!'
        write(6,*) '.%init0, %notinit0',A%init0,A%notinit0
        STOP
      END IF
      END SUBROUTINE check_init0_BFTransfo
      RECURSIVE SUBROUTINE dealloc_BFTransfo(BFTransfo)
      TYPE (Type_BFTransfo),intent(inout) :: BFTransfo

      integer :: iv
      character (len=*), parameter :: name_sub='dealloc_BFTransfo'

      IF (.NOT. BFTransfo%Frame) RETURN
      write(6,*) 'BEGINNING ',name_sub

      CALL check_init0_BFTransfo(BFTransfo,'BFTransfo',name_sub)

      deallocate(BFTransfo%type_Q)
      deallocate(BFTransfo%name_Q)

      DO iv=1,BFTransfo%nb_vect
        CALL dealloc_BFTransfo(BFTransfo%tab_BFTransfo(iv))
      END DO

      deallocate(BFTransfo%tab_BFTransfo)

      BFTransfo%allo = .FALSE.
      CALL init0_BFTransfo(BFTransfo)

      write(6,*) 'END ',name_sub

      END SUBROUTINE dealloc_BFTransfo
      SUBROUTINE Read_BFTransfo(BFTransfo,Qtransfo,i_Q,
     *                          in_unitp,out_unitp)
!     RECURSIVE SUBROUTINE Read_BFTransfo(BFTransfo,Qtransfo,i_Q,
!    *                                    in_unitp,out_unitp)
      TYPE (Type_BFTransfo),intent(inout) :: BFTransfo
      TYPE (Type_Qtransfo), intent(inout) :: Qtransfo
      integer, intent(inout) :: i_Q
      integer :: in_unitp,out_unitp


      integer :: nb_vect,nb_var,iq,iv
      logical :: Frame,cos_th,cart
      character (len=Name_len) :: name_d,name_th,name_dih,
     *                            name_x,name_y,name_z
      character (len=Name_len) :: name_F,name_v

      NAMELIST /BF/ nb_vect
      NAMELIST /vector/ Frame,cos_th,name_d,name_th,name_dih,
     *                          cart,name_x,name_y,name_z

      character (len=*), parameter :: name_sub='Read_BFTransfo'


      IF (.NOT. BFTransfo%Frame) RETURN
c     write(6,*) 'BEGINNING ',name_sub

      CALL check_init0_BFTransfo(BFTransfo,'BFTransfo',name_sub)

      nb_vect = 0

      read(in_unitp,BF)
c     write(out_unitp,BF)

      IF (nb_vect < 1) THEN
        write(out_unitp,*) ' ERROR in ',name_sub
        write(out_unitp,*) ' the number of vector is < 1',nb_vect
        STOP
      END IF
      BFTransfo%nb_vect = nb_vect
      nb_var = max(1,3*nb_vect-3)
      IF (BFTransfo%num_Frame > 0) THEN
        nb_var = nb_var + 2
        IF (nb_vect > 1) nb_var = nb_var + 1
      END IF
      BFTransfo%nb_var = nb_var

      allocate(BFTransfo%type_Q(nb_var))
      allocate(BFTransfo%name_Q(nb_var))

      allocate(BFTransfo%tab_BFTransfo(nb_vect))
      BFTransfo%allo = .TRUE.

      write(name_F,*) BFTransfo%num_Frame
      name_F = "F" // trim(adjustl(name_F))
      IF (BFTransfo%num_Frame > 0) name_F =
     *     trim(adjustl(name_F)) // trim(adjustl(BFTransfo%name_Frame))

      BFTransfo%name_Frame = name_F

c     write(6,*) 'num_Frame,name_Frame',
c    *         BFTransfo%num_Frame,BFTransfo%name_Frame

      iq = 0
      DO iv=1,nb_vect
        Frame    = .FALSE.
        cos_th   = .TRUE.
        cart     = .FALSE.
        write(name_v,*) iv
        name_v = trim(adjustl(name_v)) // "_" // trim(adjustl(name_F))
        name_x   = "x" // trim(adjustl(name_v))
        name_y   = "y" // trim(adjustl(name_v))
        name_z   = "z" // trim(adjustl(name_v))
        name_d   = "d" // trim(adjustl(name_v))
        name_th  ="th" // trim(adjustl(name_v))
        name_dih="dih" // trim(adjustl(name_v))
        read(in_unitp,vector)
c       write(out_unitp,vector)

        CALL init0_BFTransfo(BFTransfo%tab_BFTransfo(iv))
        BFTransfo%tab_BFTransfo(iv)%num_Frame = iv
        BFTransfo%tab_BFTransfo(iv)%Frame = Frame
        BFTransfo%tab_BFTransfo(iv)%name_Frame = name_F
        IF (cart .AND. iv < 3) THEN
          write(out_unitp,*) ' ERROR in ',name_sub
          write(out_unitp,*) ' vector in cartesian and iv < 3',
     *       ' is not possible'
          write(out_unitp,*) 'cart,iv',cart,iv
          STOP
        END IF
        IF (cart) THEN
           iq = iq + 1
           BFTransfo%type_Q(iq) = 1 ! cart :x
           BFTransfo%name_Q(iq) = name_x
           i_Q = i_Q + 1
           Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
           Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
           iq = iq + 1
           BFTransfo%type_Q(iq) = 1 ! cart :y
           BFTransfo%name_Q(iq) = name_y
           i_Q = i_Q + 1
           Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
           Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
           iq = iq + 1
           BFTransfo%type_Q(iq) = 1 ! cart :z
           BFTransfo%name_Q(iq) = name_z
           i_Q = i_Q + 1
           Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
           Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
        ELSE
           iq = iq + 1
           BFTransfo%type_Q(iq) = 2 ! distance
           BFTransfo%name_Q(iq) = name_d
           i_Q = i_Q + 1
           Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
           Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
           IF (iv > 1) THEN
             iq = iq + 1
             BFTransfo%type_Q(iq) = 3 ! th
             IF (cos_th) BFTransfo%type_Q(iq) = -3 ! cos(th)
             BFTransfo%name_Q(iq) = name_th
             i_Q = i_Q + 1
             Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
             Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
           END IF
           IF (iv > 2) THEN
             iq = iq + 1
             BFTransfo%type_Q(iq) = 4 ! dih
             BFTransfo%name_Q(iq) = name_dih
             i_Q = i_Q + 1
             Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
             Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
           END IF
        END IF


        CALL Read_BFTransfo(BFTransfo%tab_BFTransfo(iv),Qtransfo,i_Q,
     *                      in_unitp,out_unitp)
      END DO

      IF (BFTransfo%num_Frame > 0) THEN
        iq = iq + 1
        BFTransfo%type_Q(iq) = 4
        BFTransfo%name_Q(iq) = "alpha_" // trim(adjustl(name_F))
        i_Q = i_Q + 1
        Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
        Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
        iq = iq + 1
        BFTransfo%type_Q(iq) = 3
        IF (cos_th) BFTransfo%type_Q(iq) = -3
        BFTransfo%name_Q(iq) = "beta_" // trim(adjustl(name_F))
        i_Q = i_Q + 1
        Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
        Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
        IF (nb_vect > 1) THEN
          iq = iq + 1
          BFTransfo%type_Q(iq) = 4
          BFTransfo%name_Q(iq) = "gamma_" // trim(adjustl(name_F))
          i_Q = i_Q + 1
          Qtransfo%type_Q(i_Q) = BFTransfo%type_Q(iq)
          Qtransfo%name_Q(i_Q) = BFTransfo%name_Q(iq)
        END IF
      END IF


c     write(out_unitp,*) 'END ',name_sub

      END SUBROUTINE Read_BFTransfo
       SUBROUTINE Write_BFTransfo(BFTransfo,out_unitp)
      !RECURSIVE SUBROUTINE Write_BFTransfo(BFTransfo,out_unitp)

      TYPE (Type_BFTransfo),intent(out) :: BFTransfo

      integer :: out_unitp

      integer :: iv,iq
      character (len=*), parameter :: name_sub='Write_BFTransfo'

      IF (.NOT. BFTransfo%Frame) RETURN
      write(out_unitp,*) 'BEGINNING ',name_sub

      CALL check_init0_BFTransfo(BFTransfo,'BFTransfo',name_sub)

      write(out_unitp,*) 'allo',BFTransfo%allo
      write(out_unitp,*) 'num_Frame',BFTransfo%num_Frame
      write(out_unitp,*) 'name_Frame: ',BFTransfo%name_Frame

      write(out_unitp,*) 'nb_vect,nb_BF',
     *                   BFTransfo%nb_vect,BFTransfo%nb_BF

      write(out_unitp,*) 'nb_var,nb_var_Rot',
     *                   BFTransfo%nb_var,BFTransfo%nb_var_Rot

      write(out_unitp,*) 'BF',BFTransfo%Frame


      write(out_unitp,*) 'type_Q',BFTransfo%type_Q(:)
      write(out_unitp,*) 'name_Q: ',
     *        (trim(BFTransfo%name_Q(iq))," ",iq=1,BFTransfo%nb_var)

      IF (BFTransfo%Frame) THEN
        DO iv=1,BFTransfo%nb_vect
          CALL Write_BFTransfo(BFTransfo%tab_BFTransfo(iv),out_unitp)
        END DO
      END IF

      write(6,*) 'END ',name_sub

      END SUBROUTINE Write_BFTransfo
      END MODULE mod_Tnum
