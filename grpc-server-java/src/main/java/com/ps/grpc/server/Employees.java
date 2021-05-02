package com.ps.grpc.server;

import com.ps.grpc.messages.Messages;

import java.util.ArrayList;
import java.util.List;

public class Employees {

    static List<Messages.Employee> employeesList = new ArrayList<>();
    
    public static List<Messages.Employee> getInstance() {
        if (employeesList == null) {
            init();
        }
        return employeesList;
    }
    
    private static void init() {
        Messages.Employee emp1 = Messages.Employee.newBuilder()
                .setId(1)
                .setBadgeNumber(2080)
                .setFirstName("Grace")
                .setLastName("Decker")
                .setVacationAccrualRate(2)
                .setVacationAccrued(30)
                .build();
        
        Messages.Employee emp2 = (Messages.Employee.newBuilder()
                .setId(2)
                .setBadgeNumber(7538)
                .setFirstName("Amity")
                .setLastName("Fuller")
                .setVacationAccrualRate(2.3f)
                .setVacationAccrued(23.4f)
                .build());

        Messages.Employee emp3 = (Messages.Employee.newBuilder()
                .setId(1)
                .setBadgeNumber(5144)
                .setFirstName("Keaton")
                .setLastName("Willis")
                .setVacationAccrualRate(3)
                .setVacationAccrued(31.7f)
                .build());

        employeesList.add(emp1);
        employeesList.add(emp2);
        employeesList.add(emp3);

        return;
    }
    
}
