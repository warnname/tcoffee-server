#!/bin/bash

echo '' >> run.sh
echo 'result=$?' >> run.sh
echo 'echo -n "$result" > _errcode' >> run.sh
echo 'exit $result' >> run.sh
trap "{ echo -n "255" > _errcode; }" TERM

qsub -cwd -q web -o pipeline.log -j y -l h_rt=72:00:00 $@

[ $? -ne 0 ] && exit $?

while [ true ]; do 
  [ -f _errcode ] && exit `cat _errcode`
  sleep 10
done 


