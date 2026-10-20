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
E((x,y,d),\operatorname{Avanzar}(n))
  &= ((x,y)+n\,v(d),d),\\
E((x,y,d),\operatorname{Girar}(s))
  &= (x,y,\operatorname{girar}(d,s)),\\
E(e,\operatorname{Secuencia}([])) &= e,\\
E(e,\operatorname{Secuencia}(o::os))
  &= E(E(e,o),\operatorname{Secuencia}(os)),\\
E(e,\operatorname{Repetir}(k,o)) &=
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
\operatorname{ejecutar}(e,o)=E(e,o).
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
coincide con $E((x,y,d),\operatorname{Girar}(s))$.

### 3.2. Secuencia

Se procede por inducción sobre la lista de órdenes.

**Caso base:** la lista es vacía. El programa devuelve el estado recibido.
La especificación también define
$E(e,\operatorname{Secuencia}([]))=e$.

**Paso inductivo:** sea la lista $o::os$. El programa ejecuta primero $o$ y
obtiene $\operatorname{ejecutar}(e,o)$. Después ejecuta la secuencia $os$
desde ese resultado. Por la hipótesis de inducción, procesar el resto equivale
a su semántica, y por la hipótesis de corrección para $o$ el primer resultado
es $E(e,o)$. Así:

$$
\begin{aligned}
\operatorname{ejecutar}(e,\operatorname{Secuencia}(o::os))
&= \operatorname{ejecutar}(
    \operatorname{ejecutar}(e,o),\operatorname{Secuencia}(os))\\
&= E(E(e,o),\operatorname{Secuencia}(os))\\
&= E(e,\operatorname{Secuencia}(o::os)).
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
\operatorname{ejecutar}(e,\operatorname{Repetir}(k,o))
=E^k(e,o).
$$

## 4. Punto 3: corrección de las medidas

### 4.1. `cuantasBasicas`

Sea $B(o)$ la cantidad de órdenes básicas que se ejecutan al ejecutar $o$.
La definición recursiva es:

$$
\begin{aligned}
B(\operatorname{Avanzar}(n)) &= 1,\\
B(\operatorname{Girar}(s)) &= 1,\\
B(\operatorname{Repetir}(k,o)) &=
  \begin{cases}
    0 & k\leq0,\\
    k\,B(o) & k>0,
  \end{cases}\\
B(\operatorname{Secuencia}([])) &= 0,\\
B(\operatorname{Secuencia}(o::os)) &= B(o)+B(\operatorname{Secuencia}(os)).
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
B(\operatorname{Repetir}(2,
  \operatorname{Secuencia}([\operatorname{Avanzar}(3),
  \operatorname{Girar}(\text{Izquierda})])))
=2(1+1)=4.
$$

### 4.2. `profundidad`

Sea $D(o)$ la profundidad estructural de la orden. Según la definición:

$$
\begin{aligned}
D(\operatorname{Avanzar}(n)) &= 1,\\
D(\operatorname{Girar}(s)) &= 1,\\
D(\operatorname{Repetir}(k,o)) &= 1+D(o),\\
D(\operatorname{Secuencia}([])) &= 1,\\
D(\operatorname{Secuencia}(os)) &= 1+\max_{o\in os}D(o)
  \quad\text{si }os\ne[].
\end{aligned}
$$

El programa devuelve uno para cada orden básica y para la secuencia vacía.
En `Repetir` añade uno a la profundidad de la orden interior. Para una
secuencia no vacía calcula las profundidades de sus elementos, toma el
máximo y suma uno. El caso vacío se separa antes de calcular el máximo, por
lo que nunca se aplica `max` a una lista vacía. Estas ecuaciones coinciden
con las ramas del `match`, así que la función calcula $D(o)$ por inducción
estructural.

La profundidad no depende de `veces`: el programa mide el nivel de
anidamiento del árbol de órdenes. Por ello, incluso `Repetir(0, o)` tiene la
profundidad $1+D(o)$.

## 5. Punto 4: corrección de las funciones de orden superior

### 5.1. `componer`

El programa construye
`Secuencia(List(o1, o2))`. Por la semántica de secuencia:

$$
E(e,\operatorname{componer}(o_1,o_2))
=E(E(e,o_1),o_2).
$$

Así, ejecutar la orden compuesta equivale a ejecutar primero $o_1$ y luego
$o_2$, sin invertir el orden ni omitir alguna de las dos órdenes.

### 5.2. `rutina`

Para valores $p$, $s$ y $k$, la función construye

$$
\operatorname{Repetir}\bigl(k,
  \operatorname{Secuencia}([\operatorname{Avanzar}(p),
  \operatorname{Girar}(s)])\bigr).
$$

Por la corrección de `ejecutar` para secuencias, una iteración de la rutina
aplica `Avanzar(p)` y luego `Girar(s)`. Por la corrección para `Repetir`, el
par de acciones se ejecuta $k$ veces si $k>0$, y no se ejecuta si $k\leq0$.
La rutina representa, por tanto, la composición repetida indicada por sus
argumentos.

En particular, si $F(e)=E(E(e,\operatorname{Avanzar}(p)),
\operatorname{Girar}(s))$, entonces:

$$
E(e,\operatorname{rutina}(p)(s)(k))=
\begin{cases}
e & k\leq0,\\
F^k(e) & k>0.
\end{cases}
$$

