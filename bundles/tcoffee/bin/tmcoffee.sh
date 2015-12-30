#!/bin/bash
set -e
set -u

declare -a args=()

export ALN_LINE_LENGTH=${ALN_LINE_LENGTH:-80}

SEARCH_DB=uniref50-TM
SEARCH_TYPE=normal
SEARCH_OUT=''

function db2name() {
  case "$1" in
  'UniRef50 -- Very Fast/Rough')  SEARCH_DB=uniref50-TM;;
  'UniRef90 -- Fast/Approximate')  SEARCH_DB=uniref90-TM;;
  'UniRef100 -- Slow/Accurate') SEARCH_DB=uniref100-TM;;
  'UniProt -- Slow/Accurate')   SEARCH_DB=uniprot;;
  'NCBI-NR -- Slow/Accurate')   SEARCH_DB=nr;;
  esac
}

#
# Pre-process the command line to remove --filter-xxx options from t-coffee command line 
# 
while [[ "$*" != "" ]]; do
  case $1 in 
  --search-db) 
    db2name "$2"
    shift
    ;;
    
  --search-type)
    SEARCH_TYPE="$2"
    shift
    ;;
 
  --search-out)
    SEARCH_OUT="$2"
    shift
    ;;
 
    *) 
    args+=("$1")
    ;;
  esac
  shift;
done

# add custom params 
args+=("-protein_db") 
args+=("$SEARCH_DB")

args+=("-template_file") 
if [[ $SEARCH_TYPE == transmembrane ]]; then 
  args+=("blast,PSITM") 
  SEARCH_OUT+=" tm_html"
else 
  args+=("blast") 
  [[ $SEARCH_OUT != *"score_html"* ]] && SEARCH_OUT+=" score_html"
fi
  
args+=("-output")
args+=("$SEARCH_OUT")

# clean previous result
rm -f result.*

# log the executed command 
echo t_coffee "${args[@]}" > .tmcoffee.log

#
# Run T-Coffee
#    
t_coffee "${args[@]}"
