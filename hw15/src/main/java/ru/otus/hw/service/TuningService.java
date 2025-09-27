package ru.otus.hw.service;

import ru.otus.hw.model.BigTireSUV;
import ru.otus.hw.model.LiftedSUV;
import ru.otus.hw.model.ProtectedSUV;
import ru.otus.hw.model.SUV;
import ru.otus.hw.model.SnorkeledSUV;
import ru.otus.hw.model.WinchedSUV;

public interface TuningService {
    SnorkeledSUV installSnorkel(SUV suv);

    WinchedSUV installWinch(SnorkeledSUV suv);

    ProtectedSUV installProtection(WinchedSUV suv);

    LiftedSUV upgradeSuspension(ProtectedSUV suv);

    BigTireSUV installBigTires(LiftedSUV suv);
}
