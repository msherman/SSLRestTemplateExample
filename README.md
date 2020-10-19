Generate the server keystore.jks. Self signed ofcourse

`keytool -genkeypair -alias localServer -keyalg RSA -keysize 2048 -keystore customKeystore.jks -validity 3650 -storepass notASecurePassword -ext SAN=dns:localhost`

press enter 6 times
type yes to accept the generation
press enter

export the certificate out  

`keytool -export -keystore customKeystore.jks -alias localServer  -file customServer.cer -storepass notASecurePassword` 

copy the file to the client resources

`cp customServer.cer ../../../../ssl-client/src/main/resources `

`cd ../../../../ssl-client/src/main/resources`

`keytool -import -file customServer.cer -alias theServer -keystore customTruststore.jks -storetype JKS -storepass stillNotASecurePassword`

type yes
press enter

