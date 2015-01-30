#!/bin/sh
if [ "$#" -eq 0 ]; then
  echo 'usage: replace-version.sh <current-version> [<new-version>]'
  echo '       single argument will report which files will be changed'
fi
if [ "$#" -eq 1 ]; then
  echo searching for $1
  grep -r $1 --include="pom.xml" .
fi
if [ "$#" -eq 2 ]; then
  echo replacing $1 with $2...
  if [[ $OSTYPE == linux-gnu ]]; then
    find . -name pom.xml -type f -print0 | xargs -0 sed -i. -e "s/$1/$2/g"
  else 
    find . -name pom.xml -type f -print0 | xargs -0 sed -i . -e "s/$1/$2/g"
  fi
fi
