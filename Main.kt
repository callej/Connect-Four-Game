package connectfour

fun main() {
    val game = ConnectFourGame()
    while (game.nextPlayersMove()) {}
    println("Game over!")
}