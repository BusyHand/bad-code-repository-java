## **Описание недостатка**
В коде жестко закодированы зависимости от конкретных значений enum `UserRole` через использование `ordinal()`. При добавлении новой роли необходимо изменять код в множестве мест, что является нарушением принципа открытости/закрытости (Open/Closed Principle).

## **Проблема с `UserRole` enum**
```java
public enum UserRole {
    ADMIN,        // ordinal = 0
    MANAGER,      // ordinal = 1
    COURIER       // ordinal = 2
    // Проблема: добавление новой роли изменит ordinal существующих ролей!
}
```

## **Конкретные примеры проблемного кода**

### **1. Проверка на конкретную роль через ordinal**

```java
@Service
@Transactional
public class DeliveryServiceImpl implements DeliveryService {
    
    @Override
    public DeliveryDto createDelivery(DeliveryRequest deliveryRequest) {
       ...
        if (courier.getRole().ordinal() != 2) { // предполагается, что COURIER = 2
            throw new IllegalArgumentException("Пользователь не является курьером");
        }
        ...
    }
    
    @Override
    public DeliveryDto updateDelivery(Long id, DeliveryRequest deliveryRequest) {
        ...
        if (courier.getRole().ordinal() != 2) {
            throw new IllegalArgumentException("Пользователь не является курьером");
        }
        ...
    }
    
    private void addComplexWarnings(...) {
        ...
        if (user.getRole().ordinal() == 0) { // предполагается, что ADMIN = 0
            warnings.add("Администратор не может создать доставки в праздники");
        }
        ...
    }
    
    private boolean validateGenerationConditions(...) {
        ...
        if (courier.getRole().ordinal() != 1) { // предполагается, что MANAGER = 1
            warnings.add("Пользователь не курьер");
            return false;
        }
        ...
    }
    ...
}
```

**Проблемы:**
- Магические числа 0, 1, 2 без контекста
- Дублирование проверок по всему коду
- Ошибка в логике: `ordinal() != 1` проверяет на MANAGER, а не на COURIER
- Непонятный смысл проверок

### **2. Проверка диапазона допустимых ролей**

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Override
    public List<UserDto> getAllUsers(UserRole role) {
        ...
        if (role.ordinal() < 0 || role.ordinal() > 2) {
            throw new IllegalArgumentException("Неправильная роль");
        }
        ...
    }
    
    @Override
    public UserDto createUser(UserRequest userRequest) {
        ...
        if (userRequest.getRole().ordinal() < 0 || userRequest.getRole().ordinal() > 2) {
            throw new IllegalArgumentException("Неправильная роль");
        }
        ...
    }
    
    @Override
    public UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        ...
        if (userUpdateRequest.getRole() != null &&
            (userUpdateRequest.getRole().ordinal() < 0 || userUpdateRequest.getRole().ordinal() > 2)) {
            throw new IllegalArgumentException("Неправильная роль");
        }
        ...
    }
    
    public List<UserDto> getAllUsersAgain(UserRole roleParam) {
        ...
        if (roleParam.ordinal() < 0 || roleParam.ordinal() > 2) {
            throw new IllegalArgumentException("Неправильная роль");
        }
        ...
    }
    ...
}
```

**Проблемы:**
- Одинаковая логика проверки в 4 разных методах
- Жесткая привязка к текущему количеству ролей (3)
- Неявное знание о количестве ролей в системе

## **Почему данный код является плохим**

- Классы не открыты для расширения (добавления новых ролей)
- Изменение порядка элементов в enum сломает всю логику
- Добавление новых элементов в начало/середину enum изменит ordinal существующих
- Одинаковые проверки разбросаны по разным классам и методам

## **К чему может привести наличие такого кода**

### **Катастрофические последствия при добавлении роли**
Представим, что нужно добавить новую роль `SUPERVISOR`:

```java
public enum UserRole {
    ADMIN,        // ordinal = 0 (не изменился)
    SUPERVISOR,   // ordinal = 1 (НОВЫЙ!)
    MANAGER,      // ordinal = 2 (ИЗМЕНИЛСЯ! был 1)
    COURIER       // ordinal = 3 (ИЗМЕНИЛСЯ! был 2)
}
```
**Все проверки сломаются:**
- `ordinal() != 2` теперь проверяет не COURIER, а MANAGER
- `ordinal() == 0` все еще ADMIN
- `ordinal() < 0 || ordinal() > 2` теперь пропускает COURIER (3 > 2)

## **Почему мог появиться данный недостаток**

- Недостаток знаний о enum в Java
- Непонимание, что `ordinal()` нестабилен при изменениях enum
- Незнание о существовании безопасных альтернатив
- Ошибочное убеждение, что ordinal постоянен