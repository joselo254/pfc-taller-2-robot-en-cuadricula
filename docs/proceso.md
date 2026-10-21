# Informe de proceso: un robot en la cuadrícula

## 1. Modelo del problema

El robot está representado por un estado

$$
e=(x,y,d),
$$

donde $x$ y $y$ son sus coordenadas enteras y $d$ es la dirección hacia la que
mira. El estado inicial es $(0,0,\text{Norte})$. Avanzar hacia el norte
aumenta $y$, avanzar hacia el este aumenta $x$; hacia el sur y el oeste se
restan pasos. Un paso negativo produce el desplazamiento contrario sin
cambiar la dirección.

Las órdenes son valores del tipo recursivo `Orden`:

* `Avanzar(n)` desplaza al robot $n$ casillas.
* `Girar(s)` cambia la dirección noventa grados, sin desplazarlo.
* `Repetir(k, o)` contiene una orden y un número de repeticiones.
* `Secuencia(os)` contiene una lista de órdenes, que se ejecutan de izquierda
  a derecha.

Las órdenes `Repetir` y `Secuencia` pueden contener otras órdenes. Por eso,
las funciones que las recorren consideran cada alternativa con `match` y
vuelven a llamarse para procesar las órdenes interiores.

## 2. Punto 1: `girar`

La función `girar(d, s)` recibe la dirección actual `d` y el sentido `s`.
Compara la pareja con los ocho casos posibles. El resultado es la dirección
posterior al giro; las coordenadas no forman parte de esta función.

| Dirección inicial | Izquierda | Derecha |
|---|---|---|
| Norte | Oeste | Este |
| Este | Norte | Sur |
| Sur | Este | Oeste |
| Oeste | Sur | Norte |

Por ejemplo, si el estado del robot es `(2, -1, Norte)`, un giro a la derecha
produce `(2, -1, Este)`. Un giro posterior a la izquierda vuelve a
`(2, -1, Norte)`. La posición se conserva en ambos pasos.

## 3. Punto 2: `ejecutar`

`ejecutar(e, o)` recibe un estado y una orden, y devuelve el nuevo estado.
El `match` distingue qué orden debe procesar.

### 3.1. Avanzar

Para `Avanzar(n)`, se inspecciona la dirección del estado. Solo cambia la
coordenada correspondiente:

| Dirección | Estado resultante |
|---|---|
| Norte | `(x, y + n, Norte)` |
| Sur | `(x, y - n, Sur)` |
| Este | `(x + n, y, Este)` |
| Oeste | `(x - n, y, Oeste)` |

Por ejemplo, desde `(3, 5, Oeste)`, `Avanzar(2)` devuelve `(1, 5, Oeste)`.
`Avanzar(-2)` desde el mismo estado devuelve `(5, 5, Oeste)`.

### 3.2. Girar

Para `Girar(sentido)`, se calcula la nueva dirección con `girar` y se
conservan las coordenadas. Desde `(0, 2, Norte)`, `Girar(Derecha)` devuelve
`(0, 2, Este)`.

### 3.3. Secuencia

Una secuencia no vacía se separa en su primera orden y el resto de la lista.
Primero se ejecuta la primera orden. Después se ejecuta la secuencia restante
desde el estado que resultó. Si la lista está vacía, el estado se devuelve
sin cambios.

Traza de
`Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))` desde el estado
inicial:

| Orden ejecutada | Estado resultante |
|---|---|
| Estado inicial | `(0, 0, Norte)` |
| `Avanzar(2)` | `(0, 2, Norte)` |
| `Girar(Derecha)` | `(0, 2, Este)` |
| `Avanzar(4)` | `(4, 2, Este)` |

### 3.4. Repetir

Si `veces` es cero o negativo, la orden no se ejecuta y el estado permanece
igual. Si es positivo, se ejecuta la orden una vez y luego se procesa
recursivamente `Repetir(veces - 1, orden)` desde el estado actualizado. Así,
cada llamada positiva reduce el número de repeticiones hasta alcanzar el caso
base.

Traza de
`Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))`. La columna
“Orden pendiente” indica qué queda por procesar después de cada acción:

