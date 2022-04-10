// Tuning parameters which control how heavily we value green matches vs yellow matches.
const val EXACT_MATCH_POINTS = 28
const val ANAGRAM_MATCH_POINTS = 10

/**
 * Find a guess with letters that occur frequently in the same positions as in a lot of the remaining solutions.
 */
fun frequencyStrategy(last: String? = null, hint: List<Hint>? = null, blacklist: Set<Char>): String? {
    fun filter(word: String): Boolean {
        if (last == null || hint == null) return true
        return isWordValid(word, last, hint, blacklist)
    }

    // Build occurrence counts.
    val overallCounts = mutableMapOf<Char, Int>()
    val countsPerPosition = mutableListOf<MutableMap<Char, Int>>(
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf()
    )
    for (word in solutions.filter(::filter)) {
        for (i in 0..4) {
            overallCounts.merge(word[i], 1) { old, new -> old + new }
            countsPerPosition[i].merge(word[i], 1) { old, new -> old + new }
        }
    }

    // Score guesses. Yes it's actually better to restrict guesses to only valid solutions.
    val scoredGuesses = solutions.filter(::filter).map { guess ->
        val seenLetters = mutableSetOf<Char>()

        var score = 0
        for (i in 0..4) {
            val exactCount = countsPerPosition[i][guess[i]] ?: 0
            val anywhereCount = overallCounts[guess[i]] ?: 0

            score += EXACT_MATCH_POINTS * exactCount
            if (guess[i] !in seenLetters) {
                score += ANAGRAM_MATCH_POINTS * (anywhereCount - exactCount)
                seenLetters += guess[i]
            }
        }
        Pair(guess, score)
    }

    return scoredGuesses.maxByOrNull { it.second }?.first
}

/**
 * Try all of the possible guesses against all the remaining solutions and find the one with the overall most helpful hints.
 */
fun bruteScoringStrategy(last: String? = null, hint: List<Hint>? = null, blacklist: Set<Char>): String? {
    fun filter(word: String): Boolean {
        if (last == null || hint == null) return true
        return isWordValid(word, last, hint, blacklist)
    }

    val validSolutions = solutions.filter(::filter)
    if (validSolutions.isEmpty()) {
        return null
    }

    return solutions.filter(::filter).maxByOrNull { guess ->
        validSolutions.sumBy { solution ->
            val hints = markGuess(guess, solution)
            val seenYellows = mutableListOf<Char>()
            hints.withIndex().sumBy { (i, hint) -> when (hint) {
                Hint.GREEN -> EXACT_MATCH_POINTS
                Hint.YELLOW -> {
                    if (solution[i] !in seenYellows) {
                        seenYellows += solution[i]
                        ANAGRAM_MATCH_POINTS
                    } else 0
                }
                else -> 0
            } }
        }
    }
}

/**
 * Try all of the possible guesses against all the remaining solutions and find the guess that minimizes the worst case
 * number of solutions that could remain.
 */
fun bruteMinMaxStrategy(last: String? = null, hint: List<Hint>? = null, blacklist: Set<Char>): String? {
    if (last == null || hint == null) {
        return "ARISE" // Vast, vast speedup from precomputing this.
    }

    val validSolutions = solutions.filter { isWordValid(it, last, hint, blacklist) }
    if (validSolutions.isEmpty()) {
        return null
    }

    return solutions.filter { isWordValid(it, last, hint, blacklist) }
            .minByOrNull { guess ->
                validSolutions.maxOf { solution ->
                    val newHint = markGuess(guess, solution)
                    validSolutions.count { isWordValid(it, guess, newHint, blacklist) }
                }
            }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Driver to auto-run through a game for a single solution word, printing the steps along the way.
fun main_() {
    fun hintToString(hint: List<Hint>): String {
        return hint.fold("") { acc, cur ->
            acc + when (cur) {
                Hint.GREEN -> "G"
                Hint.YELLOW -> "Y"
                Hint.GREY -> "A"
            }
        }
    }

    val answer = "COVER" // BOXER VOTER MOWER all also take the maximum guesses with this strategy
    var lastGuess: String? = null
    var lastHint = listOf<Hint>()
    val blacklist = mutableSetOf<Char>()
    while (lastHint.any { it != Hint.GREEN }) {
        lastGuess = bruteMinMaxStrategy(lastGuess, lastHint, blacklist)!!
        lastHint = markGuess(lastGuess, answer)
        lastHint.withIndex().filter { it.value == Hint.GREY }.mapTo(blacklist) { lastGuess[it.index] }
        println("Guess: $lastGuess")
        println("Hint: ${hintToString(lastHint)}")
        println("Blacklist: $blacklist")
    }
}

// Driver to evaluate the performance of each strategy.
fun main() {
    fun evaluateStrategy(bestWordStrategy: (String?, List<Hint>?, Set<Char>) -> String?) {
        val roundsToCompletion = solutions.map { solution ->
            val blacklist = mutableSetOf<Char>()
            var lastGuess: String? = null
            var lastHint: List<Hint>? = null
            var rounds = 0
            do {
                lastGuess = bestWordStrategy(lastGuess, lastHint, blacklist)!!
                lastHint = markGuess(lastGuess, solution)
                lastHint.withIndex().filter { it.value == Hint.GREY }.mapTo(blacklist) { lastGuess[it.index] }
                rounds++

                // It can get stuck occasionally, for example on YYYYY anagrams.
                if (rounds == 15) {
                    println("Got stuck on $solution. Penalty applied.")
                    rounds = 1000000
                    break
                }
            } while (lastHint!!.any { it != Hint.GREEN })
            rounds
        }

        val average = roundsToCompletion.sum().toDouble() / roundsToCompletion.size
        println("Most rounds to completion: ${roundsToCompletion.maxOrNull()}")
        println("Least rounds to completion: ${roundsToCompletion.minOrNull()}")
        println("Average rounds to completion: $average")
    }

//    for (i in 0..20) {
//        println("Trying EXACT_MATCH_POINTS = $EXACT_MATCH_POINTS.")
//        evaluateStrategy(::bruteScoringStrategy)
//        EXACT_MATCH_POINTS++
//    }

    println("Frequency strategy...")
    evaluateStrategy(::frequencyStrategy)
    println("--------------------------------------------------")

    println("Brute scoring strategy...")
    evaluateStrategy(::bruteScoringStrategy)
    println("--------------------------------------------------")

    println("Brute minmax strategy...")
    evaluateStrategy(::bruteMinMaxStrategy)
    println("--------------------------------------------------")
}

// Driver to evaluate the strength of each starting word with bruteScoringStrategy.
fun main__() {
    guesses.forEach { guess ->
        val score = solutions.sumBy { solution ->
            val hints = markGuess(guess, solution)
            val seenYellows = mutableListOf<Char>()
            hints.withIndex().sumBy { (i, hint) -> when (hint) {
                Hint.GREEN -> EXACT_MATCH_POINTS
                Hint.YELLOW -> {
                    if (solution[i] !in seenYellows) {
                        seenYellows += solution[i]
                        ANAGRAM_MATCH_POINTS
                    } else {
                        0
                    }
                }
                else -> 0
            } }
        }
        println("$score $guess")
    }
}