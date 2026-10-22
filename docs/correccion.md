# Informe de corrección: un robot en la cuadrícula

## 1. Especificación matemática

Un estado del robot es $e=(x,y,d)$, con posición $(x,y)$ y dirección
$d\in\{\text{Norte},\text{Este},\text{Sur},\text{Oeste}\}$. Para describir
los desplazamientos, se asocia a cada dirección un vector:

$$
v(\text{Norte})=(0,1),\quad
v(\text{Este})=(1,0),\quad
v(\text{Sur})=(0,-1),\quad
v(\text{Oeste})=(-1,0).
$$

La semántica de una orden se escribe $E(e,o)$ y es el estado que resulta al
ejecutar $o$ desde $e$. En particular:

$$
\begin{aligned}
E((x,y,d),\text{Avanzar}(n))
  &= ((x,y)+n\,v(d),d),\\
E((x,y,d),\text{Girar}(s))
  &= (x,y,\text{girar}(d,s)),\\
E(e,\text{Secuencia}([])) &= e,\\
E(e,\text{Secuencia}(o::os))
  &= E(E(e,o),\text{Secuencia}(os)),\\
E(e,\text{Repetir}(k,o)) &=
  \begin{cases}
    e & k\leq 0,\\
    E^{k}(e,o) & k>0.
  \end{cases}
\end{aligned}
$$

En la primera igualdad, $(x,y)+n\,v(d)$ indica la posición desplazada y la
dirección $d$ se conserva. $E^k(e,o)$ significa aplicar $o$ sucesivamente
$k$ veces, empezando en $e$.

La demostración considera los valores enteros del modelo del taller y la
semántica de las operaciones `Int` de Scala. La suma y la resta de
coordenadas, así como la suma de pasos, respetan las igualdades usadas incluso
si hay desbordamiento de `Int`, porque las operaciones se realizan con la
misma aritmética modular.

## 2. Punto 1: corrección de `girar`

La especificación de `girar(d,s)` es la dirección que resulta de rotar $d$
noventa grados hacia el sentido $s$. La función analiza la pareja
$(d,s)$ y cubre todas sus ocho combinaciones:

| Dirección | Izquierda | Derecha |
|---|---|---|
| Norte | Oeste | Este |
| Este | Norte | Sur |
| Sur | Este | Oeste |
| Oeste | Sur | Norte |

Cada resultado corresponde a la rotación cardinal indicada. Por tanto,
`girar` devuelve la dirección especificada para toda dirección y sentido
válidos. La función no recibe ni devuelve coordenadas; el punto de giro
preserva la posición cuando se usa desde `ejecutar`.

## 3. Punto 2: corrección de `ejecutar`

Se demuestra que, para todo estado $e$ y toda orden $o$,

$$
\text{ejecutar}(e,o)=E(e,o).
$$

La demostración se hace por casos sobre la estructura de `Orden`, con
inducción adicional sobre el número de repeticiones y la longitud de las
listas.

### 3.1. Órdenes básicas

**Caso `Avanzar(n)`.** La función examina la dirección del estado y suma o
resta $n$ en la coordenada correspondiente. Esto coincide en los cuatro
casos con el desplazamiento $n\,v(d)$ de la especificación. La dirección
permanece igual, y un $n$ negativo invierte el sentido del desplazamiento
mediante la misma suma o resta.

**Caso `Girar(s)`.** El resultado que produce el programa es
`Estado(x, y, girar(d, s))`. Por la corrección de `girar`, la orientación es
la rotación especificada, y las coordenadas $x,y$ no cambian. Por tanto,
coincide con $E((x,y,d),\text{Girar}(s))$.

### 3.2. Secuencia

Se procede por inducción sobre la lista de órdenes.

**Caso base:** la lista es vacía. El programa devuelve el estado recibido.
La especificación también define
$E(e,\text{Secuencia}([]))=e$.

