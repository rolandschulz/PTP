!     C.5.3       Examples of DO constructs

!     The following are all valid examples of block DO constructs.

!     Example 1:

             SUM = 0.0
             READ (IUN) N
             OUTER: DO L = 1, N           ! A DO with a construct name
                READ (IUN) IQUAL, M, ARRAY (1:M)
                IF (IQUAL < IQUAL_MIN) CYCLE OUTER    ! Skip inner loop
                INNER: DO 40 I = 1, M     ! A DO with a label and a name
                  CALL CALCULATE (ARRAY (I), RESULT)
                  IF (RESULT < 0.0) CYCLE
                  SUM = SUM + RESULT
                  IF (SUM > SUM_MAX) EXIT OUTER
         40    END DO INNER
            END DO OUTER
end

!     The outer loop has an iteration count of MAX (N, 0), and will execute that number of times or until
!     SUM exceeds SUM MAX, in which case the EXIT OUTER statement terminates both loops. The inner
!     loop is skipped by the first CYCLE statement if the quality flag, IQUAL, is too low. If CALCULATE
!     returns a negative RESULT, the second CYCLE statement prevents it from being summed. Both loops
!     have construct names and the inner loop also has a label. A construct name is required in the EXIT
!     statement in order to terminate both loops, but is optional in the CYCLE statements because each
!     belongs to its innermost loop.

!     Example 2:

subroutine example_2
            N=0
            DO 50, I = 1, 10
               J=I
               DO K = 1, 5
                  L=K
                  N = N + 1 ! This statement executes 50 times
               END DO        ! Nonlabeled DO inside a labeled DO
         50 CONTINUE
end

!     After execution of the above program fragment, I = 11, J = 10, K = 6, L = 5, and N = 50.

!     Example 3:

subroutine example_3
            N=0
            DO I = 1, 10
               J=I
               DO 60, K = 5, 1           ! This inner loop is never executed
                  L=K
                  N=N+1
         60    CONTINUE                  ! Labeled DO inside a nonlabeled DO
            END DO
end

!     After execution of the above program fragment, I = 11, J = 10, K = 5, N = 0, and L is not defined by
!     these statements.

!     The following are all valid examples of nonblock DO constructs:

!     Example 4:

subroutine example_4
           DO 70
               READ (IUN, '(1X, G14.7)', IOSTAT = IOS) X
               IF (IOS /= 0) EXIT
               IF (X < 0.) GOTO 70
               CALL SUBA (X)
               CALL SUBB (X)
               CALL SUBY (X)
               CYCLE
        70     CALL SUBNEG (X) ! SUBNEG called only when X < 0.
end

!   This is not a block DO construct because it ends with a statement other than END DO or CONTINUE. The loop will
!   continue to execute until an end-of-file condition or input/output error occurs.

!     Example 5:

subroutine example_5
         SUM = 0.0
         READ (IUN) N
         DO 80, L = 1, N
              READ (IUN) IQUAL, M, ARRAY (1:M)
              IF (IQUAL < IQUAL_MIN) M = 0 ! Skip inner loop
              DO 80 I = 1, M
                 CALL CALCULATE (ARRAY (I), RESULT)
                 IF (RESULT < 0.) CYCLE
                 SUM = SUM + RESULT
                 IF (SUM > SUM_MAX) GOTO 81
        80    CONTINUE ! This CONTINUE is shared by both loops
        81 CONTINUE
end

!   This example is similar to Example 1 above, except that the two loops are not block DO constructs because they share
!   the CONTINUE statement with the label 80. The terminal construct of the outer DO is the entire inner DO construct.
!   The inner loop is skipped by forcing M to zero. If SUM grows too large, both loops are terminated by branching to the
!   CONTINUE statement labeled 81. The CYCLE statement in the inner loop is used to skip negative values of RESULT.

!     Example 6:

subroutine example_6
         N=0
         DO 100 I = 1, 10
            J=I
            DO 100 K = 1, 5
               L=K
     100       N = N + 1 ! This statement executes 50 times
end

!   In this example, the two loops share an assignment statement. After execution of this program fragment, I = 11, J = 10,
!     K = 6, L = 5, and N = 50.

!     Example 7:

subroutine example_7
         N=0
         DO 200 I = 1, 10
            J=I
            DO 200 K = 5, 1      ! This inner loop is never executed
               L=K
     200       N=N+1
end

!   This example is very similar to the previous one, except that the inner loop is never executed. After execution of this
!    program fragment, I = 11, J = 10, K = 5, N = 0, and L is not defined by these statements.
