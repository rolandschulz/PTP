          CHARACTER (80) :: LINE

          LEVEL = 0
          SCAN_LINE: DO I = 1, 80
             CHECK_PARENS: SELECT CASE (LINE (I:I))
             CASE ('(')
                LEVEL = LEVEL + 1
             CASE (')')
                LEVEL = LEVEL - 1
                IF (LEVEL < 0) THEN
                   PRINT *, 'UNEXPECTED RIGHT PARENTHESIS'
                   EXIT SCAN_LINE
                END IF
             CASE DEFAULT
                ! Ignore all other characters
             END SELECT CHECK_PARENS
          END DO SCAN_LINE
          IF (LEVEL > 0) THEN
             PRINT *, 'MISSING RIGHT PARENTHESIS'
          END IF
ENd
