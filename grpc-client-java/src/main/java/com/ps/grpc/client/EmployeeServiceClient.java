package com.ps.grpc.client;

import com.google.protobuf.ByteString;
import com.ps.grpc.messages.EmployeeServiceGrpc;
import com.ps.grpc.messages.Messages;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmployeeServiceClient {
    public static void main(String[] args) throws Exception {
        //create a channel b/w client server
        ManagedChannel channel
                = NettyChannelBuilder.forAddress("localhost", 9000)
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(new File("src/main/resources/cert.pm")).build())
                .build();
        EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient
                = EmployeeServiceGrpc.newBlockingStub(channel);

        EmployeeServiceGrpc.EmployeeServiceStub nonBlockingClient
                = EmployeeServiceGrpc.newStub(channel);

        switch (Integer.parseInt(args[0])) {
            case 1:
                sendMetadata(blockingClient);
                break;
            case 2:
                getByBadgeNumber(blockingClient);
                break;
            case 3:
                getAll(blockingClient);
                break;
            case 4:
                addPhoto(nonBlockingClient);
                break;
            case 5:
                saveAll(nonBlockingClient);
                break;
        }

        // Hack: wait 500 ms so that nonblocking helper threads gets completed before we close the connection.

        Thread.sleep(500);

        // Wait, why do we need to close the connection?
        // Since we have been using HTTp2(using ssl), server needs to know whether client closed the connection or not.
        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

// bi-directional streaming(require non-blocking)
// scenario : send some employees to save on the server and server will return saved employees

    private static void saveAll(EmployeeServiceGrpc.EmployeeServiceStub client) {
        List<Messages.Employee> employees = new ArrayList<Messages.Employee>();
        employees.add(Messages.Employee.newBuilder()
                .setBadgeNumber(123)
                .setFirstName("John")
                .setLastName("Smith")
                .setVacationAccrualRate(1.2f)
                .build());
        employees.add(Messages.Employee.newBuilder()
                .setBadgeNumber(234)
                .setFirstName("Lisa")
                .setLastName("Wu")
                .setVacationAccrualRate(1.7f)
                .setVacationAccrued(10)
                .build());

        // we pass in the response handler and we get back the stream overserver which will work with the employee request
        StreamObserver<Messages.EmployeeRequest> stream =
                client.saveAll(new StreamObserver<Messages.EmployeeResponse>() {
                    @Override
                    public void onNext(Messages.EmployeeResponse v) {
                        System.out.println(v.getEmployee());
                    }

                    @Override
                    public void onError(Throwable thrwbl) {
                        System.out.println(thrwbl);
                    }

                    @Override
                    public void onCompleted() {
                        // no-op
                    }
                });

        for (Messages.Employee e : employees) {
            Messages.EmployeeRequest request = Messages.EmployeeRequest.newBuilder()
                    .setEmployee(e)
                    .build();
            stream.onNext(request);
        }
        // this will notify the framework which in turn notify the server that request is com
        stream.onCompleted();

    }

    // Client streaming and bi-directional requests can not be done using blocking(synchronus) call in grpc Java
    // Non blocking is necessary
    private static void addPhoto(EmployeeServiceGrpc.EmployeeServiceStub client) {
        try {
            // this is a response handler
            StreamObserver<Messages.AddPhotoRequest> stream
                    = client.addPhoto(
                    new StreamObserver<Messages.AddPhotoResponse>() {
                        // this is gonna called when we get response from server
                        // Note: Since this is a client streaming we will send multiple request but we are only going to get one response
                        @Override
                        public void onNext(Messages.AddPhotoResponse response) {
                            System.out.println(response.getIsOk());
                        }

                        @Override
                        public void onError(Throwable thrwbl) {
                            System.out.println(thrwbl);
                        }

                        @Override
                        public void onCompleted() {
                            // no-op
                        }

                    });

            FileInputStream fs = new FileInputStream("Penguins.jpg");
            while (true) {
                byte[] data = new byte[64 * 1024];

                int bytesRead = fs.read(data);
                if (bytesRead == -1) {
                    break;
                }

                if (bytesRead < data.length) {
                    byte[] newData = new byte[bytesRead];
                    System.arraycopy(data, 0, newData, 0, bytesRead);
                    data = newData;
                }

                Messages.AddPhotoRequest request = Messages.AddPhotoRequest.newBuilder()
                        .setData(ByteString.copyFrom(data)).build();

                // If we look on server side, we understood that onNext method is called(on server side) by the framework
                // this onNext method call initiates that
                stream.onNext(request);
            }

            // this call will tell the framework that request is completed now, this will make framework call onCompleted on server side
            // and server will understand that request is completed now.
            stream.onCompleted();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /* blocking call, we are processing the request synchronusly**/
    private static void getAll(
            EmployeeServiceGrpc.EmployeeServiceBlockingStub client) {
        // This is like yeild of python, request returns an interor and we iterate through the response one by one
        Iterator<Messages.EmployeeResponse> iterator
                = client.getAll(Messages.GetAllRequest.newBuilder().build());
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getEmployee());
        }
    }

    private static void sendMetadata(
            EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient) {
        Metadata md = new Metadata();
        md.put(Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER),
                "mvansickle");
        md.put(Metadata.Key.of("password", Metadata.ASCII_STRING_MARSHALLER),
                "password1");

        // wrap default channel with a new channel which has interceptpor which will encode the metadata in the request
        Channel ch = ClientInterceptors.intercept(blockingClient.getChannel(),
                MetadataUtils.newAttachHeadersInterceptor(md));

        blockingClient.withChannel(ch).getByBadgeNumber(
                Messages.GetByBadgeNumberRequest.newBuilder().build());
    }

    private static void getByBadgeNumber(EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient) {
        //Since we are not sending any metadata we don't need any channel and we will invoke the method just like it's native
        Messages.EmployeeResponse response
                = blockingClient.getByBadgeNumber(
                Messages.GetByBadgeNumberRequest.newBuilder()
                        .setBadgeNumber(2080)
                        .build());
        System.out.println(response.getEmployee());

    }
}
