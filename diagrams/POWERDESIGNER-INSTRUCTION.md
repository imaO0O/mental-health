# Построение моделей данных в Sybase PowerDesigner

Пошаговая инструкция для проекта **«Мониторинг ментального здоровья студентов»**.
Базируется на методичке СПДБ (Lab4-SybasePD) и адаптирована под предметную область.

PowerDesigner 15 — CASE-средство со сквозным циклом проектирования:

```
CDM (концептуальная) → LDM (логическая) → PDM (физическая) → SQL → база
```

Сначала строится **концептуальная модель** (что мы моделируем), потом она
автоматически разворачивается в **логическую** (реляционные таблицы со связями
по внешним ключам), потом в **физическую** (с типами конкретной СУБД и
DDL-настройками), и из физической генерируется **SQL-скрипт**, который
создаст базу.

---

## 0. Подготовка: создание проекта

1. Запустить PowerDesigner.
2. **File → New Project**.
3. В диалоге выбрать тип **Project**, в имени написать `MentalHealthMonitoring`,
   указать каталог для сохранения. **OK**.
4. **File → Save All** — файл проекта сохраняется с расширением `*.prj`.

В проекте далее будут жить три модели: `MentalHealthCDM.cdm`,
`MentalHealthLDM.ldm`, `MentalHealthPDM.pdm`.

---

## 1. Концептуальная модель (CDM)

### 1.1. Создание модели

1. **File → New Model** → выбрать категорию **ConceptualData**
   (Concept­ual Data Model) → имя `MentalHealthCDM` → **OK**.
2. **File → Save All** → сохранить как `MentalHealthCDM.cdm`.

### 1.2. Настройка нотации

3. **Tools → Model Options**:
   - **Notation: Entity/Relationship**
   - **Sub-notation: Crow Foot** *(или IDEF1X / Barker — по требованиям
     преподавателя)*
   - В разделе **Data Item** установить флаг **Unique code**, чтобы
     PowerDesigner запрещал одинаковые имена атрибутов в разных
     сущностях. Для атрибутов вроде `ID`, которые встречаются в каждой
     таблице, удобнее этот флаг **снять**.
4. **Tools → Display Preferences**:
   - **Content → Relationship → Cardinality** — чтобы на диаграмме
     отображалась кардинальность связей (0,1 / 1,1 / 0,n / 1,n).
   - **Content → Entity → Identifiers** — чтобы у сущностей было видно
     первичные ключи (раздел Identifiers), атрибуты (Attributes → Primary
     attributes), типы данных (Data types), обязательность (Mandatory).

### 1.3. Сущности

Перетаскиваем с панели **Palette** значок **Entity** на холст по одному для
каждой сущности (если палитры нет — **Tools → Customize Toolbars → Palette**).
Двойной клик по сущности → откроется диалог **Properties**.

**Заполняем минимум:** Name (русское название), Code (английский
идентификатор для SQL), Comment (описание для отчёта).

Сущности проекта:

| # | Name (русское)        | Code            | Комментарий                                        |
|---|----------------------|-----------------|----------------------------------------------------|
| 1 | Студенты             | `Students`      | Обучающиеся, проходящие тестирование               |
| 2 | Психологи            | `Psychologists` | Сотрудники отдела психологической поддержки        |
| 3 | Тесты                | `Tests`         | Психодиагностические методики (PSS-10, BDI, STAI, MBI) |
| 4 | Категории тестов     | `Categories`    | Стресс, Тревога, Депрессия, Выгорание              |
| 5 | Уровни стресса       | `StressLevels`  | Низкий, средний, высокий, критический              |

> Для **CDM** показываем только главные «бизнес-сущности» и связи между
> ними; служебные таблицы (роли, статусы, журнал аудита) добавляются
> на стадии LDM/PDM.

### 1.4. Атрибуты

В свойствах каждой сущности — вкладка **Attributes**. Для каждой строки
заполняем Name, Code, Data Type, Primary Identifier (галочка «P» —
первичный ключ), Mandatory (M — обязательность).

