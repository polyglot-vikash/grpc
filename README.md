# grpc
Demo project demonstrates client server communication using GRPC and protocol buffers


Steps to run:
1. Import grpc-client and grpc-server applications separately.
2. Add a cert.pm and key.pm (certificates file) under resources folder in both the projects. https://stackoverflow.com/questions/10175812/how-to-create-a-self-signed-certificate-with-openssl
3. Pass any value between 1 to 4 as command line arg in client application, based on that it invokes different apis.

To compile message definition in client side (proto file) use below command (change similarly for server app)
```
protoc -I .\grpc-client-java --java_out .\grpc-client-java\src\main\java .\grpc-client-java\messages.proto --grpc_out .\grpc-client-java\src\main\java --plugin=protoc-gen-grpc=.\grpc-client-java\lib\protoc-gen-grpc-java-1.0.1-windows-x86_64.exe
```

To run the above you need to download protoc-gen-grpc-java-1.0.1-windows-x86_64.exe and put that in lib folder.
