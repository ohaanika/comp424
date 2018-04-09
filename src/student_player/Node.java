package student_player;

// Representing a node in the search tree

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class Node {
	
	private TablutBoardState boardState;
	private TablutMove lastMove;
	private Node parent;
	private List<Node> children;
	private int score;
	
	/**
	 * Constructor: 
	 * @param parent, boardState, move
	 */
    public Node(Node parent, TablutBoardState boardState, TablutMove move) {
    	this.boardState = boardState;
    	this.lastMove = move;
        this.children = new ArrayList<Node>();
    }
    
    /**
     * Create children nodes and adds them to the children list
     */
    public void addChildren() {
    	List<TablutMove> options = boardState.getAllLegalMoves();
    	for (TablutMove move : options) {
    		// Clone state and process move on clone to produce child board state
    		TablutBoardState childState = (TablutBoardState) boardState.clone();
    		childState.processMove(move);
    		// Create a child node with following data (parent node, board state, processed move)
    		Node childNode = new Node(this, childState, move);
    		// Add this child node to the list of children under the current parent
    		children.add(childNode);
    	}
    }
    
    /**
     * Accessor method to get all the child nodes.
     * @return list of child nodes
     */
    public List<Node> getChildren() {
    	return children;
    }
    
    /**
     * Accessor method to get the board state of the node.
     * @return board state of the node
     */
    public TablutBoardState getBoardState() {
    	return boardState;
    }
    
    /**
     * Accessor method to get the latest move
     * @return latest move
     */    
    public TablutMove getLastMove() {
    	return lastMove;
    }
    
    /**
     * Accessor method to get the score of the node
     * @return score
     */
    public int getScore() {
    	return score;
    }
    
    /**
     * Modification method to set the score of the node
     * @param score
     */
    public void setScore(int score) {
    	this.score = score;
    }
    
    /*
    // Returns a random child node to explore.
    public Node getRandomChildNode() {
    	Random rand = new Random();
    	int random = rand.nextInt(children.size());
    	return children.get(random);
    }
    */
	
}