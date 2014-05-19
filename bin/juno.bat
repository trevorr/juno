@echo off

rem support both source and binary jove distributions
if EXIST %JOVE_HOME%\bin (
    set JOVE_CLASSPATH=%JOVE_HOME%\bin\jove-ifgen.jar;%JOVE_HOME%\bin\newisys-utils.jar;%JOVE_HOME%\bin\langschema.jar;%JOVE_HOME%\bin\langschema-java.jar;%JOVE_HOME%\bin\langschema-jove.jar;%JOVE_HOME%\bin\jove.jar;%JOVE_HOME%\bin\randsolver.jar
) ELSE (
    set JOVE_CLASSPATH=%JOVE_HOME%\java\jove-ifgen\bin;%JOVE_HOME%\java\newisys-utils\bin;%JOVE_HOME%\java\langschema\bin;%JOVE_HOME%\java\langschema-java\bin;%JOVE_HOME%\java\langschema-jove\bin;%JOVE_HOME%\java\jove\bin;%JOVE_HOME%\java\randsolver\bin
)

set JUNO_CLASSPATH=%JUNO_HOME%\java\juno\bin;%JUNO_HOME%\java\juno-runtime\bin;%JUNO_HOME%\java\langschema-vera\bin;%JUNO_HOME%\java\langsource\bin;%JUNO_HOME%\java\make-parser\bin;%JUNO_HOME%\java\vera-parser\bin;%JUNO_HOME%\java\vlogdef\bin;

set CLASSPATH=%JOVE_CLASSPATH%;%JUNO_CLASSPATH%;%CLASSPATH%

set JAVA_OPTS="-ea"

java %JAVA_OPTS% -cp %CLASSPATH% com.newisys.juno.JunoTranslator %*
