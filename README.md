# Custom certificate server and TLS RestTemplate calls with spring boot v2

## Description
After running in to an issue when trying to call aN HTTPS endpoint secured via a custom TLS certificate signer I found that
most examples of showing to set up an TLS connection were rather vague at the point of understanding how to set up
a truststore, and connecting everything together.

Our example is straight forward. We have an `SSL-Server` serving content over `https://localhost:8443` and a `SSL-Client`
which will make calls to the `SSL-Server` via [RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html).

### Code directories
In this repository there are two spring boot applications.

#### Assumptions
1. gradle v6 or greater is installed (It may run on previous versions but is untested)
1. java 11 or greater is installed
 
#### SSL-Server 
[/ssl-server](/ssl-server) directory is a server that will serve secure content over TLS. The purpose of this server is
to be a starting ground to be able to spin up an TLS Server that allows full control of the certificate created and served.

#### SSL-Client
[/ssl-client](/ssl-client) directory is the client that will utilize the RestTemplate and modified RestTemplate to make
calls to the SSL-Server and return the result. It is set up with a few endpoints, and we will cover two of them now.
1. [http://localhost:8080/bored/static](http://localhost:8080/bored/static) - This endpoint utilizes the TLS Configured
RestTemplate and successfully call and receive values from the SSL-Server
1. [http://localhost:8080/bored/failure](http://localhost:8080/bored/failure) - This endpoint utilizes the standard RestTemplate
configuration and does not have any TLS certs loaded in. This will show the *client* failing to understand the certificate
path received from the server.

## Running the application
The code is ready to be run without any tweaks. This will allow you a chance to see it in action before tearing
it down and rebuilding to give additional context.

##### To run the application
After cloning the repository we will start with booting the server.
1. Open a terminal
1. Navigate to `SSLRestTemplateExample/ssl-server`
1. Execute `gradle bootRun`
1. At this point you can test the server by hitting [https://localhost:8443/server/static](https://localhost:8443/server/static)
    1. If you get a Chrome warning follow these sub steps
        1. In the address bar enter `chrome://flags`
        1. search for `insecure-localhost`
        1. Set it to `Enabled`
        1. Reboot your browser

After the server is running lets boot the client
1. Open another terminal
1. Navigate to `SSLRestTemplateExample/ssl-client`
1. Execute `gradle bootRun`
1. Hit one of the active endpoints
    1. [http://localhost:8080/bored/static](http://localhost:8080/bored/static) - This endpoint utilizes the TLS Configured
RestTemplate and successfully call and receive values from the SSL-Server
    1. [http://localhost:8080/bored/failure](http://localhost:8080/bored/failure) - This endpoint utilizes the standard RestTemplate
configuration and does not have any TLS certs loaded in. This will show the *client* failing to understand the certificate
path received from the server.

After hitting the endpoints review the Client logs to see the TLS handshakes and other processes taking place. These logs
can be disabled in the [ssl-client/build.gradle](ssl-client/build.gradle) file under the `bootRun` config.

## Manually running the commands

### Configuring the SSL-Server
First up we will configure the SSL-Server to run in TLS mode. There are just a handful of settings needed, and a keystore
to configure to enable TLS on our server.

Inside the [ssl-server/src/main/resources/application.properties](ssl-server/src/main/resources/application.properties)
file there are parameters we will configure for the server to find its key.
1. server.port - the port to serve the application from.
1. server.ssl.key-store-type - the type of keystore. Either JKS or PKCS12.
1. server.ssl.key-store - the location of the keystore inside the jar.
1. server.ssl.key-store-password - the password for the keystore
1. server.ssl.key-alias - the alias under which our key lives

##### At this point we need to take a super quick detour and clean out the existing keystore, cert, and client truststore.
1. Navigate to [/ssl-server/src/main/resources](/ssl-server/src/main/resources) and delete `customKeystore.jks` and `customServer.cer`
2. Navigate back to the root directory `SSLRestTemplateExample`
1. Navigate to [/ssl-client/src/main/resources](/ssl-client/src/main/resources) and delete `customTruststore.jks` and `customerServer.cer`

##### Now we have these settings configured lets jump in and actually generate the keystore.  

A keystore is a store of private and public keys. In order for the server to serve content over TLS it needs a private key
and a public key. The private and public key will be housed inside the keystore and when the server starts up it will expose
its public key as the certificate for the server. When connecting to the server it will present the certificate to the user
and if it is signed by a trusted certificate authority it will show that the connection is secure. 

In this example we will not be using a trusted certificate authority and instead will create, sign, and store our own
certificate. This is known as a self-signed certificate and all browsers will warn against this, but using a trusted
certificate authority costs money.

To generate they keystore we will utilize keytool. Keytool is a built in utility with the java install. The following command
will create a keystore and generate a key within it.

First navigate to [/ssl-server/src/main/resources](/ssl-server/src/main/resources) then run the following command. The
command will ask for 6 inputs. It's ok to accept the defaults, press enter six times. On the seventh prompt type yes to generate
the key and press enter.

`keytool -genkeypair -alias localServer -keyalg RSA -keysize 2048 -keystore customKeystore.jks -validity 3650 -storepass notASecurePassword -ext SAN=dns:localhost`

The flags as part of this command are as follows:
```
 -genkeypair - Informs the tool to generate a private/public key pair and store it in the keystore. If the keystore doesn't
exist, it creates it.
 -alias - When looking inside the keystore this value is the tag applied to the key. It holds no purpose outside of keeping
the keys organized and giving a way to keep track of what it is for.
 -keyalg - The key algorithm used to generate the key.
 -keysize - The size of the key. Keep in mind the larger the size the longer it takes to decode 2048 is a happy middle ground.
 -keystore - The name of the keystore saved inside the reources folder.
 -validity - The length in days the key will be valid for before needing to be generated again.
 -storepass - The password for the keystore.
 -ext - The value here adds a subject alternative name telling the server that the key will be served via localhost. This
prevents an error message when the client calls over to the server.
```

Next up the public key needs to be exported out of the keytool to allow it to be imported in to our truststore. More on the
truststore in a moment.

To export the public key run the command below.

`keytool -export -keystore customKeystore.jks -alias localServer  -file customServer.cer -storepass notASecurePassword` 

Two new flags in this command
```
 -export - command to export the certificate from the keystore,
 -file - where to store the certificate on the local filesystem.
```

Now it's time to move the file from the server resources over to the client resource folder to aid in the creation of the
truststore.

Let's copy and then change over to the client side of the house.

`cp customServer.cer ../../../../ssl-client/src/main/resources`

`cd ../../../../ssl-client/src/main/resources`

Now is a great time to talk about the other side of the house, the client truststore. A truststore is similar to a keystore
but does not hold private certifiate information. Instead, the truststore holds all public certificates the developer decides
can be trusted for their client to communicate with. Java actually ships with the standard trusted certificate authority certs
installed in a truststore called cacerts. 

If making an HTTPS call to a server signed by a trusted certificate authority RestTemplate will not throw any errors, but
in our example here we are covering if the certificate was signed by some internal to the client certificate authority. With
this information we can now create a truststore with the public key of the server.

To create the truststore run the following keytool command to create the truststore. Reminder this is only storing a public
certificate and does not create a certificate. 

`keytool -import -file customServer.cer -alias theServer -keystore customTruststore.jks -storetype JKS -storepass stillNotASecurePassword`

Breaking down the flags in this command
```
 -import - Imports the certificate to the truststore. If the truststore doesn't exist it creates it.
 -file - The file with the certificate in it.
 -storeType - The default is JKS, but it could be PKCS12. This sets up how the keys are stored inside the truststore. 
```

That concludes setting up a keystore and a truststore. At this point see the steps in the `To run the application` to
start the application up.

##### A common problem when certificates are in a chain
Many times a certificate authority can and will have a chain of certificate signings for an application. All certificates
within the chain must be put inside the truststore. For example if the certificate chain looked something like this:

```
Root Certificate Authority - Public certificate
  |
   --> intermediate certificate authority 1 - Public certificate
        |
         --> server certificate - Public certificate
```

All three certificates must be in the truststore otherwise errors like the ones below will appear.

`PKIX path building failed` and `unable to find valid certification path to requested target`