| Paso | Acción ejecutada | Estado resultante | Orden pendiente |
|---|---|---|---|
| Inicio | — | `(0, 0, Norte)` | `Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))` |
| 1 | `Avanzar(1)` | `(0, 1, Norte)` | `Secuencia(List(Girar(Derecha)))` |
| 1 | `Girar(Derecha)` | `(0, 1, Este)` | `Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha))))` |
| 2 | `Avanzar(1)` | `(1, 1, Este)` | `Secuencia(List(Girar(Derecha)))` |
| 2 | `Girar(Derecha)` | `(1, 1, Sur)` | `Repetir(2, Secuencia(List(Avanzar(1), Girar(Derecha))))` |
| 3 | `Avanzar(1)` | `(1, 0, Sur)` | `Secuencia(List(Girar(Derecha)))` |
| 3 | `Girar(Derecha)` | `(1, 0, Oeste)` | `Repetir(1, Secuencia(List(Avanzar(1), Girar(Derecha))))` |
| 4 | `Avanzar(1)` | `(0, 0, Oeste)` | `Secuencia(List(Girar(Derecha)))` |
| 4 | `Girar(Derecha)` | `(0, 0, Norte)` | `Repetir(0, Secuencia(List(Avanzar(1), Girar(Derecha))))` |
| Fin | `Repetir(0, ...)` no ejecuta su contenido | `(0, 0, Norte)` | `Secuencia(Nil)` |

Al finalizar, el robot regresa al origen y queda mirando al norte.

## 4. Punto 3: medir órdenes

Estas funciones inspeccionan la estructura de la orden, no modifican un
estado del robot.

### 4.1. `cuantasBasicas`

Una orden básica, `Avanzar` o `Girar`, cuenta como una. Una repetición con
cero o menos veces cuenta cero; con un número positivo, multiplica por ese
número la cuenta de la orden interior. Para una secuencia, se suma la cuenta
de su primera orden y la del resto. La secuencia vacía cuenta cero.

Ejemplo:

```text
Repetir(2, Secuencia(List(Avanzar(3), Girar(Izquierda))))
```

La secuencia interior contiene dos órdenes básicas. Al repetirla dos veces,
la cuenta total es $2 \times (1+1)=4$.

### 4.2. `profundidad`

Una orden básica tiene profundidad uno. Una repetición tiene una capa más
que su orden interior. Una secuencia no vacía tiene una capa más que la
mayor profundidad de sus elementos; la secuencia vacía tiene profundidad uno.

Para el ejemplo anterior, `Avanzar(3)` y `Girar(Izquierda)` tienen profundidad
1; la secuencia tiene profundidad $1+1=2$; y la repetición, profundidad
$1+2=3$. El número de repeticiones no altera la profundidad: se mide el
anidamiento escrito en la estructura, no cuántas veces se ejecuta.

La siguiente traza muestra qué haría el robot al ejecutar esa orden, aunque
las dos funciones de este punto no recorren estados:

| Acción | Estado |
|---|---|
| Inicio | `(0, 0, Norte)` |
| Primera repetición: `Avanzar(3)` | `(0, 3, Norte)` |
| Primera repetición: `Girar(Izquierda)` | `(0, 3, Oeste)` |
| Segunda repetición: `Avanzar(3)` | `(-3, 3, Oeste)` |
| Segunda repetición: `Girar(Izquierda)` | `(-3, 3, Sur)` |

## 5. Punto 4: rutinas y funciones de orden superior

### 5.1. `componer`

`componer(o1, o2)` construye `Secuencia(List(o1, o2))`. No ejecuta ni
simplifica las órdenes: conserva ambas en el orden recibido. Al ejecutar el
resultado, primero se aplica `o1` y luego `o2`.

Por ejemplo, componer `Avanzar(2)` con `Girar(Derecha)` y ejecutar desde el
inicio da esta traza:

| Orden | Estado |
|---|---|
| Inicio | `(0, 0, Norte)` |
| `Avanzar(2)` | `(0, 2, Norte)` |
| `Girar(Derecha)` | `(0, 2, Este)` |

### 5.2. `rutina`

La función está currificada como `rutina(pasos)(giro)(veces)`. Con los
primeros dos argumentos fija el desplazamiento de cada paso y el sentido del
giro; el último argumento indica cuántas veces repetir la secuencia
`Avanzar(pasos)` seguido de `Girar(giro)`. El resultado tiene la forma
`Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))`.

Por ejemplo, `rutina(2)(Derecha)(2)` produce:

```text
Repetir(2, Secuencia(List(Avanzar(2), Girar(Derecha))))
```

Al ejecutarla desde el inicio:

| Acción | Estado |
|---|---|
| Inicio | `(0, 0, Norte)` |
| Primera repetición: `Avanzar(2)` | `(0, 2, Norte)` |
| Primera repetición: `Girar(Derecha)` | `(0, 2, Este)` |
| Segunda repetición: `Avanzar(2)` | `(2, 2, Este)` |
| Segunda repetición: `Girar(Derecha)` | `(2, 2, Sur)` |

