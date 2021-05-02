package com.ps.grpc.server;

import com.google.protobuf.ByteString;
import com.ps.grpc.messages.EmployeeServiceGrpc;
import com.ps.grpc.messages.Messages;
import io.grpc.stub.StreamObserver;

import java.util.List;

// extending generated class EmployeeServiceGrpc, EmployeeServiceGrpc class has basic skeleton generated and we need to
// override the method inorder to serve to clients.

public class EmployeeService extends EmployeeServiceGrpc.EmployeeServiceImplBase {
    // Unary request, one request, one response
    @Override
    public void getByBadgeNumber(Messages.GetByBadgeNumberRequest request,
                                 StreamObserver<Messages.EmployeeResponse> responseObserver) {

        for (Messages.Employee e : Employees.getInstance()) {
            if (e.getBadgeNumber() == request.getBadgeNumber()) {
                Messages.EmployeeResponse response = Messages.EmployeeResponse.
                        newBuilder().
                        setEmployee(e)
                        .build();

                responseObserver.onNext(response);
                // This is needed , as it will say that we are done sending messages, even thouh unary request only require one response.
                responseObserver.onCompleted();
            }

            // What if employee is not found
            responseObserver.onError(new Exception("Employee not found with badge number" + request.getBadgeNumber()));
        }
    }

    // Server streaming, send multiple response for a single request
    @Override
    public void getAll(Messages.GetAllRequest request,
                       StreamObserver<Messages.EmployeeResponse> responseObserver) {

        List<Messages.Employee> employeeList = Employees.getInstance();
        for (Messages.Employee e : employeeList) {
            Messages.EmployeeResponse response = Messages.EmployeeResponse.
                    newBuilder().
                    setEmployee(e)
                    .build();

            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    // Bidirectional streaming, both side parallel request response
    @Override
    public StreamObserver<Messages.EmployeeRequest> saveAll(StreamObserver<Messages.EmployeeResponse> responseObserver) {
        return new StreamObserver<Messages.EmployeeRequest>() {
            @Override
            public void onNext(Messages.EmployeeRequest employeeRequest) {
                Employees.getInstance().add(employeeRequest.getEmployee());
                responseObserver.onNext(Messages.EmployeeResponse.newBuilder().setEmployee(employeeRequest.getEmployee()).build());

            }

            @Override
            public void onError(Throwable throwable) {

            }

            // when server receives the last message from the client then we are printing all the employees which server has
            // Just to showcase
            @Override
            public void onCompleted() {
                for (Messages.Employee e : Employees.getInstance()) {
                    System.out.println(e);
                }
                System.out.println("Done...");
                responseObserver.onCompleted();
            }
        };
    }

    // Client streaming, single response for multiple requests
    // Workflow: We will be returning StreamObserver response (which contains what to do on which event, OnNext, error, and onComplete
    // GRPC framework will keep invoking these methods bases on events it will be receiving from the client side
    //Note: This is multithreaded internally, otherwise server will freeze waiting for the requests and could not server to other clients
    @Override
    public StreamObserver<Messages.AddPhotoRequest> addPhoto(StreamObserver<Messages.AddPhotoResponse> responseObserver) {
        return new StreamObserver<Messages.AddPhotoRequest>() {
            private ByteString result;

            @Override
            public void onNext(Messages.AddPhotoRequest v) {
                // initial bytestring
                if (result == null) {
                    result = v.getData();
                } else {
                    result = result.concat(v.getData());
                }

                System.out.println("Received messages with " + v.getData().size() + " bytes");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }

            // This will be called when grpc recognizes that this is the last message from the client.
            // And that means we are ready to send response
            @Override
            public void onCompleted() {
                System.out.println("Total bytes received : " + result.size());
                responseObserver.onNext(Messages.AddPhotoResponse.newBuilder().setIsOk(true).build());
                responseObserver.onCompleted();
            }
        };


    }
}