| Сущность     | Атрибут (Code)         | Тип               | PK | Описание                |
|--------------|------------------------|-------------------|----|-------------------------|
| Students     | `RecordBookNumber`     | Number (10)       | P  | Номер зачётной книжки   |
|              | `LastName`             | Variable Char (50)| M  | Фамилия                 |
|              | `FirstName`            | Variable Char (50)| M  | Имя                     |
|              | `MiddleName`           | Variable Char (50)|    | Отчество                |
|              | `GroupName`            | Variable Char (20)| M  | Учебная группа          |
|              | `Email`                | Variable Char (100)|   | Адрес электронной почты |
|              | `Phone`                | Variable Char (20)|    | Контактный телефон      |
| Psychologists| `PersonnelNumber`      | Number (10)       | P  | Табельный номер         |
|              | `LastName`, `FirstName`, `MiddleName` — аналогично студентам         |
|              | `Position`             | Variable Char (100)|   | Должность               |
|              | `Email`, `Phone`                                                      |
| Tests        | `TestCode`             | Variable Char (20)| P  | Шифр методики           |
|              | `Name`                 | Variable Char (200)| M | Полное название         |
|              | `Description`          | Text              |    | Описание                |
|              | `Instructions`         | Text              |    | Инструкции для прохождения|
|              | `IsActive`             | Boolean           |    | Доступна ли в системе   |
| Categories   | `CategoryName`         | Variable Char (50)| P  | Название категории      |
|              | `Description`          | Text              |    | Описание                |
| StressLevels | `LevelName`            | Variable Char (30)| P  | Название уровня         |
|              | `MinPercent`           | Number (3)        | M  | Нижняя граница %        |
|              | `MaxPercent`           | Number (3)        | M  | Верхняя граница %       |

> Если хотите суррогатные ключи (`ID` типа Serial с автогенерацией),
> ставьте тип **Serial**. В нашем проекте PK естественные —
> `RecordBookNumber`, `PersonnelNumber`, `TestCode` и т. д.

### 1.5. Связи между сущностями

Берём значок **Relationship** с палитры, тянем от одной сущности к другой.
Двойной клик по связи → **Properties**.

В **General**: Name (русское), Code (английский).
В **Cardinalities**:
- Тип связи: **One – One / One – Many / Many – One / Many – Many**.
- **Mandatory** (обязательность) с каждой стороны.
- **Dependent** — если PK дочерней сущности должен включать PK родителя.
- Кардинальность **A to B** — сколько экземпляров B может быть у одного A
  (0..1 / 1..1 / 0..n / 1..n).

#### Связи проекта

| Имя связи (Name)        | Code              | Сущности              | Тип | Доминантная роль |
|-------------------------|-------------------|-----------------------|-----|------------------|
| Специализируется        | `Specializes`     | Психологи ↔ Категории | M:N | Психологи        |
| Курирует                | `Supervises`      | Психологи → Студенты  | 1:N | Психологи        |
| Прохождение теста       | `TakesTest`       | Студенты ↔ Тесты ↔ Психологи | тернарная M:N:L | Тесты |
| Формулирует             | `Formulates`      | Психологи ↔ Уровни    | M:N | Психологи        |

**Атрибуты связи «Прохождение теста»** (превратятся в слабую сущность
`TestProtocols` на этапе LDM): Дата прохождения (`TakenAt`),
Суммарный балл (`TotalScore`), Номер протокола (`ProtocolNumber`).

Чтобы добавить атрибуты связи: в Properties связи — вкладка
**Attributes**. Принципиально важно при тернарной связи или связи M:N,
потому что эти атрибуты «переедут» в новую таблицу при генерации LDM.

### 1.6. Наследование (опционально)

Если решите оформить `Студенты` и `Психологи` как наследников от общей
сущности `Пользователи` — в палитре есть значок **Inheritance**:

1. Родитель — `Users` (атрибуты `Login`, `PasswordHash`, дата создания).
2. Тянем стрелку наследования от родителя к группе детей.
3. В свойствах Inheritance: галочки **Mutually exclusive inheritance**
   (один пользователь — либо студент, либо психолог),
   **Complete inheritance** (нет «просто пользователей» без подтипа).
4. На вкладке **Generation** выбираем:
   - **Generate parent** — создавать таблицу-родитель,
   - **Generate child** — создавать таблицы-наследники,
   - **Inherit only primary attributes** — наследовать только PK.

