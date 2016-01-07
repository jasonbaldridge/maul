#!/bin/bash

EXIT_CODE=0
MAUL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z $JAVA_MEM_FLAG ] 
then
    JAVA_MEM_FLAG=-Xmx8g
fi

MANAGED_JARS="`find $MAUL_DIR/lib_managed -name '*.jar' -print | tr '\n' ':'`"

SCALA_LIB="$HOME/.sbt/boot/scala-2.10.4/lib/scala-library.jar"

CP="$MAUL_DIR/target/scala-2.10/classes:$SCALA_LIB:src/main/resources:$MANAGED_JARS"

JAVA=`which java`
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
    factorie-lda) CLASS=maul.topics.FactorieLda;;
    mallet-lda) CLASS=maul.topics.MalletLda;;
    run) CLASS=$1; shift;;
    help) help; exit 1;;
    *) echo "Unrecognized command: $CMD"; help; exit 1;;
esac

$JAVA_COMMAND $CLASS ${1+"$@"} 
(( EXIT_CODE += $? ))

exit $EXIT_CODE