**Paso inductivo:** sea la lista $o::os$. El programa ejecuta primero $o$ y
obtiene $\text{ejecutar}(e,o)$. Después ejecuta la secuencia $os$
desde ese resultado. Por la hipótesis de inducción, procesar el resto equivale
a su semántica, y por la hipótesis de corrección para $o$ el primer resultado
es $E(e,o)$. Así:

$$
\begin{aligned}
\text{ejecutar}(e,\text{Secuencia}(o::os))
&= \text{ejecutar}(
    \text{ejecutar}(e,o),\text{Secuencia}(os))\\
&= E(E(e,o),\text{Secuencia}(os))\\
&= E(e,\text{Secuencia}(o::os)).
\end{aligned}
$$

La ejecución respeta el orden de la lista.

### 3.3. Repetición

Si $k\leq0$, el programa devuelve $e$ sin ejecutar la orden interior. Es
exactamente el caso base de la especificación.

Para $k>0$, el programa ejecuta $o$ una vez y llama de nuevo a
`ejecutar` con `Repetir(k - 1, o)` desde el estado resultante. La recursión
termina porque el entero positivo decrece en uno hasta llegar a cero. Por
inducción sobre $k$, las $k-1$ ejecuciones restantes equivalen a
$E^{k-1}$; anteponer la primera ejecución produce $E^k$. Por tanto:

$$
\text{ejecutar}(e,\text{Repetir}(k,o))
=E^k(e,o).
$$

## 4. Punto 3: corrección de las medidas

### 4.1. `cuantasBasicas`

Sea $B(o)$ la cantidad de órdenes básicas que se ejecutan al ejecutar $o$.
La definición recursiva es:

$$
\begin{aligned}
B(\text{Avanzar}(n)) &= 1,\\
B(\text{Girar}(s)) &= 1,\\
B(\text{Repetir}(k,o)) &=
  \begin{cases}
    0 & k\leq0,\\
    k\,B(o) & k>0,
  \end{cases}\\
B(\text{Secuencia}([])) &= 0,\\
B(\text{Secuencia}(o::os)) &= B(o)+B(\text{Secuencia}(os)).
\end{aligned}
$$

El `match` del programa implementa exactamente estos casos. En las órdenes
básicas devuelve uno. En la repetición no positiva devuelve cero y, en la
positiva, multiplica las veces por el conteo recursivo interior. En la
secuencia suma el conteo de la cabeza y el de la cola; la lista vacía aporta
cero. Por inducción estructural sobre `Orden` y la lista, el valor calculado
es $B(o)$ para toda orden.

Por ejemplo:

$$
B(\text{Repetir}(2,
  \text{Secuencia}([\text{Avanzar}(3),
  \text{Girar}(\text{Izquierda})])))
=2(1+1)=4.
$$

### 4.2. `profundidad`

Sea $D(o)$ la profundidad estructural de la orden. Según la definición:

$$
\begin{aligned}
D(\text{Avanzar}(n)) &= 1,\\
D(\text{Girar}(s)) &= 1,\\
D(\text{Repetir}(k,o)) &= 1+D(o),\\
D(\text{Secuencia}([])) &= 1,\\
D(\text{Secuencia}(os)) &= 1+\max_{o\in os}D(o)
  \quad\text{si }os\ne[].
\end{aligned}
$$

El programa devuelve uno para cada orden básica y para la secuencia vacía.
En `Repetir` añade uno a la profundidad de la orden interior. Para una
secuencia no vacía, llama a `maxProfundidad`. Esta función auxiliar devuelve
cero para una lista vacía. Para una lista `head :: tail`, calcula
recursivamente $D(head)$ y el máximo de las profundidades de `tail`, y
devuelve el mayor de ambos valores. Por inducción sobre la longitud de la
lista, `maxProfundidad` devuelve exactamente la profundidad máxima de sus
órdenes. La rama de `Secuencia(Nil)` devuelve uno directamente, así que no
se usa el máximo de una lista vacía para calcular la profundidad de la
secuencia. Para una lista no vacía, sumar uno al máximo implementa $D(o)$.
Así, cada rama del `match` satisface la definición.

