package co.torri.scalamines

import scala.util.Random
import Math.abs
import java.util.LinkedList

/**
 * Created by IntelliJ IDEA.
 * User: lucastorri
 * Date: 1/21/11
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */

class MinesGameException extends Exception
class BombExplodedException extends MinesGameException
class InvalidNeighbourException extends MinesGameException

case class Square(mined: Boolean, position: (Int, Int)) {
    private var _revealed = false
    private var _neighbours = Map[(Int, Int), Square]()
    
    private var _flag = false;
    def flag_=(v: Boolean): Unit = if (!_revealed) _flag = v
    def flag = _flag

    def revealed = _revealed
    @throws(classOf[BombExplodedException])
    def reveal: Unit = {
        if (_flag) return
        _revealed = true
        if (mined) throw new BombExplodedException
        else if (#* == 0) revealNeighbours
    }
    def revealNeighbours: Unit = if (_revealed && #* == #^) _neighbours.foreach(n => if (!n._2._flag && !n._2.revealed) n._2.reveal)

    def #+(square: Square): Unit = {
        //TODO: create a match option Neighbour
        if (_neighbours.contains(square.position) || abs(square.position._1 - position._1) > 1 && abs(square.position._2 - position._2) > 1 || square.position == position) {
            throw new InvalidNeighbourException
        }
        _neighbours += ((square.position, square))
    }
    def #@(position: (Int, Int)): Square = _neighbours(position)
    def #*(): Int = _neighbours.filter((m) => m._2.mined).size
    def #?(): Int = _neighbours.size
    def #^(): Int = _neighbours.filter((m) => m._2._flag).size

    override def toString = {
        if (_revealed) {
            if (mined) "*" else #*.toString 
        } else if (_flag) {
            "F"
        } else {
            ""
        }
    }
}

class GameBoard(boardSize: Int) {

    type SquarePosition = (Int, Int)

    private var _squares: Array[Array[Square]] = {
        var squares = Array.ofDim[Square](boardSize, boardSize)
        var bombPlaces = generateRandomBombPlaces
        for (i <- Iterator.range(0, boardSize); j <- Iterator.range(0, boardSize)) {
            squares(i)(j) = Square(bombPlaces.contains((i, j)), (i, j))
        }
        organizeNeighbours(squares)
        squares
    }

    private def generateRandomBombPlaces: List[SquarePosition] = {
        var r = new Random()
        var randomBombPlaces = List[SquarePosition]()
        while (randomBombPlaces.size < boardSize) {
            var newPlace = (r.nextInt(boardSize), r.nextInt(boardSize))
            if (!randomBombPlaces.contains(newPlace)) randomBombPlaces ::= newPlace
        }
        randomBombPlaces
    }

    private def organizeNeighbours(squares: Array[Array[Square]]): Unit = {
        def square(i: Int, j: Int): Square = {
            val BoardSize = boardSize;
            (i, j) match {
                case (-1, _) | (BoardSize, _) | (_, -1) | (_, BoardSize) => null
                case _ => squares(i)(j)
            }
        }
        for (i <- Iterator.range(0, boardSize); j <- Iterator.range(0, boardSize)) {
            for (x <- Iterator.range(i - 1, i + 2); y <- Iterator.range(j - 1, j + 2); if (x, y) != (i, j) && square(x, y) != null) {
                squares(i)(j) #+ square(x, y)
            }
        }
    }

    def size: SquarePosition = (boardSize, boardSize)

    def square(position: SquarePosition): Square = position match { case (i, j) => _squares(i)(j) }

    def getSquare(i: Int, j: Int): Square = _squares(i)(j)

}

class MinesGame(boardSize: Int) {

    type SquarePosition = (Int, Int)

    lazy val board: GameBoard = new GameBoard(boardSize)

    def finished: Boolean = gameStatus(!_.revealed)

    def won: Boolean = gameStatus(s => !s.revealed && !s.revealed)

    private def gameStatus(f: (Square) => Boolean) = {
        (for (i <- Iterator.range(0, boardSize); j <- Iterator.range(0, boardSize); if f(board.square(i, j))) yield board.square(i, j)).size == boardSize
    }
}
