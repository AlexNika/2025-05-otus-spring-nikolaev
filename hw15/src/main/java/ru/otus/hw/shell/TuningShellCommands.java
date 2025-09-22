package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.gateway.TuningGateway;
import ru.otus.hw.model.BigTireSUV;
import ru.otus.hw.model.SUV;

@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class TuningShellCommands {

    private final TuningGateway tuningGateway;

    @ShellMethod(key = "tune", value = "Тюнинг автомобиля")
    public String tuneCar(
            @ShellOption(help = "Марка автомобиля") String brand,
            @ShellOption(help = "Модель автомобиля") String model) {
        try {
            log.info("Начало тюнинга автомобиля: {} {}", brand, model);
            SUV suv = SUV.builder()
                    .brand(brand)
                    .model(model)
                    .build();
            BigTireSUV tunedSUV = tuningGateway.tuneSUV(suv);
            log.info("Тюнинг завершен успешно!");
            return formatResult(tunedSUV);
        } catch (Exception e) {
            log.error("Ошибка при тюнинге автомобиля: {}", e.getMessage());
            return "Ошибка: " + e.getMessage();
        }
    }

    @ShellMethod(key = "tune-sample", value = "Тюнинг тестового автомобиля")
    public String tuneSampleCar() {
        return tuneCar("Toyota", "Land Cruiser");
    }

    private String formatResult(BigTireSUV tunedSUV) {
        StringBuilder tuningResult = new StringBuilder();
        tuningResult.append("Результат тюнинга:\n");
        tuningResult.append(String.format("Автомобиль: %s %s\n", tunedSUV.getBrand(), tunedSUV.getModel()));
        tuningResult.append(String.format("Шноркель установлен: %s\n", tunedSUV.isSnorkelInstalled()));
        tuningResult.append(String.format("Лебедка установлена: %s\n", tunedSUV.isWinchInstalled()));
        tuningResult.append(String.format("Защита днища установлена: %s\n", tunedSUV.isProtectionInstalled()));
        tuningResult.append(String.format("Подвеска усилена с лифтом: %.1f дюймов\n", tunedSUV.getLiftHeightInches()));
        tuningResult.append(String.format("Большие шины установлены: %s (%s)\n", tunedSUV.isBigTiresInstalled(),
                tunedSUV.getTireSize()));
        return tuningResult.toString();
    }
}