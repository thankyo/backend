case class Converted(name: String)

trait Converter[A] {
  def perform: Converted
}

implicit def toConverted(name: String) = Converted("String")
implicit def toIntConverted(int: Int) = Converted("Int")

def f(needsConverted: Converted): String = needsConverted.name

f("some")
f(5)