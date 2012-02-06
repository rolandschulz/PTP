int one_arg(int arg);

int main(int argc, char **argv) {
  struct { int a; };
  char *ptr = 1.2345;
  undefined_variable = 6;
  function_not_declared();
  one_arg(1, 2, 3);
}
