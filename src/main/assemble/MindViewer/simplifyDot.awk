#!/usr/bin/awk -f
#
# This script create simple dot graph representing the ADL, 
# based on the full dot graph representation.
#
# Typical Usage :
#
# for i in $(find ./build/ -name "*.dot");
#  do simplifyDot $i > $(dirname $i)"/Simple"$(basename $i);
# done 
#
# Will create a "MindViewer compatible simple dot graph set" 
# from a "MindViewer compatible full dot graph set"
#
{FS="->|:"}
BEGIN{ARGV[ARGC++]=ARGV[ARGC-1];color++;}
NR==FNR{if(/->/&&NF==4) te[$1"->"$3]++;next;}

!/->/{gsub("TopLevel","SimpleTopLevel");print}
/->/{for ( i in te ) if (($1"->"$3 == i)&&(te[$1"->"$3]!=0))  
{print i"[penwidth=" te[i]*5 " tailport=e headport=w colorscheme=\"paired12\" color="color"]"; te[$1"->"$3]=0;color=color%12+1}}
/->/&&NF<4{print}



