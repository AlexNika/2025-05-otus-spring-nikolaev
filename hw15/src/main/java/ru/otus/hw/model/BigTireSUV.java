package ru.otus.hw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BigTireSUV extends LiftedSUV {

    private String tireSize;

    private boolean bigTiresInstalled;
}