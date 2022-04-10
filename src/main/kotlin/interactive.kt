fun main() {
    println("Computing first move...")

    val blacklist = mutableSetOf<Char>()
    var lastWord = bruteScoringStrategy(blacklist = blacklist)!!

    println("Okay, to start, play the word $lastWord.")

    round@while (true) {
        println("Please enter the feedback that you got from the game for $lastWord.")
        println("Use G (Green), Y (Yellow), and A (grAy) in their correct positions.")
        println("For example, GGYGA would indicate that we have green, green, yellow, green, gray.")
        print("Input: ")
        val input = readLine()!!.toUpperCase()
        if (input == "GGGGG") {
            println("Yay!")
            break;
        }

        val hints = mutableListOf<Hint>()
        for (i in 0..4) {
            hints += when (input[i]) {
                'G' -> Hint.GREEN
                'Y' -> Hint.YELLOW
                'A' -> {
                    blacklist += lastWord[i]
                    Hint.GREY
                }
                else -> continue@round
            }
        }

        println("Computing next move...")
        val nextWord = bruteScoringStrategy(lastWord, hints, blacklist)
        if (nextWord == null) {
            println("Sorry, there are no words left in the word list which satisfy the constraints.")
            break
        }
        println("Your next word is ${nextWord.toUpperCase()}. Play it now.")
        println("---------------------------------------------------------------------------------")
        lastWord = nextWord
    }
}