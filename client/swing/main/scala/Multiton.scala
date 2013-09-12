/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  Copyright  ©  2012  Christian Krause                                                         *
 *                                                                                               *
 *  Christian Krause  <christian.krause@ufz.de>                                                  *
 *                    <kizkizzbangbang@googlemail.com>                                           *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  This file is part of 'ClusterKit'.                                                           *
 *                                                                                               *
 *  This project is free software: you can redistribute it and/or modify it under the terms      *
 *  of the GNU General Public License as published by the Free Software Foundation, either       *
 *  version 3 of the License, or any later version.                                              *
 *                                                                                               *
 *  This project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;    *
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    *
 *  See the GNU General Public License for more details.                                         *
 *                                                                                               *
 *  You should have received a copy of the GNU General Public License along with this project.   *
 *  If not, see <http://www.gnu.org/licenses/>.                                                  *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


package ckit
package client
package swing

/** [[http://en.wikipedia.org/wiki/Multiton_pattern Multiton pattern]] for factory objects. Uses a
  * [[scala.collection.mutable.WeakHashMap]] (caching) and
  * [[http://en.wikipedia.org/wiki/Double-checked_locking double-checked locking]] (synchronisation
  * on the mutable `Map` for thread safety) internally.
  *
  * ==Usage==
  *
  * {{{
  * object MyMultitonFactory extends Multiton[Int,String] {
  *   override protected val create = (i: Int) ⇒ {
  *     i.toString
  *   }
  * }
  * }}}
  *
  * One may also want to use more than one argument:
  *
  * {{{
  * object MyMultitonFactory extends Multiton[(Int,Int),Int] with Function2[Int,Int,Int] {
  *   override protected val create = (p: (Int,Int)) ⇒ {
  *     p._1 + p._2
  *   }
  *
  *   override def apply(a: Int, b: Int) = apply((a,b))
  * }
  * }}}
  *
  * You will have to make all constructors `private` if you want to force that
  * every instance creation must go through `Multiton`:
  *
  * {{{
  * case class Foo private (number: String)
  *
  * object Foo extends Multiton[Int,Foo] {
  *   override protected val create = (i: Int) ⇒ {
  *     new Foo(i.toString)
  *   }
  * }
  * }}}
  *
  * @tparam A key/argument-type, instances should be immutable
  * @tparam B value/return-type
  *
  * @define key   key
  * @define value value
  */
trait Multiton[A,B] {
  /** Returns the cache. */
  private val instances = collection.mutable.WeakHashMap[A,B]()

  /** Returns the $value associated to the given $key. */
  final def apply(key: A): B = instances get key getOrElse {
    instances.synchronized {
      instances get key getOrElse {
        val value = create(key)
        instances += (key → value)
        value
      }
    }
  }

  /** Creates and returns a new $value. */
  protected val create: A ⇒ B
}
