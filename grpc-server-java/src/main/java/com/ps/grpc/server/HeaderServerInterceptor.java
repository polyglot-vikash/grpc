package com.ps.grpc.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

// This class will itercept metadata sent in the request
public class HeaderServerInterceptor implements ServerInterceptor {
    /**
     *
     * @param serverCall, this is the actual incoming call, anything available in incoming call will be in this object
     * @param metadata, metadata
     * @param next, next method in chain, we can chain multiple interceptor, when we are done with this interceptor
     *                           then we need to call next(interceptor) with required paramters.
     *                           At the end of the chain, final method call will be actual service call.
     * @param <ReqT>
     * @param <RespT>
     * @return
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        if(serverCall.getMethodDescriptor().getFullMethodName().equalsIgnoreCase("EmployeeService/GetByBadgeNumber")) {
            for(String key: metadata.keys()) {
             System.out.println(key + ";" + metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)));
            }
        }

        return next.startCall(serverCall, metadata);
    }
}
