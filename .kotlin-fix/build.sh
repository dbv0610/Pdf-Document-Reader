#!/bin/bash
# Build android_document, split Kotlin errors per group (first matching prefix wins; G11 = everything else)
S="$(cd "$(dirname "$0")" && pwd)"
cd /Volumes/Data/Android/Pdf-Document-Reader
./gradlew :android_document:compileDebugKotlin --continue -q > $S/build.tmp 2>&1
grep -E "^e: " $S/build.tmp | sed 's#file:///Volumes/Data/Android/Pdf-Document-Reader/android_document/src/main/java/com/wxiwei/office/##' > $S/all_errors.tmp
awk -v dir="$S/errors" '
BEGIN{ while((getline l < "'$S'/groups.txt")>0){ n=split(l,a," "); g[++ng]=a[1]; np[ng]=n-1; for(i=2;i<=n;i++) p[ng,i-1]=a[i]; out[a[1]]="" } }
{ f=$2; hit="G11";
  for(k=1;k<=ng&&hit=="G11";k++) for(i=1;i<=np[k];i++) if(index(f,p[k,i])==1){hit=g[k];break}
  out[hit]=out[hit] $0 "\n" }
END{ for(x in out){ fn=dir"/"x".txt"; printf "%s", out[x] > fn; close(fn) } }' $S/all_errors.tmp
mv $S/build.tmp $S/build_last.log
echo "$(date +%T) total=$(wc -l < $S/all_errors.tmp)" >> $S/build_history.txt
for f in $S/errors/*.txt; do printf "%s=%s " $(basename $f .txt) $(wc -l < $f); done >> $S/build_history.txt; echo >> $S/build_history.txt
