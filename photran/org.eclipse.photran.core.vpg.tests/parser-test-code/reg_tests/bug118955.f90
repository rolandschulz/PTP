IDENT = RESHAPE ( (/ (1, (0, II=1,N), JJ=1,N-1), 1 /), (/ N,N /) )


   a = reshape( (/ (1.0, (real(k), i=1,N), j=1,N), 1.0 /), &
      (/ N, N /) )

STOKES=DEN&
      &SITY-11.11

end
