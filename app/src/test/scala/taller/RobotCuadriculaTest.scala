package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))

  // Punto 1 -------------------------------------------------------------------

  test("girar: Norte a la derecha da Este") { assert(girar(Norte, Derecha) == Este) }
  test("girar: Norte a la izquierda da Oeste") { assert(girar(Norte, Izquierda) == Oeste) }
  test("girar: Oeste a la derecha da Norte") { assert(girar(Oeste, Derecha) == Norte) }
  test("girar: Sur a la izquierda da Este") { assert(girar(Sur, Izquierda) == Este) }

  test("girar: cuatro giros al mismo lado vuelven al principio") {
    val cuatro = List(Derecha, Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Norte)
  }

  test("girar: un giro y su contrario se cancelan") {
    assert(girar(girar(Este, Derecha), Izquierda) == Este)
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar tres al norte") {
    assert(ejecutar(inicial, Avanzar(3)) == Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de avance, giro y avance") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))) ==
      Estado(4, 2, Este))
  }

  test("ejecutar: el cuadrado vuelve al punto de partida") {
    assert(ejecutar(inicial, cuadrado) == Estado(0, 0, Norte))
  }

  test("ejecutar: avanzar con paso negativo retrocede") {
    assert(ejecutar(inicial, Avanzar(-2)) == Estado(0, -2, Norte))
  }

  test("ejecutar: repetir cero veces no hace nada") {
    assert(ejecutar(inicial, Repetir(0, Avanzar(5))) == inicial)
  }

  test("ejecutar: la secuencia vacía no hace nada") {
    assert(ejecutar(inicial, Secuencia(Nil)) == inicial)
  }

  test("ejecutar: avanzar al sur disminuye y") {
    assert(ejecutar(Estado(5, 5, Sur), Avanzar(2)) == Estado(5, 3, Sur))
  }

  test("ejecutar: avanzar al oeste disminuye x") {
    assert(ejecutar(Estado(5, 5, Oeste), Avanzar(2)) == Estado(3, 5, Oeste))
  }

  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: una orden básica cuenta uno") {
    assert(cuantasBasicas(Avanzar(3)) == 1)
  }

  test("cuantasBasicas: el cuadrado ejecuta ocho básicas") {
    assert(cuantasBasicas(cuadrado) == 8)
  }

  test("cuantasBasicas: repetir cero veces no ejecuta nada") {
    assert(cuantasBasicas(Repetir(0, cuadrado)) == 0)
  }

  test("cuantasBasicas: la secuencia vacía no ejecuta nada") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("profundidad: una orden básica tiene profundidad 1") {
    assert(profundidad(Avanzar(3)) == 1)
  }

  test("profundidad: una secuencia de básicas tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Avanzar(1), Girar(Derecha)))) == 2)
  }

  test("profundidad: el cuadrado tiene profundidad 3") {
    assert(profundidad(cuadrado) == 3)
  }

  test("profundidad: la secuencia vacía tiene profundidad 1") {
    assert(profundidad(Secuencia(Nil)) == 1)
  }

  test("profundidad: manda la rama más honda") {
    assert(profundidad(Secuencia(List(Avanzar(1), cuadrado))) == 4)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: deja las dos órdenes en secuencia") {
    assert(componer(Avanzar(1), Girar(Derecha)) ==
      Secuencia(List(Avanzar(1), Girar(Derecha))))
  }

  test("componer: ejecutar la compuesta es ejecutar una y después la otra") {
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) ==
      ejecutar(ejecutar(inicial, Avanzar(2)), Girar(Derecha)))
  }

  test("rutina: currificada, el cuadrado de cuatro vuelve al origen") {
    val cuadradoDe = rutina(1)(Derecha) _
    assert(ejecutar(inicial, cuadradoDe(4)) == Estado(0, 0, Norte))
  }

  test("rutina: con los mismos argumentos construye el cuadrado del enunciado") {
    assert(rutina(1)(Derecha)(4) == cuadrado)
  }

  test("aplicarA: ejecuta la misma orden sobre varios estados") {
    assert(aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2)) ==
      List(Estado(0, 2, Norte), Estado(5, 3, Sur)))
  }

  test("aplicarA: sobre la lista vacía no hay nada que hacer") {
    assert(aplicarA(Nil, Avanzar(2)) == Nil)
  }

  test("transformar: duplicar los avances deja los giros como estaban") {
    val doble = transformar(cuadrado, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }

  test("transformar: con la identidad no cambia nada") {
    assert(transformar(cuadrado, (o: Orden) => o) == cuadrado)
  }

  // Punto 5 -------------------------------------------------------------------

  test("simplificar: dos avances seguidos se juntan") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(3)))) == Avanzar(5))
  }

  test("simplificar: repetir una vez es la orden misma") {
    assert(simplificar(Repetir(1, Avanzar(4))) == Avanzar(4))
  }

  test("simplificar: avanzar cero desaparece de la secuencia") {
    assert(simplificar(Secuencia(List(Avanzar(0), Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta solo los avances contiguos") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3)))) ==
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3))))
  }

  test("simplificar: repetir cero veces no hace nada") {
    assert(simplificar(Repetir(0, Avanzar(9))) == Secuencia(Nil))
  }

  test("simplificar: dos avances que se cancelan desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2)))) == Secuencia(Nil))
  }

  test("simplificar: es idempotente") {
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Secuencia(List(Avanzar(0), Avanzar(4)))),
      Secuencia(List(Secuencia(Nil), Repetir(0, Avanzar(3)))))
    assert(ordenes.forall(o => simplificar(simplificar(o)) == simplificar(o)))
  }

  test("simplificar: no cambia el resultado de ejecutar") {
    val estados = List(inicial, Estado(3, -2, Este), Estado(-1, 4, Sur))
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(2), Avanzar(3))),
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Avanzar(4)),
      Repetir(0, Avanzar(9)),
      Secuencia(List(Avanzar(0), Girar(Derecha))),
      Secuencia(List(Avanzar(2), Avanzar(-2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(2)), Girar(Izquierda)))))
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: el anidamiento interior también se reduce") {
    assert(simplificar(Repetir(3, Repetir(1, Avanzar(2)))) == Repetir(3, Avanzar(2)))
  }

 //  5 pruebas del punto numero 1--------------------------

  test("girar: Sur a la derecha da Oeste") {assert(girar(Sur, Derecha) == Oeste)}
  test("girar: Norte  a la Izquierda da Oeste") {assert(girar(Norte, Izquierda) == Oeste)}

  test("girar: un giro,su contrario y otro contrario da la primera orden") {
    assert(girar(girar(girar(Norte, Derecha), Izquierda),Derecha) == Este)
  }

  test("girar: Norte dos veces a la derecha da Sur") {
    assert(girar(girar(Norte, Derecha), Derecha) == Sur)
  }
  test("girar: verificar que girar en direcciones opuestas con el mismo sentido da resultados simétricos") {
    assert(girar(Norte, Derecha) == Este && girar(Sur, Izquierda) == Este)
  }

  //  5 pruebas del punto numero 2--------------------------

  test("ejecutar: avanzar al este desde una posición distinta de cero") {
    assert(ejecutar(Estado(2, 3, Este), Avanzar(4)) == Estado(6, 3, Este))
  }

  test("ejecutar: secuencia larga con giros alternados y avance") {
    val ordenes = Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(2), Girar(Derecha), Avanzar(1)))
    assert(ejecutar(inicial, ordenes) == Estado(2, 2, Sur))
  }

  test("ejecutar: avanzar 10 al norte") {
    assert(ejecutar(inicial, Avanzar(10)) == Estado(0, 10, Norte))
  }

  test("ejecutar: avanzar con cero pasos mantiene la misma posición y orientación") {
    assert(ejecutar(Estado(4, 7, Oeste), Avanzar(0)) == Estado(4, 7, Oeste))
  }
  test("ejecutar: giro suelto hacia la izquierda desde el sur") {
    assert(ejecutar(Estado(1, 1, Sur), Girar(Izquierda)) == Estado(1, 1, Este))
  }

  //  5 pruebas del punto numero 3--------------------------

  test("cuantasBasicas: la secuencia ejecuta cuatro básicas") {
    val secuencia = Secuencia(List(Girar(Izquierda), Repetir(2,Avanzar(3)), Girar(Derecha)))
    assert(cuantasBasicas(secuencia) == 4)
  }
  test("cuantasBasicas: repetir 3 veces avanzar y girar ejecuta 6 básicas") {
    assert(cuantasBasicas(Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha))))) == 6)
  }
  test("cuantasBasicas: repetir 2 veces una secuencia vacía no ejecuta nada"){
    assert(cuantasBasicas(Repetir(2, Secuencia(Nil))) == 0)
  }
  test("profundidad: la secuencia tiene profundidad 3") {
    val secuencia = Secuencia(List(Girar(Izquierda), Repetir(2,Avanzar(3)), Girar(Derecha)))
    assert(profundidad(secuencia) == 3)
  }
  test("profundidad: una secuencia de cuadrados tiene profundidad 4") {
    assert(profundidad(Secuencia(List(cuadrado, cuadrado, cuadrado))) == 4)
  }

  //  5 pruebas del punto numero 4--------------------------

  test("componer: crea una secuencia de las dos secuencias") {
    val secuencia1 = Secuencia(Avanzar(3) :: Girar(Izquierda) :: Nil)
    val secuencia2 = Secuencia(Repetir(2, Girar(Derecha)) :: Avanzar(5) :: Nil)
    val resultado = Secuencia(secuencia1 :: secuencia2 :: Nil)
    assert(componer(secuencia1, secuencia2) == resultado)
  }
  test("rutina: se fijan siete pasos y un giro, y se repite 2 veces") {
    val secuencia = Repetir(2, Secuencia(Avanzar(7) :: Girar(Derecha) :: Nil))
    assert(rutina(7)(Derecha)(2) == secuencia)
  }
  test("aplicarA: se gira a la izquierda en los estados") {
    assert(aplicarA(List(Estado(2,-3,Oeste), Estado(5,1,Este), Estado(-3,-9,Norte)), Girar(Izquierda)) ==
      List(Estado(2,-3,Sur), Estado(5,1,Norte), Estado(-3,-9,Oeste)))
  }
  test("transformar: los pasos se multiplican por tres y los giros cambian su sentido") {
    val secuencia = Secuencia(List(Girar(Derecha), Avanzar(3), Girar(Izquierda)))
    val resultado = transformar(secuencia, {
      case (Avanzar(n)) => Avanzar(3 * n)
      case (Girar(Izquierda)) => Girar(Derecha)
      case (Girar(Derecha)) => Girar (Izquierda)
    })
    assert(resultado ==
      Secuencia(List(Girar(Izquierda), Avanzar(9), Girar(Derecha))))
  }
  test("transformar: si hay giro a la izquierda se avanza 5 pasos") {
    val secuencia = Secuencia(List(Girar(Izquierda), Girar(Derecha)))
    val resultado = transformar(secuencia, {
      case (Girar(Izquierda)) => Secuencia(List(Girar(Izquierda), Avanzar(5)))
      case otro => otro
    })
    assert(resultado ==
    Secuencia(List(Secuencia(List(Girar(Izquierda), Avanzar(5))), Girar(Derecha))))
  }

  //  5 pruebas adicionales del punto numero 5--------------------------

  test("simplificar: elimina órdenes vacías y combina los avances que quedan contiguos") {
    val orden = Secuencia(List(
      Avanzar(4),
      Secuencia(Nil),
      Repetir(-2, Girar(Derecha)),
      Avanzar(-1)
    ))
    assert(simplificar(orden) == Avanzar(3))
  }

  test("simplificar: repetir una secuencia que queda vacía también queda vacío") {
    val orden = Repetir(4, Secuencia(List(Avanzar(5), Avanzar(-5))))
    assert(simplificar(orden) == Secuencia(Nil))
  }

  test("simplificar: reduce el interior de una repetición sin cambiar sus veces") {
    val orden = Repetir(3, Secuencia(List(
      Secuencia(List(Avanzar(2), Avanzar(4))),
      Avanzar(0),
      Girar(Izquierda)
    )))
    assert(simplificar(orden) ==
      Repetir(3, Secuencia(List(Avanzar(6), Girar(Izquierda)))))
  }

  test("simplificar: secuencia unitaria dentro de una repetición se reduce") {
    assert(simplificar(Repetir(3, Secuencia(List(Girar(Derecha))))) ==
      Repetir(3, Girar(Derecha)))
  }

  test("simplificar: al quitar órdenes nulas la secuencia queda en su única orden") {
    val orden = Secuencia(List(Avanzar(0), Girar(Derecha), Secuencia(Nil)))
    assert(simplificar(orden) == Girar(Derecha))
  }
}
