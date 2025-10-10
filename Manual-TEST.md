### Схема потоков данных (упрощённо) для ввода кода 

User → AttemptService.processAttempt()
│
▼
CodeAttempt (новая попытка)
│
├─> Проверка Code (нормализация, поиск в level.codes)
│
├─ если WRONG → сохранить attempt
├─ если DUPLICATE → сохранить attempt
│
└─ если ACCEPTED:
│
├─ NORMAL → progress.sectorsClosed++
│             └─ если все сектора → LevelCompletion + progress.closedAt
│
├─ BONUS   → progress.bonusOnLevelSec += shift
│
└─ PENALTY → progress.penaltyOnLevelSec += shift
