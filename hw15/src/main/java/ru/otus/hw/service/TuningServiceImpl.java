package ru.otus.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.BigTireSUV;
import ru.otus.hw.model.LiftedSUV;
import ru.otus.hw.model.ProtectedSUV;
import ru.otus.hw.model.SUV;
import ru.otus.hw.model.SnorkeledSUV;
import ru.otus.hw.model.WinchedSUV;

@Slf4j
@Service
public class TuningServiceImpl implements TuningService {

    @Override
    public SnorkeledSUV installSnorkel(SUV suv) {
        log.info("Установка шноркеля на {} {}", suv.getBrand(), suv.getModel());
        SnorkeledSUV snorkeledSUV = new SnorkeledSUV();
        snorkeledSUV.setBrand(suv.getBrand());
        snorkeledSUV.setModel(suv.getModel());
        snorkeledSUV.setSnorkelInstalled(true);
        return snorkeledSUV;
    }

    @Override
    public WinchedSUV installWinch(SnorkeledSUV suv) {
        log.info("Установка лебедки на {} {}", suv.getBrand(), suv.getModel());
        WinchedSUV winchedSUV = new WinchedSUV();
        winchedSUV.setBrand(suv.getBrand());
        winchedSUV.setModel(suv.getModel());
        winchedSUV.setSnorkelInstalled(suv.isSnorkelInstalled());
        winchedSUV.setWinchInstalled(true);
        return winchedSUV;
    }

    @Override
    public ProtectedSUV installProtection(WinchedSUV suv) {
        log.info("Установка защиты днища на {} {}", suv.getBrand(), suv.getModel());
        ProtectedSUV protectedSUV = new ProtectedSUV();
        protectedSUV.setBrand(suv.getBrand());
        protectedSUV.setModel(suv.getModel());
        protectedSUV.setSnorkelInstalled(suv.isSnorkelInstalled());
        protectedSUV.setWinchInstalled(suv.isWinchInstalled());
        protectedSUV.setProtectionInstalled(true);
        return protectedSUV;
    }

    @Override
    public LiftedSUV upgradeSuspension(ProtectedSUV suv) {
        log.info("Замена подвески с лифтом 2.5 дюйма на {} {}", suv.getBrand(), suv.getModel());
        LiftedSUV liftedSUV = new LiftedSUV();
        liftedSUV.setBrand(suv.getBrand());
        liftedSUV.setModel(suv.getModel());
        liftedSUV.setSnorkelInstalled(suv.isSnorkelInstalled());
        liftedSUV.setWinchInstalled(suv.isWinchInstalled());
        liftedSUV.setProtectionInstalled(suv.isProtectionInstalled());
        liftedSUV.setLiftHeightInches(2.5);
        liftedSUV.setSuspensionUpgraded(true);
        return liftedSUV;
    }

    @Override
    public BigTireSUV installBigTires(LiftedSUV suv) {
        log.info("Установка больших колес 35x12.5R17 на {} {}", suv.getBrand(), suv.getModel());
        BigTireSUV bigTireSUV = new BigTireSUV();
        bigTireSUV.setBrand(suv.getBrand());
        bigTireSUV.setModel(suv.getModel());
        bigTireSUV.setSnorkelInstalled(suv.isSnorkelInstalled());
        bigTireSUV.setWinchInstalled(suv.isWinchInstalled());
        bigTireSUV.setProtectionInstalled(suv.isProtectionInstalled());
        bigTireSUV.setLiftHeightInches(suv.getLiftHeightInches());
        bigTireSUV.setSuspensionUpgraded(suv.isSuspensionUpgraded());
        bigTireSUV.setTireSize("35x12.5R17");
        bigTireSUV.setBigTiresInstalled(true);
        return bigTireSUV;
    }
}