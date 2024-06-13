## Encrypted Chat room application

This is a simple chat room application that allows multiple clients to connect to a server and send messages to each other.

The application is built using Java and Maven, using version 20.

The application uses RSA encryption to encrypt messages between the client and server, with a key size of 2048 bits.

## Usage
- Client:
```
Run the client shell script to start the client application:
$ ./client.sh

Alternatively, run the following command in the terminal:
$ mvn exec:java -Dexec.mainClass="clientside.backend.Client"
```

- Server:
```
Run the server shell script to start the server application:
$ ./server.sh

Alternatively, run the following command in the terminal:
$ mvn exec:java -Dexec.mainClass="serverside.backend.Server"
```
Server-password is `password` by default,
and can be changed in the `config.ConnectionConfig.java` file.

# Commands
- Direct message: `/msg <username> <message>`
- List all users: `/list`
- Kick user: `/nick <username>`
- Exit chatroom: `/quit`
- Help: `/help`

# Security
The application uses RSA encryption to encrypt messages between the client and server.
All clients has their own key pair, as well as their Client handler thread on the server.

All messages are decrypted on the server side, to enable direct messaging between clients and command execution on the server.

This is a proof of concept, and should not be used for any sensitive information.