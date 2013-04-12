#!/bin/bash
mkdir -p $TMP_4_TCOFFEE
mkdir -p $LOCKDIR_4_TCOFFEE
mkdir -p $CACHE_4_TCOFFEE
python $BIG_HOME/bigbigtree.py "$@"
if [ ! -e $DATA_PATH/result/trees/final.ph ]; then 
  exit 1
fi 
