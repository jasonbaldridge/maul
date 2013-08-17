#!/bin/bash

EXIT_CODE=0

if [ -z $JAVA_MEM_FLAG ] 
then
    JAVA_MEM_FLAG=-Xmx8g
fi

MANAGED_JARS="`find ./lib_managed -name '*.jar' -print | tr '\n' ':'`"

SCALA_LIB="$HOME/.sbt/boot/scala-2.10.1/lib/scala-library.jar"

CP="target/scala-2.10/classes:$SCALA_LIB:src/main/resources:$MANAGED_JARS"

JAVA="$JAVA_HOME/bin/java"
JAVA_COMMAND="$JAVA $JAVA_MEM_FLAG -classpath $CP"

CMD=$1
shift

help()
{
cat <<EOF
commands: 

  factorie-lda           run Factorie's LDA
  mallet-lda             run Mallet's LDA  

Include --help with any option for more information
EOF
}

CLASS=

case $CMD in
    factorie-lda) CLASS=FactorieLda;;
    mallet-lda) CLASS=MalletLda;;
    help) help; exit 1;;
    *) echo "Unrecognized command: $CMD"; help; exit 1;;
esac

$JAVA_COMMAND $CLASS ${1+"$@"} 
(( EXIT_CODE += $? ))

exit $EXIT_CODE