В нашем проекте удобнее всего: Generate parent + Generate child (получим
3 таблицы — `Users`, `Students`, `Psychologists` с FK `Login` к `Users`).

### 1.7. Проверка модели

**Tools → Check Model** (или F4). PowerDesigner проверит:
- одинаковые имена атрибутов / сущностей;
- сущности без первичного ключа;
- связи без кардинальности;
- циклы наследования.

Все замечания исправляем до перехода к LDM.

### 1.8. Сохранение

**File → Save All** — `MentalHealthCDM.cdm` обновлён.

---

## 2. Логическая модель (LDM)

LDM получается **автоматически** из CDM. PowerDesigner превратит:
- сущности — в таблицы;
- связи 1:N — в внешние ключи (FK);
- связи M:N — в **слабые сущности** (junction-таблицы) с составным
  PK из FK обеих сторон;
- атрибуты связи M:N — в обычные столбцы новой junction-таблицы.

### 2.1. Генерация LDM

1. **Tools → Generate Logical Data Model**.
2. В диалоге:
   - **Generate New Logical Data Model** — генерировать новую модель;
   - Имя `MentalHealthLDM`.
3. На вкладке **Configure Model Options**:
   - В разделе **Entity/Relationship** проверить, что галочка **Allow
     n-n relationships** **снята** (логическая модель не должна
     содержать связей M:N — они должны быть разрешены в junction-таблицы).
4. На вкладке **Migration Settings**:
   - **Foreign attribute name template:** `%PARENT%` —
     префикс имени родителя в имени мигрировавшего FK
     (например, FK на `Tests.TestCode` будет называться `TestsTestCode`).
   - Снять галочку **Always use template** — чтобы PowerDesigner
     использовал шаблон только когда без него получается коллизия.
5. На вкладке **Detail** — оставить значения по умолчанию.
6. **OK**.

### 2.2. Что получилось

LDM-модель будет содержать примерно такие таблицы:

| Таблица в LDM             | Откуда взялась                                |
|---------------------------|-----------------------------------------------|
| `Students`                | напрямую из CDM-сущности                      |
| `Psychologists`           | напрямую                                      |
| `Tests`                   | напрямую + FK на `Categories`, `Psychologists`|
| `Categories`              | напрямую                                      |
| `StressLevels`            | напрямую                                      |
| `PsychologistSpecializations` | из связи M:N «Специализируется»           |
| `TestProtocols`           | из тернарной связи «Прохождение теста» — слабая сущность с атрибутами `ProtocolNumber`, `TakenAt`, `TotalScore` |
| `PsychologistNotes`       | из связи «Курирует» (если хотите хранить заметки) |
| `Recommendations`         | из связи M:N «Формулирует»                    |

### 2.3. Настройка отображения

**Tools → Display Preferences**:
- **Content → Relationship → Cardinality + Name** — кардинальность и имя
  связи на линии.
- **Content → Entity → Identifiers + All attributes + Mandatory +
  Identifier indicators** — полный набор атрибутов с PK/FK и
  обязательностью.

### 2.4. Проверка LDM

**Tools → Check Model**. Логическая модель должна быть пригодна для
прямого превращения в реляционную БД: нет связей M:N, у каждой таблицы
есть PK, FK ссылаются на существующие PK.

---

## 3. Физическая модель (PDM)

PDM — это **реляционная** модель для **конкретной СУБД**, со всеми
DDL-настройками (типы, индексы, ограничения, правила update/delete).
В нашем случае СУБД — **PostgreSQL**, но в методичке примеры даны для
**Microsoft SQL Server**. Принципиальной разницы нет — поменяем DBMS
в диалоге генерации.

### 3.1. Генерация PDM

1. **Tools → Generate Physical Data Model**.
2. **Generate New Physical Data Model** → имя `MentalHealthPDM`.
3. **DBMS:** выбрать `PostgreSQL` (если нет в списке — `Sybase ASE` или
   `Microsoft SQL Server 2000`, как в методичке; DDL будет другим, но
   структура та же).
