package com.nus.tt02backend.models;

import com.nus.tt02backend.models.enums.InternalRoleEnum;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InternalStaff extends User {

    @Column(nullable = false)
    private String staff_num;

    @Enumerated(EnumType.STRING)
    private InternalRoleEnum role;

}
