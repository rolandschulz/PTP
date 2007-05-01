!R1101 no program-stmt, no specification-part, no execution-part, no internal-subprogram-part
end

!R1101 no program-stmt, no specification-part, has execution-part, no internal-subprogram-part
stop
end

!R1101 no program-stmt, no specification-part, no execution-part, has internal-subprogram-part
contains
  subroutine foo
  end subroutine
end

!R1101 has program-stmt, no specification-part, no execution-part, no internal-subprogram-part
program main
end


