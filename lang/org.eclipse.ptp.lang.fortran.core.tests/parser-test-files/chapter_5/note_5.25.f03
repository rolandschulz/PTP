character (len = 10) name
integer, dimension (0:9) :: miles
real, dimension (100, 100) :: skew
type(node), pointer :: head_of_list
type(person) myname, yourname
data name / 'JOHN DOE' /, miles / 10 * 0 /
data ((skew(k,j), j=1, k), k=1, 100) / 5050 * 0.0 /
data head_of_list / NULL() /
data myname / person (21, 'JOHN SMITH') /
data yourname%age, yourname%name /35, 'FRED BROWN'/
end
