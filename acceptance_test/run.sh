#!/bin/bash

rm test_data/output.txt
docker build . --target app -t funds-loader:latest && \
docker run -v $PWD/acceptance_test/test_data:/data funds-loader:latest /data/input.txt /data/output.txt

if diff -q "acceptance_test/test_data/expected_output.txt" "acceptance_test/test_data/output.txt" >/dev/null; then
  echo "Success"
  exit 0
else
  echo "Failure. Expected vs Actual:"
  diff "acceptance_test/test_data/expected_output.txt" "acceptance_test/test_data/output.txt"
  exit 1
fi