В коде реализованы 4 основные сущности `ShipGenerator`, `Tunnel`, `Port`, `CargoManager`. В объекте `Config` прописано какое количество кораблей каждого типа должен создать генератор.

1. `ShipGenerator` - с какой-то задержкой генерирует случайный корабль и отправляет его в `Tunnel` методом `send`.
2.  Класс `Tunnel` представляет из себя четыре thread-safe блокирующие очереди, одна из которых имеет ограниченную емкость - `awaitingBananaShips`, `awaitingClothesShips`, `awaitingBreadShips` и `tunnel`, а также `threadPool`. Очередь `tunnel` имеет ограниченную емкость соответствующую условию задачи (5 кораблей максимум в туннеле). Когда генератор кладет задачу в `Tunnel` она отправляется в очередь `tunnel`, при этом в threadPool планируется на выполнение через секунду задача - достать корабль из очереди `tunnel` и положить в очередь ожидающих разгрузки соответствующего типа.
3. Класс `Port` представляет из себя три потока - то есть причала, которые берут из очереди корабли соответствующего типа и выполняют их загрузку вызывая метод `run` на каждом корабле.
4. `CargoManager` - создает объекты всех классов выше, а также `Config`


Как происходит завершение программы? У `ShipGenerator` есть ограничение на количество кораблей, которое нужно создать, его он получает в конфиге. Каждый причал из порта также знает сколько кораблей каждого типа планируется на загрузку сегодня, после того как он обслуживает нужное число кораблей своего типа он завершает выполнение.