4. **Configure Model Options → Relational** — оставить по умолчанию.
5. **Detail:**
   - **FK column name template:** `%PARENT%` — префикс имени FK от
     имени родительской таблицы.
   - Снять **Always use template** (как и в LDM).
   - **Update rule: Cascade** — при изменении PK родителя обновлять FK.
   - **Delete rule: Restrict** — нельзя удалить родителя, если есть
     ссылки (наиболее безопасное правило для системы психологии).
6. **OK**.

### 3.2. Настройка отображения

**Tools → Display Preferences → Content → Reference → Center: None** —
чтобы стрелки FK не наезжали на середину таблиц.

### 3.3. Что проверить в PDM

- **Типы данных** превратились в типы PostgreSQL:
  `Serial` → `BIGSERIAL`, `Number(10)` → `BIGINT`, `Variable Char(50)`
  → `VARCHAR(50)`, `Text` → `TEXT`, `Boolean` → `BOOLEAN`.
- **Все ограничения целостности** настроены: PK, FK, UNIQUE на логине,
  NOT NULL на обязательных столбцах.
- **Для junction-таблиц** (`PsychologistSpecializations`)
  составной PK = (PsychologistID, CategoryName).

### 3.4. Изменение правила Update Constraint на ассоциациях

Для каждой ассоциации (двойной клик на линии FK) → вкладка **Integrity**:
- **Update constraint: Restrict** — иначе при попытке поменять
  естественный PK в родителе будет каскад, что часто нежелательно
  (но в нашем проекте PK — естественные строки, которые меняются редко;
  можно оставить Cascade).

### 3.5. Проверка PDM

**Tools → Check Model**. Корректная физическая модель готова к генерации
DDL.

---

## 4. Генерация SQL-скрипта

1. В открытом PDM: **Database → Generate Database**.
2. В диалоге:
   - **Directory** — каталог для сохранения `*.sql`.
   - **File name** — например, `mental_health.sql`.
3. На вкладке **Options**:
   - **Database**: установить **Drop database**.
   - **Group by → Table & Columns**:
     - **Table:** Create table, Check, Drop, Comment;
     - **Column:** Default Value, Check (Outside);
     - **Key:** Primary key (Outside), Drop primary key;
     - **Foreign key:** Foreign key (Outside), Drop foreign key;
     - **Index:** только Create index.
   - Если в этих галочках запутались — нажать **Setting set →
     Minimal → Save**.
4. **Preview** — посмотреть готовый SQL.
5. **OK** — `*.sql` сохранён.

Применить можно через `psql -h localhost -U postgres -d mental_health_db -f mental_health.sql` или через GUI pgAdmin (Query Tool → Open File → Execute).

---

## 5. Подключение к существующей БД (Reverse Engineering)

Это пригодится, если нужно собрать PDM **из уже существующей** БД —
например, из той, что создаст Spring-приложение проекта.

### 5.1. Настройка ODBC DSN

1. Запустить `C:\Windows\SysWOW64\odbcad32.exe` (32-битная версия,
   потому что PowerDesigner — 32-битное приложение).
2. Вкладка **User DSN → Add** → драйвер `PostgreSQL Unicode`
   (или `Microsoft SQL Server`, если БД на MS SQL).
3. **Data Source Name:** `MentalHealth`. **Server:** `localhost`.
   **Database:** `mental_health_db`. **User:** `postgres`. **Password:** `123`.
4. **Test Data Source** → Successful → **OK**.

### 5.2. Реверс-инжиниринг

1. В PowerDesigner: **File → Reverse Engineer → Database**.
2. Выбрать **DBMS = PostgreSQL** (или MS SQL).
3. Имя новой PDM-модели → **OK**.
4. В источнике: **Using a data source → ODBC source → выбрать `MentalHealth`**.
5. Ввести логин/пароль, **Connect**.
6. PowerDesigner покажет список найденных таблиц → выбрать все нужные
   (Roles, Users, Students, Psychologists, Tests, Questions, Answers,
   StressLevels, ResultStatuses, TestProtocols, ResultAnswers,
   Recommendations, PsychologistNotes, PsychologistSpecializations,
   TestCategories, AuditLog).
7. **OK** — диаграмма PDM построится автоматически.

