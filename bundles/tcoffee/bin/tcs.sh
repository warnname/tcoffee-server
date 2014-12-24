#!/bin/bash
set -e
set -u

declare -a args=()

export ALN_LINE_LENGTH=${ALN_LINE_LENGTH:-80}

FILTER_TYPE=column
FILTER_MIN=0
FILTER_MAX=9
FILTER_GAP=no
FILTER_OUT=''

filter_opts() {
  case $1 in
  --filter-type) FILTER_TYPE=$2;; 
  --filter-min)  FILTER_MIN=$2;;
  --filter-max)  FILTER_MAX=$2;;
  --filter-gap)  FILTER_GAP=$2;; 
  esac 
}

#
# Pre-process the command line to remove --filter-xxx options from t-coffee command line 
# 
while [[ "$*" != "" ]]; do
  case $1 in 
    -infile) 
    args+=("$1")
    INPUT_FILE=$2;
    ;;
    --filter*) 
    filter_opts $1 $2 
    shift
    ;;
  
    *) 
    args+=("$1")
    ;;
  esac
  shift;
done

# clean previous result
rm -f result.*

#
# Run T-Coffee
#    
t_coffee "${args[@]}"

# log the executed command 
echo t_coffee "${args[@]}" > .tcs.log

#
# Apply the filter
# 
ACTION=""

# use consensus (if columns is selected)
[[ $FILTER_TYPE == column ]] && ACTION="$ACTION +use_consensus"

# filter 
if [[ $FILTER_MIN == $FILTER_MAX ]]; then
  ACTION="$ACTION +keep $FILTER_MIN"
else 
  ACTION="$ACTION +keep [$FILTER_MIN-$FILTER_MAX]" 
fi   

# remove gap 
[[ $FILTER_GAP == yes ]] && ACTION="$ACTION +rm_gap 100"

declare -a outs=(clustalw_aln fasta_aln phylip)
REFORMAT="-other_pg seq_reformat -in $INPUT_FILE -struc_in result.score_ascii -struc_in_f number_aln -action$ACTION"

# Apply the filter for each output format 
for x in "${outs[@]}"; do 
  [[ ! -f result.$x ]] && continue
  echo "t_coffee $REFORMAT -output $x > filtered.$x" >> .tcs.log
  t_coffee $REFORMAT -output $x > filtered.$x
done 

# Rename the formatted output file to the standard name
for x in "${outs[@]}"; do
  [[ ! -f result.$x ]] && continue
  mv result.$x bak_result.$x 
  mv filtered.$x result.$x
done 

