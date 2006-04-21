

void computeFunction(double [][] value, int m, int n)
{
  int i,j,maxiter,x,y;
  double total_iters=0;
#pragma omp parallel for private(j, x, y) reduction(+:total_iters)
  for(i=0; i<m; i++)   {
  	for(j=0; j<n; j++) {
  		x = i/(double)m;
  		y = j/(double)n;
  		value[i,j] = mandel_val(x,y,maxiter);
  		total_iters = total_iters+value[i,j];
  	}
  }
  
  #pragma omp sections
  {
  	#pragma omp section
    {  i=total_iters; }
  	
  	#pragma omp section
  	{  j=total_iters+m*n; }
  }
}

