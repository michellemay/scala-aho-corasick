/**
 * node.scala
 *
 * @author <a href="mailto:jim@corruptmemory.com">Jim Powers</a>
 *
 * Copyright 2011 Jim Powers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.corruptmemory.aho_corasick

import scalaz._
import Scalaz._
import scala.collection.mutable.ArrayBuffer

case class NodeEntry[T](char:Char,node:Node[T])

case class Node[T](data:T,threshold:Int = 6) {
  var entries:Option[ArrayBuffer[NodeEntry[T]]] = none
  def +=(n:(Char,Node[T])):Node[T] = {
    insert(NodeEntry(n._1,n._2))
    this
  }
  def get(char:Char):Option[Node[T]] = {
    entries.flatMap {
      ab => {
        ab.length match {
          case 0 => none
          case 1 => if (char == ab(0).char) some(ab(0).node) else none
          case n => {
            if (char < ab(0).char || char > ab.last.char) none
            else if (n < threshold) {
              var i:Int = 0
              while (i < n && char != ab(i).char) i+=1
              if (i < n && ab(i).char == char) some(ab(i).node) else none
            } else {
              var t = n
              var b = 0
              var p = t/2
              while (p < n && char != ab(p).char && b < t && p < t) {
                if (char < ab(p).char) t = p
                else b = p
                val tb2 = (t + b)/2
                p = if (p == tb2) tb2+1 else tb2
              }
              if (char == ab(p).char) some(ab(p).node)
              else none
            }
          }
        }
      }
    }
  }
  def insert(node:NodeEntry[T]):Node[T] = {
    val char = node.char
    def findInsertIndex(ab:ArrayBuffer[NodeEntry[T]]):Int = {
      ab.length match {
        case 0 => 0
        case 1 =>
          if (char < ab(0).char) 0
          else 1
        case n => {
          if (char < ab(0).char) 0
          else if (char > ab.last.char) ab.length
          else if (n < threshold) {
            var i:Int = 0
            while (i < n && char >= ab(i).char) i += 1
            i
          } else {
            var t = n
            var b = 0
            var p = t/2
            while (p < n && char != ab(p).char && p < t) {
              if (char < ab(p).char) t = p
              else b = p
              val tb2 = (t + b)/2
              p = if (p == tb2) tb2+1 else tb2
            }
            p
          }
        }
      }
    }
    def doInsert(ab:ArrayBuffer[NodeEntry[T]]):ArrayBuffer[NodeEntry[T]] = {
      ab.insert(findInsertIndex(ab),node)
      ab
    }
    entries.fold({
                   entries = some(ArrayBuffer(node))
                   this
                 })(
                 ab => {
                   doInsert(ab)
                   this
                 })
  }
}