La profundidad no depende de `veces`: el programa mide el nivel de
anidamiento del árbol de órdenes. Por ello, incluso `Repetir(0, o)` tiene la
profundidad $1+D(o)$.

## 5. Punto 4: corrección de las funciones de orden superior

### 5.1. `componer`

El programa construye
`Secuencia(List(o1, o2))`. Por la semántica de secuencia:

$$
E(e,\text{componer}(o_1,o_2))
=E(E(e,o_1),o_2).
$$

Así, ejecutar la orden compuesta equivale a ejecutar primero $o_1$ y luego
$o_2$, sin invertir el orden ni omitir alguna de las dos órdenes.

### 5.2. `rutina`

Para valores $p$, $s$ y $k$, la función construye

$$
\text{Repetir}\bigl(k,
  \text{Secuencia}([\text{Avanzar}(p),
  \text{Girar}(s)])\bigr).
$$

Por la corrección de `ejecutar` para secuencias, una iteración de la rutina
aplica `Avanzar(p)` y luego `Girar(s)`. Por la corrección para `Repetir`, el
par de acciones se ejecuta $k$ veces si $k>0$, y no se ejecuta si $k\leq0$.
La rutina representa, por tanto, la composición repetida indicada por sus
argumentos.

En particular, si $F(e)=E(E(e,\text{Avanzar}(p)),
\text{Girar}(s))$, entonces:

$$
E(e,\text{rutina}(p)(s)(k))=
\begin{cases}
e & k\leq0,\\
F^k(e) & k>0.
\end{cases}
$$

La firma curried conserva el tipo solicitado: después de fijar `pasos` y
`giro`, el argumento restante es `veces`, de tipo `Int`, y el resultado es
una `Orden`.

### 5.3. `aplicarA`

Para una lista de estados $[e_1,\ldots,e_m]$, `aplicarA` se define
recursivamente. Para la lista vacía devuelve la lista vacía. Para una cabeza
$e_1$ y una cola $[e_2,\ldots,e_m]$, devuelve
`ejecutar(e_1, o)` seguido del resultado de la llamada recursiva sobre la
cola. Por inducción sobre la longitud de la lista, el resultado es:

$$
[\text{ejecutar}(e_1,o),\ldots,
  \text{ejecutar}(e_m,o)].
$$

Cada estado se procesa de manera independiente, recibe la misma orden y
conserva su posición en la lista. Por lo tanto, la función cumple la
especificación elemento por elemento.

### 5.4. `transformar`

La especificación de esta función es estructural: reemplazar cada orden
básica mediante $f$, preservando las formas de las órdenes compuestas y el
orden de los elementos de las secuencias. El programa cumple esa
especificación por inducción estructural:

* Para `Avanzar` y `Girar`, devuelve $f(o)$.
* Para `Repetir(k,o)`, devuelve `Repetir(k, transformar(o, f))`: conserva
  $k$ y transforma el contenido.
* Para `Secuencia(Nil)`, devuelve la secuencia vacía.
* Para `Secuencia(o::os)`, transforma la cabeza y transforma recursivamente
  `Secuencia(os)`. La llamada recursiva devuelve una secuencia; el programa
  extrae de ella la lista transformada y antepone la cabeza transformada,
  conservando el orden.

Por tanto, las órdenes básicas originales se sustituyen exactamente una vez
y la estructura compuesta original se recorre completa. No se afirma que
`transformar` conserve el comportamiento del robot para cualquier $f$: la
función está diseñada para cambiar órdenes básicas. Si $f$ es la identidad,
la orden transformada sí es igual a la original; para otras funciones, el
comportamiento depende de las órdenes que $f$ produzca.

