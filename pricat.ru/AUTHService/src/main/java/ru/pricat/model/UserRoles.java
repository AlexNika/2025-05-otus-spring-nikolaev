package ru.pricat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles")
public class UserRoles {

    @Id
    private Void id;

    @Column("user_id")
    private UUID userId;

    @Column("role_id")
    private UUID roleId;
}
