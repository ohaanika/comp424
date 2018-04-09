package student_player;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
	
	// Constants
	public final int MUSCOVITE = TablutBoardState.MUSCOVITE;
	public final int SWEDE = TablutBoardState.SWEDE;    
	private int player;
	private int opponent;
	
	// Constants (Alpha-Beta)
	private final int MAX_DEPTH = 3;
	private final int DEFAULT_ALPHA = Integer.MIN_VALUE;
	private final int DEFAULT_BETA = Integer.MAX_VALUE;
	
	// Constants (MCTS)
	private final int MCTS_TIME_LIMIT = 2000; // 2 seconds
    private final int MCTS_TIME_BUFFER = 800;
    private final int MAX_SIMULATION_TURNS = 20;
    
    // Constants (Heuristic) 
    private final int WEIGHT_KING_POSITION = 25;
    public static int[][] WEIGHT_KING_POSITION_BOARD = {
    	{200 ,1   ,20  ,20  ,20  ,20  ,20  ,1   ,200 },
    	{1   ,1   ,8   ,8   ,8   ,8   ,8   ,1   ,1   },
    	{20  ,8   ,4   ,4   ,4   ,4   ,4   ,8   ,20  },
    	{20  ,8   ,4   ,2   ,2   ,2   ,4   ,8   ,20  },
    	{20  ,8   ,4   ,2   ,0   ,2   ,4   ,8   ,20  },
    	{20  ,8   ,4   ,2   ,2   ,2   ,4   ,8   ,20  },
    	{20  ,8   ,4   ,4   ,4   ,4   ,4   ,8   ,20  },
    	{1   ,1   ,8   ,8   ,8   ,8   ,8   ,1   ,1   },
    	{200 ,1   ,20  ,20  ,20  ,20  ,20  ,1   ,200 } };
    private final int WEIGHT_KING_MOBILITY = 5;
    private final int WEIGHT_KING_SURROUNDED = 50;
    private final int WEIGHT_MUSCOVITE_PIECES = 9;
    private final int WEIGHT_SWEDES_PIECES = 15;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260662187");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
    	
    	/*
    	 * Consider minimax, alpha-beta, MCTS
    	 * Consider heuristics
    	 * Consider loading "pre-processed" opening strategies
    	 * Consider adding weights to each points of the board
    	 * Save 2 players to assess strategy updates against one another
    	 * 
    	 */
    	
    	// Evaluate the best possible move
    	Move myMove = strategy1(boardState);
    	
    	// Return your move to be processed by the server.
    	return myMove;
    	
    }
    
    /**
     * Calculate heuristic according to board state (for both strategies)
     * 
     * @param boardState
     * @return
     */
    
    public int evaluate(TablutBoardState boardState) {
    	int score = 0;
        if (boardState.getWinner() == player) {
        	score += 100000;
            return score;
        }
        else if (boardState.getWinner() == opponent) {
        	score -= 100000;
            return score;
        }
        else if (boardState.gameOver()) {
            return score;
        }
        else {
        	// Note weights declared above and the following 
        	int muscovitePieces = boardState.getNumberPlayerPieces(MUSCOVITE);
        	int swedePieces = boardState.getNumberPlayerPieces(SWEDE);
        	Coord king = boardState.getKingPosition();
        	
        	// Number of pieces: 
        	// (+) player pieces, (-) opponent pieces
        	if (player == MUSCOVITE) {
        		score += WEIGHT_MUSCOVITE_PIECES*muscovitePieces;
        		score -= WEIGHT_SWEDES_PIECES*swedePieces;
        		score += ((int)(Math.random()*11)-5);
        	}
        	else {
        		score += WEIGHT_SWEDES_PIECES*swedePieces;
        		score -= WEIGHT_MUSCOVITE_PIECES*muscovitePieces;
        		score -= ((int)(Math.random()*11)-5);
        	}
        	
        	// King position: 
        	// (+) player = SWEDES, (-) player = MUSCOVITES
	        if (player == SWEDE) {
	    		score += WEIGHT_KING_POSITION*WEIGHT_KING_POSITION_BOARD[king.x][king.y];
	    	} 
	    	else {
	    		score -= WEIGHT_KING_POSITION*WEIGHT_KING_POSITION_BOARD[king.x][king.y];
	    	} 
        	
        	// King mobility (has more options on where to move): 
        	// (+) player = SWEDES, (-) player = MUSCOVITES
        	if (player == SWEDE) {
        		score += WEIGHT_KING_MOBILITY*kingMobility(boardState);
        	} 
        	else {
        		score -= WEIGHT_KING_MOBILITY*kingMobility(boardState);
        	}
        	
        	// King surrounded by Muscovites: 
        	// (+) player = MUSCOVITES, (-) player = SWEDES
        	if (player == MUSCOVITE) {
        		score += WEIGHT_KING_SURROUNDED*kingSurrounded(boardState);
        	} 
        	else {
        		score -= WEIGHT_KING_SURROUNDED*kingSurrounded(boardState);
        	}

        	/*
	    	// Pieces being close to king: (-) player far, (+) opponent far
	    	for (Coord c: playerPieces) {
				score -= WEIGHT_PIECES_TO_KING * c.distance(king);
			}
			for (Coord c: opponentPieces) {
				score += WEIGHT_PIECES_TO_KING * c.distance(king);
			}     
        	*/
        	
        	return score;
        }
    }
    public int kingSurrounded(TablutBoardState boardState) {
    	int subScore = 0;
    	Coord king = boardState.getKingPosition();
    	List<Coord> neighbours = Coordinates.getNeighbors(king);
        for (Coord c: neighbours) {
            if (boardState.getPieceAt(c) == TablutBoardState.Piece.BLACK){
            	if (player == SWEDE) {
            		subScore--;
            	} 
            	else {
            		subScore++;
            	}
            }
        }
        return subScore;
    }
    public int kingMobility(TablutBoardState boardState) {
    	int subScore = 0;
    	Coord king = boardState.getKingPosition();
        for (int i = king.x - 1; i > 0; i--){
            if (boardState.coordIsEmpty(Coordinates.get(i, king.y))) {
            	subScore++;
            } else {
                break;
            }
        }
        for (int i = king.x + 1; i < 9; i++){
            if (boardState.coordIsEmpty(Coordinates.get(i, king.y))) {
            	subScore++;
            } else {
                break;
            }
        }
        for (int j = king.y - 1; j > 0; j--){
            if (boardState.coordIsEmpty(Coordinates.get(king.x, j))) {
            	subScore++;
            } else {
                break;
            }
        }
        for (int j = king.y + 1; j < 9; j++){
            if (boardState.coordIsEmpty(Coordinates.get(king.x, j))) {
            	subScore++;
            } else {
                break;
            }
        }
    	return subScore;
    }    
    
    /**
     * Strategy 1: Minimax & Alpha Beta Pruning
     * 
     * @param boardState
     * @return bestMove
     */
    public Move strategy1(TablutBoardState boardState) {
    	
    	// Set IDs for player and opponent 
    	// () holds the if, ? means then, : means else
    	player = player_id;
    	opponent = (player == MUSCOVITE) ? SWEDE : MUSCOVITE; 
    	
    	// Note all legal moves from current board state as options
    	List<TablutMove> options = boardState.getAllLegalMoves();
    	
    	// Let initial move be random
    	TablutMove bestMove = options.get(new Random(1848).nextInt(options.size()));
    	int bestValue = DEFAULT_ALPHA;
    	int value;
    	
    	long START_TIME = System.currentTimeMillis();
    	// Evaluate move options
    	for (TablutMove move : options) {
    		// Clone boardState and process move on clone
    		TablutBoardState cloneState = (TablutBoardState) boardState.clone();
    		cloneState.processMove(move);
    		// Evaluate through alpha-beta pruning and update bestValue (i.e. maxValue for maxPlayer)
            value = minimax(cloneState, DEFAULT_ALPHA, DEFAULT_BETA, MAX_DEPTH-1, false, START_TIME);
            if (value > bestValue) {
                bestMove = move;
                bestValue = value;
            }
    	}

        // Return best move (if breaching time limit, suffice with current best move)
    	return bestMove;
    }
    
    /**
     * Alpha Beta Pruning algorithm (recursive) 
     * 
     * @param boardState
     * @param alpha
     * @param beta
     * @param maxDepth
     * @param maxPlayer
     * @return bestValue
     */
    public int minimax(TablutBoardState boardState, int alpha, int beta, int maxDepth, boolean maxPlayer, long START_TIME) {
    	List<TablutMove> options = boardState.getAllLegalMoves();
    	int bestValue = DEFAULT_ALPHA;
    	int value;
    	
    	if (maxDepth == 0 || boardState.gameOver()){
    		return evaluate(boardState);
        }
    	
    	if (maxPlayer) {
    		//if ((System.currentTimeMillis() - START_TIME) < (AB_TIME_LIMIT - AB_TIME_BUFFER)) {
        	//	return bestValue;
        	//}
    		bestValue = DEFAULT_ALPHA;
    		for (TablutMove move : options) {
        		TablutBoardState cloneState = (TablutBoardState) boardState.clone();
        		cloneState.processMove(move);
        		value = minimax(cloneState, alpha, beta, maxDepth-1, false, START_TIME);
                bestValue = Math.max(value, bestValue);
                alpha = Math.max(alpha, bestValue);
                if (beta <= alpha) {
                	break;
                }
            }
    	}
    	else {
    		//if ((System.currentTimeMillis() - START_TIME) < (AB_TIME_LIMIT - AB_TIME_BUFFER)) {
        	//	return bestValue;
        	//}
    		bestValue = DEFAULT_BETA;
    		for (TablutMove move : options) {
        		TablutBoardState cloneState = (TablutBoardState) boardState.clone();
        		cloneState.processMove(move);
        		value = minimax(cloneState, alpha, beta, maxDepth-1, true, START_TIME);
                bestValue = Math.min(value, bestValue);
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) {
                	break;
                }
            }
        }
    	return bestValue;
    }
    
    /**
     * Strategy 2: Monte Carlo
     * 
     * @param boardState
     * @return bestMove
     */
    public Move strategy2(TablutBoardState boardState) {
    	
    	// Set IDs for player and opponent 
    	// () holds the if, ? means then, : means else
    	player = player_id;
    	opponent = (player == MUSCOVITE) ? SWEDE : MUSCOVITE; 
    	
    	// Initialize tree at root node and add all child nodes
    	Node rootNode = new Node(null, boardState, null);
    	rootNode.addChildren();
    	List<Node> children = rootNode.getChildren(); 
    	
    	// Check if there exists a move that results in a win
    	for (Node child: children) {
    		if (child.getBoardState().getWinner() == player) {
    			TablutMove winMove = child.getLastMove();
    			return winMove;
    		}
    	}
    	
    	// Check if there exists a move that results in a capture
    	int oldNumberOpponents = rootNode.getBoardState().getNumberPlayerPieces(opponent);
    	for (Node child: children) {
    		int newNumberOpponents = child.getBoardState().getNumberPlayerPieces(opponent);
    		if ((oldNumberOpponents - newNumberOpponents) > 0) {
    			TablutMove captureMove = child.getLastMove();
    			return captureMove;
    		}
    	}
    	
    	// Run Monte Carlo simulation until time limit
    	long START_TIME = System.currentTimeMillis();
        while ((System.currentTimeMillis() - START_TIME) < (MCTS_TIME_LIMIT - MCTS_TIME_BUFFER)) {
        	// (1) Selection
        	// (2) Expansion
        	// (3) Simulation
        	// (4) Backpropagation
        	for (Node child : children) {
        		Node promisingNode = select(child);
    			TablutBoardState playoutResult = simulate(promisingNode);
    			if (playoutResult.getWinner() == player) {
    				child.setScore(child.getScore() + 1);
    			}
        	}
        }
    	
        Node bestChild = getTopScore(children);
        Move bestMove = bestChild.getLastMove();
        return bestMove;
    }
    
    /**
     * MCTS SELECTION PHASE
     * Select most promising child node according to heuristic
     * 
     * @param node
     * @return bestChild
     */
    private Node select(Node node) {
    	Node selectedNode = node;
    	selectedNode.addChildren();
    	int bestScore = -1;
    	Node bestChild = null;
    	for (Node child: selectedNode.getChildren()) {
    		int score = evaluate(child.getBoardState());
    		if (score > bestScore) {
    			bestChild = child;
    		}
    	}
    	return bestChild;
    }
    
    /**
     * MCTS SIMULATION PHASE
     * 
     * Simulate a random play out until game ends
     * @param node
     * @return endState (board state at the end of the game)
     */
    private TablutBoardState simulate(Node node) {
    	TablutBoardState endState = (TablutBoardState) node.getBoardState().clone();
    	int initialTurnNumber = endState.getTurnNumber();
    	// Keep simulating while no winner declared and limit has not been reached
        while ((!endState.gameOver()) && ((endState.getTurnNumber() - initialTurnNumber) < MAX_SIMULATION_TURNS)) {
        	Random r = new Random(System.currentTimeMillis());
        	List<TablutMove> moves = endState.getAllLegalMoves();
        	endState.processMove(moves.get(r.nextInt(moves.size())));
        }
        return endState;
    }
    
    /**
     * Find the child with highest score among all children
     * 
     * @param children
     * @return topChild
     */
    private Node getTopScore(List<Node> children) {
    	Node topChild = children.get(0);
    	for (Node child: children) {
    		if (child.getScore() > topChild.getScore()) {
    			topChild = child;
    		}
    	}
    	return topChild;
    }
    
}