### 5.3. Обратная генерация LDM и CDM

Из готового PDM можно собрать LDM (**Tools → Generate Logical Data
Model**) и из LDM — CDM (**Tools → Generate Conceptual Data Model**).
Полный обратный цикл: **PDM → LDM → CDM**.

---

## 6. Косметика и экспорт картинок для ПЗ

### 6.1. Выравнивание и цвета

- **Format → Fill / Line Style** — изменить заливку и контур сущностей.
- Сетка автоматически выравнивает. Если что-то пошло не так —
  **Edit → Undo**.
- На каждой модели: правый клик на пустом месте → **Align → Distribute
  Horizontally / Vertically** — выровнять выделенные сущности.

### 6.2. Экспорт в картинку

Для каждой модели:
1. Открыть нужную диаграмму.
2. **Edit → Export Image** (или **File → Export → Image**, в зависимости
   от версии).
3. Выбрать формат **GIF** или **PNG** (BMP тоже, но он тяжёлый и не
   масштабируется).
4. Указать имя файла:
   - `Рисунок 2 — Концептуальная модель.png`
   - `Рисунок 3 — Логическая модель.png`
   - `Рисунок 4 — Физическая модель.png`
5. Эти картинки вставить в `PZ_mental_health.docx` вместо текущих
   изображений (правый клик на старой картинке → **Изменить рисунок →
   Из файла**).

---

## 7. Генерация тестовых данных (опционально)

PowerDesigner может заполнить PDM-таблицы случайными значениями для
проверки модели:

1. В открытом PDM: **Database → Generate Test Data**.
2. **Selection:** выбрать таблицы (например, `Students`, `Tests`).
3. **Number of Rows** — сколько строк генерировать.
4. Можно сохранить в **SQL-script** (`INSERT`-ы) или применить сразу
   к подключённой через ODBC БД.
5. **OK**.

В нашем проекте этого делать обычно не нужно — `DataInitializer.java`
уже наполняет БД демо-данными при первом старте Spring-приложения.

---

## 8. Краткий чек-лист «сделать всё с нуля за час»

```
[ ] File → New Project → MentalHealthMonitoring.prj
[ ] File → New Model → CDM → MentalHealthCDM.cdm
[ ] Tools → Model Options → Crow Foot / Entity-Relationship
[ ] Tools → Display Preferences → Cardinality + Identifiers
[ ] Сущности: Students, Psychologists, Tests, Categories, StressLevels
[ ] Атрибуты каждой сущности + PK
[ ] Связи: Specializes (M:N), Supervises (1:N), TakesTest (тернарная M:N),
    Formulates (M:N)
[ ] Атрибуты связи TakesTest: TakenAt, TotalScore, ProtocolNumber
[ ] Tools → Check Model — без ошибок
[ ] File → Save All
[ ] Edit → Export Image → conceptual.png
[ ] Tools → Generate Logical Data Model → MentalHealthLDM.ldm
[ ] Tools → Check Model — без ошибок
[ ] Edit → Export Image → logical.png
[ ] Tools → Generate Physical Data Model → DBMS = PostgreSQL → MentalHealthPDM.pdm
[ ] Двойной клик по каждой ассоциации → Integrity → Update/Delete rule
[ ] Tools → Check Model — без ошибок
[ ] Edit → Export Image → physical.png
[ ] Database → Generate Database → mental_health.sql
[ ] Вставить картинки в PZ_mental_health.docx (Рисунки 2, 3, 4)
```

---

## 9. Если PowerDesigner недоступен

Уже сделанные у вас PUML/drawio-исходники концептуальной и логической
моделей (см. [README.md](README.md)) дают тот же результат на выходе —
PNG картинки в стиле ПЗ. PowerDesigner — это пожелание методички;
если преподаватель примет диаграммы из PlantUML / drawio (а они
содержательно идентичны), результат принципиально не отличается.

PowerDesigner — индустриальный коммерческий продукт, его сильная сторона —
**сквозной цикл** проектирования и обратная связь между моделями.
PlantUML / drawio дают **только картинки**, без модельной связности
между CDM и PDM. Если методичка требует именно PowerDesigner, идите по
шагам выше; если хватает картинок — оставьте PUML.
