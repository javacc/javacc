mkdir temp
del /Q temp\*.*
copy /Y target\javacc-6.1.0.jar temp
copy /Y target\javacc-6.1.0-sources.jar temp
copy /Y target\javacc-6.1.0-javadoc.jar temp
copy deployment_pom\javacc-6.1.0.pom temp

cd temp

gpg -ab javacc-6.1.0.jar
gpg -ab javacc-6.1.0-sources.jar
gpg -ab javacc-6.1.0-javadoc.jar
gpg -ab javacc-6.1.0.pom

jar -cvf bundle.jar javacc-6.1.0.pom javacc-6.1.0.pom.asc javacc-6.1.0.jar javacc-6.1.0.jar.asc javacc-6.1.0-javadoc.jar javacc-6.1.0-javadoc.jar.asc javacc-6.1.0-sources.jar javacc-6.1.0-sources.jar.asc

cd ..