package connectfour

import kotlin.properties.Delegates

const val DEFAULT_SIZE = "6 x 7"
const val RANGE = "5 to 9"

data class Player(val name: String, val marker: String, var score: Int)

data class Pos(val row: Int, val col: Int)

class ConnectFourGame {
    private var rows by Delegates.notNull<Int>()
    private var cols by Delegates.notNull<Int>()
    private lateinit var board: List<MutableList<String>>
    private val players = emptyList<Player>().toMutableList()
    private var turn = 0
    private var numberOfGames = 1
    private var gameNumber = 1

    init {
        initGame()
    }

    private fun initGame() {
        println("Connect Four")
        println("First player's name:")
        players.add(Player(readln(), "o", 0))
        println("Second player's name:")
        players.add(Player(readln(), "*", 0))
        var dimensions: List<String>
        while (true) {
            println("Set the board dimensions (Rows x Columns)\n" +
                    "Press Enter for default ($DEFAULT_SIZE)"
            )
            dimensions = readln().trim().split(Regex("\\s*[x|X]\\s*"))
            if (dimensions.size == 1 && dimensions.first() == "") {
                dimensions = DEFAULT_SIZE.split(" x ")
                break
            }
            if (dimensions.size != 2 || !Regex("\\d+").matches(dimensions[0]) || !Regex("\\d+").matches(dimensions[1])) {
                println("Invalid input")
            } else {
                if (dimensions[0].toInt() in RANGE.takeWhile { it != ' ' }.toInt()..RANGE.takeLastWhile { it != ' ' }.toInt() &&
                    dimensions[1].toInt() in RANGE.takeWhile { it != ' ' }.toInt()..RANGE.takeLastWhile { it != ' ' }.toInt()) {
                    break
                } else {
                    if (dimensions[0].toInt() !in RANGE.takeWhile { it != ' ' }.toInt()..RANGE.takeLastWhile { it != ' ' }.toInt()) {
                        println("Board rows should be from $RANGE")
                    }
                    if (dimensions[1].toInt() !in RANGE.takeWhile { it != ' ' }.toInt()..RANGE.takeLastWhile { it != ' ' }.toInt()) {
                        println("Board columns should be from $RANGE")
                    }
                }
            }
        }
        rows = dimensions[0].toInt()
        cols = dimensions[1].toInt()
        while (true) {
            println("Do you want to play single or multiple games?\n" +
                     "For a single game, input 1 or press Enter\n" +
                     "Input a number of games:"
            )
            val games = readln()
            if (games == "") break
            if (!Regex("\\d+").matches(games) || games == "0") {
                println("Invalid input")
            } else {
                numberOfGames = games.toInt()
                break
            }
        }
        println("${players[0].name} VS ${players[1].name}")
        println("$rows X $cols board")
        println(if (numberOfGames == 1) "Single game" else "Total $numberOfGames games\nGame #1")
        board = List(rows) { MutableList(cols) { " " } }
        println(this)
    }

    private fun column(col: Int): MutableList<String> {
        val columnContent = emptyList<String>().toMutableList()
        for (row in board.indices) {
            columnContent.add(board[row][col])
        }
        return columnContent
    }

    private fun diagonal(pos: Pos, pSlope: Boolean): MutableList<String> {
        var row = if (pSlope) (pos.row + pos.col).coerceAtMost(board.lastIndex) else (pos.row - pos.col).coerceAtLeast(0)
        var col = if (pSlope) (pos.row + pos.col - board.lastIndex).coerceAtLeast(0) else (pos.col - pos.row).coerceAtLeast(0)
        val diag = mutableListOf((pos.col - col + 1).toString())
        while (row in 0 until rows && col in 0 until cols) {
            diag.add(board[row][col])
            if (pSlope) { row--; col++ } else { row++; col++ }
        }
        return diag
    }

    private fun verticalWin(pos: Pos, mark: String): Boolean {
        return column(pos.col).subList(pos.row, (pos.row + 4).coerceAtMost(board.size)).count { it == mark } == 4
    }

    private fun horizontalWin(pos: Pos, mark: String): Boolean {
        return board[0].size - (board[pos.row].subList(0, pos.col).dropLastWhile { it == mark } +
               board[pos.row].subList(pos.col, board[0].size).dropWhile { it == mark }).size >= 4
    }

    private fun diagonalWin(pos: Pos, mark: String): Boolean {
        for (pSlope in listOf(true, false)) {
            val diag = diagonal(pos, pSlope)
            if (diag.size - (diag.subList(0, diag.first().toInt()).dropLastWhile { it == mark } +
                        diag.subList(diag.first().toInt(), diag.size).dropWhile { it == mark }).size >= 4
            ) return true
        }
        return false
    }

    private fun boardIsFull() = board.flatten().count { it == " " } == 0

    private fun win(lastMove: Int): Boolean {
        val pos = Pos(column(lastMove).firstFree() + 1, lastMove)
        val currentMarker = board[pos.row][pos.col]
        if (verticalWin(pos, currentMarker)) return true
        if (horizontalWin(pos, currentMarker)) return true
        if (diagonalWin(pos, currentMarker)) return true
        return false
    }

    private fun checkConditions(lastMove: Int): Boolean {
        if (win(lastMove)) {
            println("Player ${players[turn].name} won")
            players[turn].score += 2
            return true
        }
        if (boardIsFull()) {
            println("It is a draw")
            players[0].score += 1
            players[1].score += 1
            return true
        }
        return false
    }

    private fun MutableList<String>.isFull() = this.first() != " "

    private fun MutableList<String>.firstFree() = this.count { it == " " } - 1

    fun nextPlayersMove(): Boolean {
        println("${players[turn].name}'s turn:")
        val move = readln()
        when {
            move == "end" -> return false
            !Regex("\\d+").matches(move) -> println("Incorrect column number")
            move.toInt() !in 1..cols -> println("The column number is out of range (1 - $cols)")
            column(move.toInt() - 1).isFull() -> println("Column ${move.toInt()} is full")
            else -> { board[column(move.toInt() - 1).firstFree()][move.toInt() - 1] = players[turn].marker
                println(this)
                val gameFinished = checkConditions(move.toInt() - 1)
                turn = 1 - turn
                if (gameFinished) {
                    if (numberOfGames != 1) {
                        println("Score\n${players[0].name}: ${players[0].score} ${players[1].name}: ${players[1].score}")
                    }
                    if (gameNumber == numberOfGames) {
                        return false
                    } else {
                        gameNumber++
                        println("Game #$gameNumber")
                        board = List(rows) { MutableList(cols) { " " } }
                        println(this)
                        turn = (gameNumber - 1) % 2
                    }
                }
                return true
            }
        }
        return true
    }

    override fun toString(): String {
        var printBoard = " "
        for (col in 1..cols) {
            printBoard += "$col "
        }
        printBoard += "\n"
        for (row in 0 until  rows) {
            for (col in 0 until  cols) {
                printBoard += "║${board[row][col]}"
            }
            printBoard += "║\n"
        }
        printBoard += "╚═"
        for (col in 2..cols) {
            printBoard += "╩═"
        }
        printBoard += "╝"
        return printBoard
    }
}