En el caso no vacío de una secuencia, la llamada recursiva sobre la cola se
hace con `Secuencia(os)`. Por definición de las ramas de `transformar`, esta
llamada devuelve otra `Secuencia`, incluso si la cola está vacía o si $f$
produce órdenes compuestas para sus elementos. Por eso el patrón que extrae
la lista transformada de esa secuencia corresponde a la estructura
construida por la propia función. La inducción sobre la longitud de la lista
garantiza que todos sus elementos se transforman exactamente una vez y que
el orden relativo se conserva.

## 6. Punto 5: corrección de `simplificar`

Se demuestra la propiedad requerida:

$$
\forall o\in\text{Orden}\;\forall e\in\text{Estado}:
\quad E(e,o)=E(e,\text{simplificar}(o)).
$$

La prueba es por inducción estructural sobre `Orden`. En los casos
compuestos, se usa la hipótesis de inducción para cada orden interior. Las
funciones auxiliares `noHaceNada`, `simplificarLista` y `normalizar` se
justifican primero.

### 6.1. Órdenes que no hacen nada

La función `noHaceNada` reconoce tres formas:

1. `Avanzar(0)` conserva tanto la posición como la dirección.
2. `Repetir(k, o)` con $k\leq0$ no ejecuta la orden interior.
3. `Secuencia(Nil)` ejecuta cero órdenes.

En cada caso, para todo estado $e$:

$$
E(e,o)=e.
$$

Por tanto, sustituir cualquiera de esas órdenes por `Secuencia(Nil)` o
eliminarla de una secuencia no cambia el estado resultante.

### 6.2. Recorrido recursivo de la lista y normalización

La función `simplificarLista` recorre la lista con dos casos. Para la lista
vacía devuelve la lista vacía. Para `orden :: resto`, devuelve
`simplificar(orden) :: simplificarLista(resto)`. Por inducción sobre la
longitud de la lista, cada elemento de salida es la versión simplificada de
su correspondiente elemento de entrada, y el orden de los elementos se
conserva. Por la hipótesis de inducción estructural sobre `Orden`, cada
reemplazo conserva el efecto de ejecutar ese elemento. En consecuencia, la
secuencia completa antes y después de `simplificarLista` tiene el mismo
resultado.

La función `normalizar` recibe esa lista simplificada. Se demuestra por
inducción sobre la lista que su resultado representa una secuencia con la
misma semántica.

**Lista vacía.** Devuelve la lista vacía. Ambas secuencias dejan el estado
sin cambios.

**Cabeza que no hace nada.** Si `noHaceNada` reconoce la cabeza, `normalizar`
la descarta y se llama recursivamente con el resto. Como la cabeza deja el
estado igual, ejecutar la lista completa equivale a ejecutar solo el resto.
La hipótesis de inducción asegura que normalizar el resto conserva su
resultado.

**Cabeza `Avanzar(a)`.** El programa normaliza primero el resto. Si el
resultado de esa llamada empieza por `Avanzar(b)`, los dos avances son
contiguos en la lista normalizada. La dirección no cambia entre ellos; si la
dirección es $d$, el desplazamiento conjunto es

$$
a\,v(d)+b\,v(d)=(a+b)\,v(d).
$$

Por ello, sustituirlos por `Avanzar(a + b)` conserva el estado. El programa
vuelve a normalizar la lista que contiene el avance combinado y la cola, de
modo que el resultado también queda combinado con cualquier avance que le
siga. Si $a+b=0$, el avance combinado se reconoce como una orden que no hace
nada y se elimina. La lista recibida por esa llamada recursiva es más corta
que la lista en la que se detectó el par; la hipótesis de inducción sobre la
longitud garantiza que su normalización conserva el efecto.

Si el resto normalizado no empieza por un avance, `normalizar` conserva
`Avanzar(a)` delante de ese resto. La secuencia conserva su orden y su
semántica por la hipótesis de inducción aplicada al resto.

