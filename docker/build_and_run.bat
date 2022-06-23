cd ..
call mvnw clean package -DskipTests

cd docker
xcopy /s "..\consumers\target\*.jar" ".\consumers\" /Y
xcopy /s "..\producers\target\*.jar" ".\producers\" /Y
xcopy /s "..\dataServices\target\*.jar" ".\dataServices\" /Y

call docker-compose up
