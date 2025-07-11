# Урок №2 - Введение в Spring Framework

# Домашнее задание

## Цель:

- закрепление материала прошедших лекций:
- проектирование доменных сущностей;
- ООП;
- паттерн DAO;
- создание и конфигурирование приложений с помощью Spring;
- XML-based конфигурация контекста;
- SOLID;
- основы тестирования в Java.

### Результат: приложение на чистом Spring с XML-based конфигурацией контекста и юнит-тестом сервиса

## Описание/Пошаговая инструкция выполнения домашнего задания

### <i><u>Общая задача для двух первых ДЗ:</u></i>

Разработать однопользовательское, консольное приложение для проведения тестирования
студентов на основе фиксированного списка вопросов. Приложение должно запросить фамилию и имя студента,
задать вопросы с вариантами ответа и вывести результат тестирования.

Разработка приложения делится на два этапа, которые выполняются, в первом и, соответственно, во втором ДЗ.

### <i><u>Текущее задание:</u></i>

Приложение должно загрузить и вывести на экран вопросы с вариантами ответа.
Задание выполняется на базе заготовки, содержащей каркас приложения (с прицелом на его развитие).
Необходимо дописать классы и служебные файлы заготовки, ориентируясь на оставленные в ней подсказки так,
чтобы выполнялись условия задания. Также решение должно удовлетворять нижеуказанным требованиям.

#### <i><u>Требования к использованию заготовки:</u></i>

- То, что уже было в заготовке должно остаться неизменным (количество, имена и текущее содержимое классов и файлов,
  сигнатуры методов и т.д.)
- Можно добавлять код/текст в существующие методы классов и файлы, писать свои приватные методы,
  а также создавать новые классы
- Вышеуказанные требования не относятся к комментариям, их после реализации нужно удалить

#### <i><u>Требования к реализации:</u></i>

- Весь ввод-вывод осуществляется на английском языке, в т.ч. вопросы и варианты ответов;
- Список вопросов с вариантами ответа хранится в ресурсах приложения, в формате csv;
- Файл с вопросами читается именно как ресурс, а не как файл (см. подсказки в коде заготовки);
- Все зависимости должны быть настроены в IoC контейнере;
- Контекст описывается XML-файлом;
- Контекст содержит только компоненты приложения и не содержит стандартных классов и доменных объектов (см.
  соответствующие слайды с занятия);
- Имя ресурса с вопросами (csv-файла) необходимо захардкодить строчкой в XML-файле с контекстом;
- В приложении должна присутствовать объектная модель (отдаём предпочтение объектам и классам, а не строчкам и
  массивам/спискам строчек);
- Все классы в приложении должны решать строго определённую задачу (см. п. 18-19 "Правила оформления кода.pdf",
  прикреплённые к материалам занятия);
- Необходимо написать юнит-тест сервиса тестирования;
- Проверка checkstyle должна проходить успешно;
- Без фанатизма;

Заготовка для выполнения работы: https://github.com/OtusTeam/Spring/tree/master/templates/hw01-xml-config

Задание сдаётся в виде ссылки на pull-request в чат с преподавателем в личном кабинете ОТУС, а не в Telegram

В pull-request должно присутствовать только то, что касается текущей работы.
Временные файлы и файлы IDE не должны попадать в PR.
