package taller

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 *
 * Tal como está, el proyecto compila y las pruebas quedan en rojo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion = (d,s) match {
    case (Norte, Izquierda)=> Oeste
    case (Norte, Derecha)=> Este
    case (Sur, Izquierda)=> Este
    case (Sur, Derecha)=> Oeste
    case (Oeste, Izquierda)=> Sur
    case (Oeste, Derecha) =>  Norte
    case (Este, Izquierda)=> Norte
    case (Este, Derecha)=> Sur
  }

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(e: Estado, o: Orden): Estado = (e,o) match{

    case (Estado(x, y, orientacion), Avanzar(n)) =>
      orientacion match {
        case Norte => Estado(x, y + n, Norte)
        case Sur   => Estado(x, y - n, Sur)
        case Este  => Estado(x + n, y, Este)
        case Oeste => Estado(x - n, y, Oeste)
      }

    case (Estado(x, y, orientacion), Girar(sentido)) =>
      Estado(x, y, girar(orientacion, sentido))

    case (estadoActual, Secuencia(ord :: rest)) =>
      val siguienteEstado = ejecutar(estadoActual, ord)
      ejecutar(siguienteEstado, Secuencia(rest))


    case (estadoActual, Secuencia(Nil)) =>
      estadoActual

    case (estadoActual, Repetir(veces, orden)) =>
      if (veces <= 0) estadoActual
      else {
        val siguienteEstado = ejecutar(estadoActual, orden)
        ejecutar(siguienteEstado, Repetir(veces - 1, orden))
      }





  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int = o match {
    case (Avanzar(n)) => 1

    case (Girar(sentido)) => 1

    case (Repetir(veces, orden)) =>
      if (veces <= 0) 0
    else {
      veces * cuantasBasicas(orden)
    }

    case (Secuencia(Nil)) => 0

    case (Secuencia(head :: tail)) =>
      cuantasBasicas(head) + cuantasBasicas(Secuencia(tail))

  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
  def profundidad(o: Orden): Int = o match {
    case (Avanzar(n)) => 1

    case (Girar(sentido)) => 1

    case (Repetir(veces, orden)) =>
      1 + profundidad(orden)

    case (Secuencia(Nil)) => 1

    case (Secuencia(ordenes)) =>
      1 + ordenes.map(profundidad).max

  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden = ???

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden = ???

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] = ???

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
  def transformar(o: Orden, f: Orden => Orden): Orden = ???

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden = ???
}
