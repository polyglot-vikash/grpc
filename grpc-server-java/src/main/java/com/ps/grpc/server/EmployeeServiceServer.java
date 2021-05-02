package com.ps.grpc.server;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import java.io.File;

public class EmployeeServiceServer {
    public static void main(String args[]) {
        try {
            EmployeeServiceServer employeeServiceServer = new EmployeeServiceServer();
            employeeServiceServer.start();
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    private Server server;

    private void start() throws Exception {
        final int port = 9000;
        ClassLoader classLoader = getClass().getClassLoader();
        File cert = new File("src/main/resources/cert.pm");
        File key = new File("src/main/resources/key.pm");

        EmployeeService employeeService = new EmployeeService();
        // binding Service and interceptor, since service and interceptor both are not aware of each other
        ServerServiceDefinition serviceDefinition = ServerInterceptors.interceptForward(employeeService, new HeaderServerInterceptor());

        server = ServerBuilder.
                forPort(port).
                useTransportSecurity(cert, key).
                addService(serviceDefinition).
                build().
                start();
        System.out.println("Server up on " + port);

        // adding a shutdown hook, this will be excecuted when application starts to shutdown
        // a new thread will be spawned and the stop method will be called.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down the server");
                EmployeeServiceServer.this.stop();
            }
        });

        // This method blocks inside the method until a request comes for termination.
        server.awaitTermination();

    }

    private void stop() {
        if (server != null) {
            System.out.println("Shutting down the server..");
            server.shutdown();
        }
    }
}
