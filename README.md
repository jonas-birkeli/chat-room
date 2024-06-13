Simple chatroom application using sockets in Java 20.

Application uses a server and client architecture to allow multiple clients to connect to the server and send messages to each other.

# Security
AES in BCB mode is used to encrypt messages sent between clients and the server.
On connecting, the server requests a password from the client, stored in the config.ConnectionConfig class.