La firma curried conserva el tipo solicitado: después de fijar `pasos` y
`giro`, el argumento restante es `veces`, de tipo `Int`, y el resultado es
una `Orden`.

### 5.3. `aplicarA`

Para una lista de estados $[e_1,\ldots,e_m]$, `map` produce

$$
[\operatorname{ejecutar}(e_1,o),\ldots,
  \operatorname{ejecutar}(e_m,o)].
$$

Cada elemento se procesa de manera independiente, conserva su posición
dentro de la lista y recibe la misma orden. La lista vacía produce una lista
vacía. Por definición de `map`, el resultado satisface la especificación
elemento por elemento.

### 5.4. `transformar`

La especificación de esta función es estructural: reemplazar cada orden
básica mediante $f$, preservando las formas de las órdenes compuestas y el
orden de los elementos de las secuencias. El programa cumple esa
especificación por inducción estructural:

* Para `Avanzar` y `Girar`, devuelve $f(o)$.
* Para `Repetir(k,o)`, devuelve `Repetir(k, transformar(o, f))`: conserva
  $k$ y transforma el contenido.
* Para `Secuencia(Nil)`, devuelve la secuencia vacía.
* Para `Secuencia(o::os)`, transforma la cabeza y la cola y las vuelve a
  unir en el mismo orden.

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
construida por la propia función.

## 6. Punto 5: corrección de `simplificar`

Se demuestra la propiedad requerida:

$$
\forall o\in\operatorname{Orden}\;\forall e\in\operatorname{Estado}:
\quad E(e,o)=E(e,\operatorname{simplificar}(o)).
$$

La prueba es por inducción estructural sobre `Orden`. En los casos
compuestos, se usa la hipótesis de inducción para cada orden interior. Las
funciones auxiliares `noHaceNada` y `normalizar` se justifican primero.

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

### 6.2. Normalización de una secuencia

La función `normalizar` recibe una lista de órdenes que ya se simplificaron
recursivamente. Se demuestra por inducción sobre la lista que reemplaza su
secuencia por otra con la misma semántica.

**Lista vacía.** Devuelve la lista vacía. Ambas secuencias dejan el estado
sin cambios.

**Cabeza que no hace nada.** Si `noHaceNada` reconoce la cabeza, la función
la descarta y normaliza el resto. Como la cabeza deja el estado igual,
ejecutar la lista completa equivale a ejecutar solo el resto. La hipótesis
de inducción asegura que normalizar el resto conserva su resultado.

**Dos avances consecutivos.** Para dos órdenes
`Avanzar(a), Avanzar(b)`, la dirección no cambia entre ellas. Si la dirección
es $d$, el desplazamiento total es

$$
a\,v(d)+b\,v(d)=(a+b)\,v(d).
$$

Por ello, ejecutar ambas órdenes equivale a ejecutar `Avanzar(a + b)`.
`normalizar` reemplaza el par por ese avance y vuelve a procesarlo junto con
el resto. Si $a+b=0$, ese nuevo `Avanzar(0)` se reconoce como una orden que no
hace nada en la siguiente llamada y se elimina. La lista de la llamada
recursiva, formada por el avance combinado y el resto, es más corta que la
lista original. La hipótesis de inducción se aplica a esa lista menor; junto
con la equivalencia del par de avances, garantiza que el resultado completo
se conserva.

**Cualquier otra cabeza.** La función la conserva y normaliza la cola. La
semántica de una secuencia ejecuta esa cabeza seguida de la cola; por la
hipótesis de inducción, reemplazar la cola por su versión normalizada
preserva el estado final.

En cada rama, la recursión de `normalizar` termina: la rama general consume
una orden, la rama de eliminación reduce la lista y la rama de combinación
reemplaza dos avances por uno o por ninguno. Por lo tanto, la lista resultante
es finita y semánticamente equivalente a la recibida.

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
E(e,o)=E(e,\operatorname{simplificar}(o)).
$$

Una repetición de una vez tiene el mismo efecto que ejecutar su contenido
una vez, así que la sustitución conserva el resultado.

**`Repetir(k,o)` con $k>1$.** Sea $o'=\operatorname{simplificar}(o)$.
Por hipótesis de inducción, para cualquier estado $e'$,
$E(e',o)=E(e',o')$. En cada una de las $k$ iteraciones se aplica esta
igualdad al estado producido por la iteración anterior; por inducción sobre
$k$, se obtiene:

$$
E(e,\operatorname{Repetir}(k,o))
=E(e,\operatorname{Repetir}(k,o')).
$$

Si `noHaceNada(o')` es verdadero, $o'$ deja cualquier estado sin cambios.
Repetirlo un número positivo de veces también deja el estado igual, y el
programa devuelve `Secuencia(Nil)`, que tiene el mismo efecto. Si no es una
orden vacía, el programa conserva la repetición con el contenido
simplificado, ya probado equivalente.

### 6.5. Secuencias

Para `Secuencia(os)`, primero se aplica `simplificar` a cada elemento. Por
hipótesis de inducción, cada elemento se puede reemplazar por su versión
simplificada sin cambiar el efecto desde ningún estado. Como la secuencia
ejecuta sus elementos en orden, reemplazar cada elemento conserva el
resultado de toda la lista.

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
\boxed{E(e,o)=E(e,\operatorname{simplificar}(o))}
$$

Por consiguiente, simplificar reduce la representación de una orden sin
cambiar el resultado observable de ejecutarla.
