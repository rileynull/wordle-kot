val solutions = java.io.File("""wordlist_solutions.txt""").readLines().map { it.toUpperCase() }
val guesses = java.io.File("""wordlist_guesses.txt""").readLines().map { it.toUpperCase() }

enum class Hint {
    GREEN, YELLOW, GREY
}

/**
 * Produce colored hints for a guess with respect to a solution, just like the game does.
 */
fun markGuess(guess: String, correct: String): List<Hint> {
    val hints = MutableList(5) { _ -> Hint.GREY }

    for (i in 0..4) {
        if (guess[i] == correct[i]) {
            hints[i] = Hint.GREEN
        }
    }

    outer@for (i in 0..4) {
        if (hints[i] != Hint.GREEN) {
            for (j in 0..4) {
                if (guess[i] == correct[j] && hints[j] != Hint.GREEN) {
                    hints[i] = Hint.YELLOW
                    continue@outer
                }
            }
            hints[i] = Hint.GREY
        }
    }

    return hints
}

/**
 * Determines whether a word comports with a previous hint and checks against a blacklist of known bad letters.
 */
fun isWordValid(word: String, last: String, hint: List<Hint>, blacklist: Set<Char>): Boolean {
    val yellows = mutableListOf<Char>()
    val newBlacklist = blacklist.toMutableSet()

    outer@for (i in 0..4) {
        when (hint[i]) {
            Hint.GREEN ->
                if (word[i] != last[i]) return false
            Hint.YELLOW -> {
                if (word[i] == last[i]) return false
                yellows += last[i]
                for (j in 0..4) {
                    if (hint[j] != Hint.GREEN && word[j] == last[i]) continue@outer
                }
                return false
            }
            Hint.GREY ->
                newBlacklist += last[i]
        }
    }

    for (i in 0..4) {
        // Technically I think we should be checking the exact number of yellows for each character but w/e.
        if (hint[i] != Hint.GREEN && word[i] in blacklist && word[i] !in yellows) {
            return false;
        }
    }

    return true
}
