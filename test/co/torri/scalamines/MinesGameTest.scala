package co.torri.scalamines

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ Spec, FlatSpec }

@RunWith(classOf[JUnitRunner])
class MinesGameTest extends Spec with ShouldMatchers {

    def findBombs(board: GameBoard): List[(Int, Int)] =
        (for (i <- Iterator.range(0, board.size._1); j <- Iterator.range(0, board.size._2); if board.square((i, j)).mined) yield (i, j)).toList

    describe("A Mines Game") {

        it("should have a square board") {

            val board: GameBoard = new MinesGame(10).board
            board.size should be((10, 10))
        }

        it("should be able to check if it was finished when won") {
            val game: MinesGame = new MinesGame(2)
            game should not be('won)
            val board = game.board
            var bombs = findBombs(board)
            var bombFreeSquares = List((0, 0), (0, 1), (1, 0), (1, 1)) -- bombs
            bombs.size should be(2)

            game should not be ('finished)
            board.square(bombs(0)).flag = true
            game should not be ('finished)
            board.square(bombs(1)).flag = true
            game should not be ('finished)

            board.square(bombFreeSquares(0)).reveal
            game should not be ('finished)
            board.square(bombFreeSquares(1)).reveal
            game should be('finished)
            game should be('won)

            board.square(bombs(0)).flag = false
            game should be('finished)
            game should be('won)
            board.square(bombs(1)).flag = false
            game should be('finished)
            game should be('won)
        }
    }

    describe("A Mines Board") {

        val tenByTenGameBoard = new MinesGame(10).board

        def iterateOverBoardSquares[T](board: GameBoard)(f: (Square) => T) = {
            board.size match {
                case (width, height) =>
                    for (i <- Iterator.range(0, width); j <- Iterator.range(0, height)) {
                        f(board.square((i, j)))
                    }
            }
        }

        it("should have 'boardSize^2' unrevealed squares") {
            iterateOverBoardSquares(tenByTenGameBoard) { square =>
                square.revealed should be(false)
            }
        }

        it("should have 'boardSize' random placed mines") {
            var mineCountInGame1 = 0
            var minesPlacesInGame1 = List[Tuple2[Int, Int]]()
            iterateOverBoardSquares(tenByTenGameBoard) { square =>
                square match { case Square(true, (i, j)) => mineCountInGame1 += 1; minesPlacesInGame1 ::= (i, j); case _ => }
            }
            mineCountInGame1 should be(10)

            var mineCountInGame2 = 0
            var minesPlacesInGame2 = List[Tuple2[Int, Int]]()
            iterateOverBoardSquares(new MinesGame(10).board) { square =>
                square match { case Square(true, (i, j)) => mineCountInGame2 += 1; minesPlacesInGame2 ::= (i, j); case _ => }
            }
            mineCountInGame2 should be(10)

            minesPlacesInGame1.sorted should not equals (minesPlacesInGame2.sorted)
        }

        it("should let squares know their neighbours") {
            iterateOverBoardSquares(tenByTenGameBoard) { square =>
                square.position match {
                    case (0, 0) | (0, 9) | (9, 9) | (9, 0) => (square #?) should be(3)
                    case (0, _) | (9, _) | (_, 0) | (_, 9) => (square #?) should be(5)
                    case _ => (square #?) should be(8)
                }
            }

            var firstSquare = tenByTenGameBoard.square((0, 0))
            (firstSquare #@ (1, 0)) should not be (null)
            (firstSquare #@ (1, 1)) should not be (null)
            (firstSquare #@ (0, 1)) should not be (null)
        }

    }

    describe("A square") {

        it("can be revealed") {
            var square = Square(false, (0, 0))
            square.revealed should be(false)
            square.reveal
            square.revealed should be(true)
        }

        it("should throw an exception if it has a bomb and it's revealed") {
            var square = Square(true, (0, 0))
            evaluating { square.reveal } should produce[BombExplodedException]
        }

        describe("when dealing with neighbour squares") {

            it("should not reveal when flagged") {
                var square = Square(false, (1, 1))
                square.flag = true
                square.reveal
                square.revealed should be(false)
            }

            it("can add neighbours") {
                var square = Square(false, (1, 1))
                for (i <- Iterator.range(0, 3); j <- Iterator.range(0, 3) if (i, j) != (1, 1)) {
                    var neighbour = Square(false, (i, j))
                    square #+ (neighbour)
                    square #@ (i, j) should be(neighbour)
                }
            }

            it("can't add squares that are not near by") {
                var square = Square(false, (1, 1))
                evaluating { square #+ Square(false, (3, 3)) } should produce[InvalidNeighbourException]
            }

            it("can't add repeated neighbours or itself") {
                var square = Square(false, (1, 1))
                square #+ Square(false, (0, 0))
                evaluating { square #+ Square(false, (0, 0)) } should produce[InvalidNeighbourException]
                evaluating { square #+ Square(false, (1, 1)) } should produce[InvalidNeighbourException]
            }

            it("should know how many neighbours have mines") {
                var square = Square(false, (1, 1))
                (square #*) should be(0)
                square #+ Square(false, (0, 0))
                (square #*) should be(0)
                square #+ Square(true, (1, 0))
                (square #*) should be(1)
                square #+ Square(true, (0, 1))
                (square #*) should be(2)
            }

            it("can be flagged and unflagged") {
                var square = Square(false, (1, 1))
                square.flag should be(false)
                square.flag = true
                square.flag should be(true)
            }

            it("should know how many neighbours have flags") {
                var square = Square(false, (1, 1))
                (square #^) should be(0)
                var otherSquare = Square(false, (0, 0))
                square #+ (otherSquare)
                (square #^) should be(0)
                otherSquare.flag = true
                (square #^) should be(1)
                otherSquare = Square(true, (0, 1))
                square #+ (otherSquare)
                (square #^) should be(1)
                otherSquare.flag = true
                (square #^) should be(2)
            }

            it("can't reveal neighbours if it wasn't revealed") {
                var square = Square(false, (1, 1))
                var otherSquare = Square(false, (0, 0))
                square #+ (otherSquare)
                square.revealNeighbours
                otherSquare should not be ('revealed)

                square.reveal
                square.revealNeighbours
                otherSquare should be('revealed)
            }

            it("should reveal neighbours that don't have mines and when #* == #^") {
                var squares = Map[(Int, Int), Square]()
                def addSquare(square: Square, mined: Boolean, position: (Int, Int)): Unit = {
                    var newSquare = Square(mined, position)
                    squares += ((position, newSquare))
                    square #+ (newSquare)
                }
                var square = Square(false, (1, 1))
                addSquare(square, true, (0, 0))
                addSquare(square, true, (0, 1))
                addSquare(square, false, (0, 2))
                addSquare(square, false, (1, 2))
                addSquare(square, false, (2, 2))
                addSquare(square, false, (2, 1))
                addSquare(square, false, (2, 0))
                addSquare(square, false, (1, 0))

                def countRevealedSquares(): Int = squares.filter(e => e._2.revealed).size

                square.reveal
                square.revealNeighbours
                countRevealedSquares should be(0)
                squares((0, 0)).flag = true
                square.revealNeighbours
                countRevealedSquares should be(0)
                squares((0, 1)).flag = true
                square.revealNeighbours
                countRevealedSquares should be(6)
                squares((0, 0)) should not be ('revealed)
                squares((0, 1)) should not be ('revealed)
            }

            it("should override toString method") {
                var square = Square(false, (1, 1))
                square.toString should be("")
                square #+ (Square(true, (0, 0)))
                square.toString should be("")
                square.reveal
                square should be('revealed)
                square.toString should be("1")

                square = Square(false, (1, 1))
                square.flag = true
                square.reveal
                square.toString should be("F")
                square.flag = false
                square.toString should be("")

                square = Square(false, (1, 1))
                square #+ (Square(true, (0, 0)))
                square #+ (Square(true, (0, 1)))
                square.reveal
                square.flag = true
                square.toString should be("2")
            }

            it("should not be able to change flag value after it was revealed") {
            	var square = Square(false, (1, 1))
            	square.reveal
            	square.flag = true
            	square.flag should be (false)
            }
            
            it("should open empty neighbours recursively")  {
            	var square1 = Square(false, (0, 0))
            	var square2 = Square(false, (1, 1))
            	var square3 = Square(false, (2, 2))
            	
            	square1 #+ square2
            	square2 #+ square1
            	square2 #+ square3
            	square3 #+ square2
            	
            	square1.reveal
            	square1 should be ('revealed)
            	square2 should be ('revealed)
            	square3 should be ('revealed)
            }
        }
    }
}