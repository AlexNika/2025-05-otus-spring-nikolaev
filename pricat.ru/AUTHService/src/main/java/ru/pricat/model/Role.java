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
@Table("roles")
public class Role {

    @Id
    private UUID id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
