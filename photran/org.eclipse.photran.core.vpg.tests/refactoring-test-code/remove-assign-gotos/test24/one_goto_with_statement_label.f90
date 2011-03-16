! Test Case 24 with one of the assigned goto has a statement label.
! Test passes to include the statement label at the start of the select case section.
program one_goto_with_stmt_label !<<<<< 1, 1, 17, 31, true, pass
implicit none
integer labelinaction
integer anotherlabel

assign 20 to labelinaction
goto labelinaction

assign 30 to anotherlabel

write (6, labelinaction)
20  format("6xTrying specify this line with label")

30  goto anotherlabel
end program one_goto_with_stmt_label