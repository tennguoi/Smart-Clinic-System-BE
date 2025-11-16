// com/example/demo/entity/DoctorPatientId.java
package com.example.demo.entity;

import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class DoctorPatientId implements Serializable {
    private String doctorId;
    private String patientId;
}