### 5.3. `aplicarA`

`aplicarA(estados, o)` usa `map` para ejecutar la misma orden `o` sobre cada
estado y conservar el orden de la lista. Para una lista vacía el resultado
también es vacío.

Con `Avanzar(2)`:

| Estado de entrada | Estado resultante |
|---|---|
| `(0, 0, Norte)` | `(0, 2, Norte)` |
| `(5, 5, Sur)` | `(5, 3, Sur)` |

Por tanto, la lista de salida es
`List(Estado(0, 2, Norte), Estado(5, 3, Sur))`.

### 5.4. `transformar`

`transformar(o, f)` recorre la estructura. Si encuentra `Avanzar` o `Girar`,
aplica `f` a esa orden básica. Si encuentra una repetición, mantiene el
número de veces y transforma la orden interior. Si encuentra una secuencia,
transforma sus elementos en el mismo orden; una secuencia vacía permanece
vacía. La función puede producir cualquier `Orden` a partir de una básica,
por lo que el resultado de aplicar `f` puede ser también una orden compuesta.

Si `f` duplica los pasos de cada avance y deja los giros sin cambio, el
cuadrado de una casilla se transforma en:

```text
Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha))))
```

Traza de ejecución de la orden transformada:

| Acción | Estado |
|---|---|
| Inicio | `(0, 0, Norte)` |
| Avance de 2 al norte y giro | `(0, 2, Este)` |
| Avance de 2 al este y giro | `(2, 2, Sur)` |
| Avance de 2 al sur y giro | `(2, 0, Oeste)` |
| Avance de 2 al oeste y giro | `(0, 0, Norte)` |

## 6. Punto 5: `simplificar`

`simplificar(o)` procesa las órdenes interiores antes de normalizar una
secuencia. Tiene dos funciones auxiliares locales:

* `noHaceNada` reconoce `Avanzar(0)`, `Repetir` con cero o menos veces y
  `Secuencia(Nil)`.
* `normalizar` recorre la lista de órdenes de izquierda a derecha. Descarta
  las órdenes reconocidas como vacías; si encuentra dos avances consecutivos,
  los sustituye por un avance cuya cantidad es la suma y vuelve a procesar
  esa lista. En los demás casos conserva la primera orden y continúa con el
  resto.

Después de normalizar una lista, una lista vacía se representa como
`Secuencia(Nil)`, una lista con un elemento se reduce a ese elemento y una
lista con varios elementos se mantiene como `Secuencia`.

En `Repetir`, cero o menos veces se sustituye por `Secuencia(Nil)`. Una
repetición de una vez se sustituye por la versión simplificada de su orden.
Para más de una vez, se simplifica primero la orden interior; si esta no hace
nada, la repetición completa tampoco hace nada. En otro caso se conserva la
repetición con su interior simplificado.

Ejemplo con órdenes vacías que, al eliminarse, dejan dos avances contiguos:

```text
Secuencia(List(
  Avanzar(4),
  Secuencia(Nil),
  Repetir(0, Girar(Derecha)),
  Avanzar(-1)
))
```

El recorrido es:

1. `Avanzar(4)` queda igual.
2. `Secuencia(Nil)` queda como orden vacía.
3. `Repetir(0, Girar(Derecha))` se convierte en `Secuencia(Nil)`.
4. `Avanzar(-1)` queda igual.
5. `normalizar` elimina las dos órdenes vacías. Quedan `Avanzar(4)` y
   `Avanzar(-1)`, que son contiguos, por lo que se reemplazan por
   `Avanzar(4 + (-1))`, es decir, `Avanzar(3)`.
6. La secuencia tenía un único elemento después de normalizar; el resultado
   final es `Avanzar(3)`.

La traza del robot desde el estado inicial confirma el mismo resultado:

| Orden | Estado |
|---|---|
| Inicio | `(0, 0, Norte)` |
| `Avanzar(4)` | `(0, 4, Norte)` |
| Las dos órdenes vacías | `(0, 4, Norte)` |
| `Avanzar(-1)` | `(0, 3, Norte)` |
| Orden simplificada `Avanzar(3)` | `(0, 3, Norte)` |

La simplificación conserva el orden relativo de las órdenes que no elimina ni
combina. En el informe de corrección se demuestra que cada transformación
preserva el estado final para cualquier estado inicial.
