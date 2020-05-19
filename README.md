# CSCI561_AI_HalmaGamePlayingAgent
  Halma game playing agent.<br/>**Algorithm used:** Minimax Game playing algorithm with alpha-beta pruning.
  
  ![](images/halma.gif)
  
  ## About the Game
  Halma is a strategy board game invented in 1883 or 1884 by George Howard Monks, a US thoracic surgeon at Harvard Medical School. His inspiration was the English game Hoppity which was devised in 1854. The gameboard is checkered and divided into 16×16 squares. Pieces may be small checkers or counters, or wooden or plastic cones or men resembling small chess pawns.[2] Piece colors are typically black and white for two-player games, and various colors or other distinction in games for four players.
  
  ## Details about Agent program
  The agent program uses a game playing algortihm called as **minimax algorithm** with alpha-beta pruning. The program receives an input file (eg.input.txt) as input which specifies the intital board configuration. The program searches for the best possible move upto depth 3 and returns the **best possible move** according to some **heuristic function**. The agent can switch between depths 1 or 3 depending on the board configuration. This heuristic function plays a significant role in determining the best move. The heuristic function calculates the value for each board configuration possible when every possible move is played. The move with best utility value calculated by the heuristic function is selected for playing. The best move is returned as an output file (eg.output.txt).
