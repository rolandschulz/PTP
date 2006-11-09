z=A		! level-1-expr: A is a primary
z=B ** C	! mult-operand: B is a level-1-expr, ** is a power-op, and C is a mult-operand
z=D * E		! add-operand: D is an add-operand, * is a mult-op, and E is a mult-operand
z=+1		! level-2-expr: + is an add-op and 1 is an add-operand
z=F - I		! level-2-expr: F is a level-2-expr, - is an add-op, and I is and add-operand
z=- A + D * E + B ** C

END
