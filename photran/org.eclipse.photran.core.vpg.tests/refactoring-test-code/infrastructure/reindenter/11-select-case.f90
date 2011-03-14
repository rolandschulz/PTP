program p !<<<<<START
read *, i
SELECT CASE (i)
CASE (1)
print *, "One"
CASE (2)
print *, "Two"
CASE (3)
print *, "Three"
CASE DEFAULT
print *, "Not one, two, or three"
END SELECT
end program !<<<<<END
