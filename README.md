## Wordle-kot

Just some messing around in Kotlin to try to solve [Wordle](https://www.powerlanguage.co.uk/wordle/) more efficiently.

There are three strategies implemented with the following performance:

```
Frequency strategy...
Most rounds to completion: 9
Least rounds to completion: 1
Average rounds to completion: 3.7339092872570196
--------------------------------------------------
Brute scoring strategy...
Most rounds to completion: 9
Least rounds to completion: 1
Average rounds to completion: 3.7041036717062634
--------------------------------------------------
Brute minmax strategy...
Most rounds to completion: 9
Least rounds to completion: 1
Average rounds to completion: 4.075161987041037
```

### What do?

Try just running `interactive.kt`. It'll walk you through playing a game, telling you what to enter and asking for the hints you get back.

### Known issues:

1. The code is trash, and crammed into a few files, and just uses IntelliJ's build system.
2. There's something weird with the way that the game handles duplicate letters with yellows. Like I think that if there are 2 `E` in the solution and you guess a word with 3 `E`s then you can get only up to 2 yellows, and that gives you information. This code neither produces hints like that nor tries to use that extra information. 
3. The only persistence between guesses is the result of the previous guess, plus a running list of known-bad letters (since this was needed for convergence in many cases). To do this right you'd have to properly keep track of which letters cannot occur in which positions. For example, a yellow `E` in a certain position means that two guesses later we shouldn't try an `E` in the same place.
4. This is all far too slow to do a proper solve of the game, so the included strategies are just approximate. 
5. The strategies are pretty basic. I think even for an approximate solution, you'd want to start out using valid guess words and then shift to using only valid solution words later, and none of the strategies do that.  