**Cualquier otra cabeza.** La función la conserva y normaliza la cola. La
semántica de una secuencia ejecuta esa cabeza seguida de la cola; por la
hipótesis de inducción, reemplazar la cola por su versión normalizada
preserva el estado final. En la implementación, el resto se procesa primero
en la rama de `Avanzar`; para las demás cabezas, se conserva la cabeza y se
normaliza recursivamente la cola, con el mismo argumento de equivalencia.

En cada rama, la recursión de `normalizar` termina. Las ramas de eliminación
y de conservación procesan una lista estrictamente menor. En la rama de
combinación, la llamada que normaliza el resto procesa una lista menor que la
original, y la llamada posterior recibe una lista con un avance en lugar del
par combinado, también menor que la lista original. Por tanto, la recursión
termina y la lista resultante es semánticamente equivalente a la recibida.

### 6.3. Órdenes básicas

**`Avanzar(0)`.** `simplificar` devuelve `Secuencia(Nil)`. Ambas órdenes dejan
el estado sin cambios.

**`Avanzar(n)` con $n\ne0$ y `Girar(s)`.** Se devuelve la orden original,
cuyo comportamiento es evidentemente el mismo.

### 6.4. Repeticiones

**`Repetir(k,o)` con $k\leq0$.** Se devuelve `Secuencia(Nil)`. La repetición
no ejecuta su contenido y la secuencia vacía tampoco cambia el estado.

**`Repetir(1,o)`.** El programa devuelve `simplificar(o)`. Por hipótesis de
inducción, para todo estado $e$:

$$
E(e,o)=E(e,\text{simplificar}(o)).
$$

Una repetición de una vez tiene el mismo efecto que ejecutar su contenido
una vez, así que la sustitución conserva el resultado.

**`Repetir(k,o)` con $k>1$.** Sea $o'=\text{simplificar}(o)$.
Por hipótesis de inducción, para cualquier estado $e'$,
$E(e',o)=E(e',o')$. En cada una de las $k$ iteraciones se aplica esta
igualdad al estado producido por la iteración anterior; por inducción sobre
$k$, se obtiene:

$$
E(e,\text{Repetir}(k,o))
=E(e,\text{Repetir}(k,o')).
$$

Si `noHaceNada(o')` es verdadero, $o'$ deja cualquier estado sin cambios.
Repetirlo un número positivo de veces también deja el estado igual, y el
programa devuelve `Secuencia(Nil)`, que tiene el mismo efecto. Si no es una
orden vacía, el programa conserva la repetición con el contenido
simplificado, ya probado equivalente.

### 6.5. Secuencias

Para `Secuencia(os)`, primero se aplica `simplificarLista` recursivamente a
cada elemento. Por hipótesis de inducción, cada elemento se puede reemplazar
por su versión simplificada sin cambiar el efecto desde ningún estado. Como
la secuencia ejecuta sus elementos en orden, reemplazar cada elemento
conserva el resultado de toda la lista.

Luego se aplica `normalizar`, cuya equivalencia se probó en la sección 6.2.
Finalmente:

* Si la lista normalizada está vacía, devolver `Secuencia(Nil)` conserva el
  efecto de la secuencia vacía.
* Si tiene un elemento, devolver ese elemento conserva el efecto porque una
  secuencia unitaria ejecuta precisamente esa orden.
* Si tiene dos o más elementos, devolver la secuencia conserva su orden y,
  por la corrección de `normalizar`, su efecto.

### 6.6. Conclusión

Todos los casos básicos preservan el estado. En los casos de `Repetir` y
`Secuencia`, las hipótesis de inducción permiten reemplazar las órdenes
interiores y los lemas de identidad y normalización justifican las
transformaciones adicionales. Por inducción estructural se concluye que,
para toda orden $o$ y todo estado $e$:

$$
\boxed{E(e,o)=E(e,\text{simplificar}(o))}
$$

Por consiguiente, simplificar reduce la representación de una orden sin
cambiar el resultado observable de ejecutarla.
