#Custom certificate server and SSL RestTemplate calls with spring boot v2

## Description
After running in to an issue when trying to call a HTTPS endpoint secured via a custom SSL certificate signer I found that
most examples of showing to set up an SSL connection were rather vague at the point of understanding how to set up
a truststore, and connecting everything together.

Our example is straight forward. We have an `SSL-Server` serving content over `https://localhost:8443` and a `SSL-Client`
which will make calls to the `SSL-Server` via [RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)

### Code directories
In this repository there are two spring boot applications.

#### Assumptions
1. gradle v6 or greater is installed (It may run on previous versions but is untested)
1. java 11 or greater is installed
 
#### SSL-Server 
[/ssl-server](/ssl-server) directory is a server that will serve secure content over SSL. The purpose of this server is
to be a starting ground to be able to spin up a SSL Server that allows for us to know the certificate being served and
have full control over it.

#### SSL-Client
[/ssl-client](/ssl-client) directory is the client that will utilize the RestTemplate and modified RestTemplate to make
calls to the SSL-Server and return the result. It is set up with a few endpoints and we will cover two of them now.
1. [http://localhost:8080/bored/static](http://localhost:8080/bored/static) - This endpoint utilizes the SSL Configured
RestTemplate and successfully call and receive values from the SSL-Server
1. [http://localhost:8080/bored/failure](http://localhost:8080/bored/failure) - This endpoint utilizes the standard RestTemplate
configuration and does not have any SSL certs loaded in. This will show the *client* failing to understand the certificate
path received from the server.

## Running the application
The code is set and ready to be run without any tweaks. This will allow you a chance to see it in action before tearing
it down and rebuilding to give additional context.

##### To run the application
After cloning the repository we will start with booting the server.
1. Open a terminal
1. Navigate to `SSLRestTemplateExample/ssl-server`
1. Execute `gradle bootRun`
1. At this point you can test the server by hitting [https://localhost:8443/server/static](https://localhost:8443/server/static)
    1. If you get a Chrome warning follow these substeps
        1. In the address bar enter `chrome://flags`
        1. search for `insecure-localhost`
        1. Set it to `Enabled`
        1. Reboot your browser

After the server is running lets boot the client
1. Open another terminal
1. Navigate to `SSLRestTemplateExample/ssl-client`
1. Execute `gradle bootRun`
1. Hit one of the active endpoints
    1. [http://localhost:8080/bored/static](http://localhost:8080/bored/static) - This endpoint utilizes the SSL Configured
RestTemplate and successfully call and receive values from the SSL-Server
    1. [http://localhost:8080/bored/failure](http://localhost:8080/bored/failure) - This endpoint utilizes the standard RestTemplate
configuration and does not have any SSL certs loaded in. This will show the *client* failing to understand the certificate
path received from the server.

After hitting the endpoints review the Client logs to see the SSL handshakes and other processes taking place. These logs
can be disabled in the [ssl-client/build.gradle](ssl-client/build.gradle) file under the `bootRun` config.

## Manually running the commands

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

