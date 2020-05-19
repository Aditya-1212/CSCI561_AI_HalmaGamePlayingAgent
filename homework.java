
import java.beans.Visibility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.math.*;

import javax.jws.Oneway;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

class ReadInput {
	public static int play_board[][] = new int[16][16];
	static String gameType;
	static String myColour;
	static String opponentColour;
	static float time;

	public ReadInput() {
	}

	void readInput() {
		String fileName = "input.txt";
		try (BufferedReader fileBufferReader = new BufferedReader(new FileReader(fileName))) {
			gameType = fileBufferReader.readLine();
			myColour = fileBufferReader.readLine();
			if(myColour.equals("WHITE"))
				opponentColour = "BLACK";
			else if(myColour.equals("BLACK"))
				opponentColour = "WHITE";
			time = Float.parseFloat(fileBufferReader.readLine());
			int i = 0;
			while (i < 16) {
				String row = fileBufferReader.readLine();
				char[] ch = row.toCharArray();
				for (int j = 0; j < 16; j++) {
					if (ch[j] == '.')
						play_board[i][j] = 0;
					if (ch[j] == 'W')
						play_board[i][j] = 1;
					if (ch[j] == 'B')
						play_board[i][j] = 2;
				}
				i++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	int[][] getBoard() {
		return play_board;

	}

	void writeOutput(String s) {
		try {
			File file = new File("output.txt");
			FileWriter fos = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fos);
			bw.write(s);
			bw.flush();
			bw.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
class Node {
	int r;
	int c;
	Node parent;
	boolean insideCamp;

	Node(int r, int c, boolean insideCamp) {
		this.r = r;
		this.c = c;
		this.insideCamp = insideCamp;
	}

	Node(int r, int c, Node parent) {
		this.r = r;
		this.c = c;
		this.parent = parent;
	}

	Node direction(int r, int c, int i, Node current) {
		if (i == 0)
			return new Node(r - 1, c - 1, current);
		else if (i == 1)
			return new Node(r - 1, c, current);
		else if (i == 2)
			return new Node(r - 1, c + 1, current);
		else if (i == 3)
			return new Node(r, c + 1, current);
		else if (i == 4)
			return new Node(r + 1, c + 1, current);
		else if (i == 5)
			return new Node(r + 1, c, current);
		else if (i == 6)
			return new Node(r + 1, c - 1, current);
		else if (i == 7)
			return new Node(r, c - 1, current);
		return null;
	}

	Node jump(int r, int c, int i, Node current) {
		if (i == 0)
			return new Node(r - 2, c - 2, current);
		else if (i == 1)
			return new Node(r - 2, c, current);
		else if (i == 2)
			return new Node(r - 2, c + 2, current);
		else if (i == 3)
			return new Node(r, c + 2, current);
		else if (i == 4)
			return new Node(r + 2, c + 2, current);
		else if (i == 5)
			return new Node(r + 2, c, current);
		else if (i == 6)
			return new Node(r + 2, c - 2, current);
		else if (i == 7)
			return new Node(r, c - 2, current);
		return null;
	}

	boolean validJump(Node n) {
		if (n.r >= 0 && n.r < 16 && n.c >= 0 && n.c < 16)
			return true;
		return false;
	}

}

class Move {
	static boolean visited[][] = new boolean[16][16];
	Node oldPoint;
	Node newPoint;
	char moveType;
	double value;
	double alpha = Double.MIN_VALUE;
	double beta = Double.MAX_VALUE;
	static int noOfMoves =0;
	static int noOfPruns = 0;
	static int childs = 0;

	Move bestMove;
	static int iniDepth;
	Move() {

	}
	Move(Move m){
		oldPoint = m.oldPoint;
		newPoint = m.newPoint;
		moveType = m.moveType;
		value = m.value;
	}
	Move(Node x, Node y, double value, char moveType){
		oldPoint = x;
		newPoint = y;
		this.value = value;
		this.moveType = moveType;
	}

	Move(Node x, Node y, char moveType) {
		oldPoint = x;
		newPoint = y;
		this.moveType = moveType;
	}

	boolean gameOver(int board[][], String colour) {
		Board b = new Board();
		int noOfPiecesInGoal = 0;
		for(int r = 0; r < 16; r++) {
			for(int c =0; c < 16; c++) {
				if(colour.equals("WHITE")) {
					if(board[r][c] == 1 && b.isInGoal(r, c, colour))
						noOfPiecesInGoal ++;
				}
				else
				{
					if(board[r][c] == 2 && b.isInGoal(r, c, colour))
						noOfPiecesInGoal ++;
				}
			}
		}
		if(noOfPiecesInGoal == 19)
			return true;
		else
			return false;
	}

	boolean myPiecesInGoal(int[][] board, String colour) {
		Board b = new Board();
		int countPieces = 0;
		if(colour.equals("WHITE")) {
			for(int r = 0;r < 16;r++) {
				for(int c = 0;c < 16;c++) {
					if(board[r][c] == 1 && b.isInGoal(r, c, colour))
						countPieces++;
				}
			}
		}
		else {
			for(int r = 0;r < 16;r++) {
				for(int c = 0;c < 16;c++) {
					if(board[r][c] == 2 && b.isInGoal(r, c, colour))
						countPieces++;

				}
			}
		}
		if(countPieces >=15)
			return true;
		else
			return false;

	}


	double eval2(int[][] board, boolean isMaxPlayer, String colour) {
		Board b = new Board();
		List<Node> myPieces = new ArrayList<Node>();
		List<Node> insideEmptySpaces = new ArrayList<Node>();
		double utilityValue = 0.0;

		for(int r = 0; r < 16 ; r++) {
			for(int c = 0; c< 16; c++) {
				if(ReadInput.myColour.equals("WHITE")) {
					if(board[r][c] == 1)
						myPieces.add(new Node(r,c,b.isInsideCamp(r, c, ReadInput.myColour)));
					else if(board[r][c] == 0 && b.isInGoal(r, c, ReadInput.myColour)) {
						insideEmptySpaces.add(new Node(r,c,b.isInsideCamp(r, c, ReadInput.myColour)));
					}
				}
				else {
					if(board[r][c] == 2)
						myPieces.add(new Node(r,c,b.isInsideCamp(r,c,ReadInput.myColour)));
					else if(board[r][c] == 0 && b.isInGoal(r, c, ReadInput.myColour)){
						insideEmptySpaces.add(new Node(r,c,b.isInsideCamp(r, c, ReadInput.myColour)));
					}
				}

			}
		}
		double maxDist = -10000;
		for(int i = 0; i < myPieces.size();i++) {
			for(int j = 0; j < insideEmptySpaces.size(); j++) {
				utilityValue = utilityValue + Math.sqrt(Math.pow(myPieces.get(i).r - insideEmptySpaces.get(j).r , 2) + Math.pow(myPieces.get(i).c - insideEmptySpaces.get(j).c, 2));
//				if(utilityValue > maxDist)
//					maxDist = utilityValue;
			}
		}
		//System.out.println("MaxDist: " + maxDist);
		//System.out.println("Inside empty : " + insideEmptySpaces.size());
		//System.out.println(insideEmptySpaces.get(0).r + "," + insideEmptySpaces.get(0).c);
		if(gameOver(board, colour))
			return 5000;
		return (-1.0 * utilityValue);
	}

	List<Move> makeMove(int[][] board, Node start, Node current, List<Move> moves, boolean flag, String colour) {
		Board b = new Board();
		for (int i = 0; i < 8; i++) {
			Node neigh = current.direction(current.r, current.c, i, current);
			neigh.insideCamp = b.isInsideCamp(neigh.r, neigh.c, colour);
			Node jumped = current.jump(current.r, current.c, i, current);
			jumped.insideCamp = b.isInsideCamp(jumped.r, jumped.c, colour);
			if (current.validJump(neigh)) {
				if(b.isInGoal(current.r, current.c, colour)) {
					if(board[neigh.r][neigh.c] == 0)
					{
						if(b.isInGoal(neigh.r, neigh.c, colour)) {
							if(flag!= true)
								moves.add(new Move(current,neigh,'E'));
						}
					}
					else if(current.validJump(jumped)) {
						if(board[jumped.r][jumped.c] == 0 && !visited[jumped.r][jumped.c]) {
							visited[current.r][current.c] = true;
							if(b.isInGoal(jumped.r, jumped.c, colour))
								moves.add(new Move(start, jumped, 'J'));
							moves = makeMove(board,start,jumped, moves, true, colour);
						}
					}
				}
				else if (board[neigh.r][neigh.c] == 0) {
					if (!b.isInsideCamp(neigh.r, neigh.c, colour)) {
						if (flag != true)
							moves.add(new Move(current, neigh, 'E'));
					}

				} else if (current.validJump(jumped)) {
					if (board[jumped.r][jumped.c] == 0 && !visited[jumped.r][jumped.c]) {
						visited[current.r][current.c] = true;
						if(!b.isInsideCamp(jumped.r, jumped.c, colour))
							moves.add(new Move(start, jumped, 'J'));
						moves = makeMove(board,start,jumped, moves, true, colour);
					}
				}
			}
		}
		return moves;
	}

	List<Move> furtherAwayMoves(int[][] board, Node start, Node current, List<Move> moves, boolean flag, String colour){
		Board b = new Board();
		for(int i = 0; i < 8 ; i++) {
			Node neigh = current.direction(current.r, current.c, i, current);
			neigh.insideCamp = b.isInsideCamp(neigh.r, neigh.c, colour);
			Node jumped = current.jump(current.r, current.c, i, current);
			jumped.insideCamp = b.isInsideCamp(jumped.r, jumped.c, colour);
			if (current.validJump(neigh)) {
				if (board[neigh.r][neigh.c] == 0) {
					if (colour.equals("WHITE")) {
						if(neigh.r <= current.r && neigh.c <= current.c) {
							if (flag != true)
								moves.add(new Move(current, neigh, 'E'));
						}
					}
					else if(colour.equals("BLACK")) {
						if(neigh.r >= current.r && neigh.c >= current.c) {
							if (flag != true)
								moves.add(new Move(current, neigh, 'E'));
						}
					}
				} else if (current.validJump(jumped)) {
					if (board[jumped.r][jumped.c] == 0 && !visited[jumped.r][jumped.c]){
						if(colour.equals("WHITE")) {
							if(jumped.r <= current.r && jumped.c <= current.c) {
								visited[current.r][current.c] = true;
								moves.add(new Move(start, jumped, 'J'));
								moves = furtherAwayMoves(board,start,jumped, moves, true, colour);
							}
						}
						else if(colour.equals("BLACK")) {
							if(jumped.r >= current.r && jumped.c >= current.c) {
								visited[current.r][current.c] = true;
								moves.add(new Move(current, jumped, 'J'));
								moves = furtherAwayMoves(board, start,jumped, moves, true, colour);
							}
						}
					}
				}
			}
		}
		return moves;
	}

	int[][] copyBoard(int board[][]) {
		int copy[][] = new int[16][16];
		for (int r = 0; r < 16; r++) {
			for (int c = 0; c < 16; c++) {
				copy[r][c] = board[r][c];
			}
			System.out.println();
		}
		return copy;
	}

	void printBoard(int board[][]) {
		for (int r = 0; r < 16; r++) {
			for (int c = 0; c < 16; c++) {
				System.out.print(board[r][c]);
			}
			System.out.println();
		}
		System.out.println();
	}

	void moveUpdate(int board[][], Move m) {
		board[m.newPoint.r][m.newPoint.c] = board[m.oldPoint.r][m.oldPoint.c];
		board[m.oldPoint.r][m.oldPoint.c] = 0;
	}

	void undoMove(int board[][], Move m) {
		board[m.oldPoint.r][m.oldPoint.c] = board[m.newPoint.r][m.newPoint.c];
		board[m.newPoint.r][m.newPoint.c] = 0;
	}

	double evaluateBoard(int board[][], boolean isMaxPlayer, String colour) {
		double utilityValue = 0.0;
		double referenceValue = 10000;
		int topCornerR = 0, topCornerC = 0;
		int bottomCornerR = 15, bottomCornerC = 15;

//		double white_distance = 0.0;
//		double black_distance = 0.0;
//
//		for(int r = 0; r < 16; r++) {
//			for(int c = 0; c < 16; c++) {
//				if(board[r][c] == 1) {
//					white_distance += Math.sqrt(Math.pow(r - topCornerR, 2) + Math.pow(c - topCornerC, 2));
//				}
//				else if(board[r][c] == 2) {
//					black_distance += Math.sqrt(Math.pow(r - bottomCornerR, 2) + Math.pow(c - bottomCornerC, 2));
//				}
//			}
//		}
//		if (isMaxPlayer && colour.equals("WHITE")) {
//			return black_distance - white_distance;
//		}
//		else if (isMaxPlayer && colour.equals("BLACK")){
//			return white_distance - black_distance;
//		}
//		else if(!isMaxPlayer && colour.equals("WHITE")) {
//			return white_distance - black_distance;
//		}
//		else if(!isMaxPlayer && colour.equals("BLACK")) {
//			return black_distance - white_distance;
//		}
//		return 0;
//
//
		//System.out.println(colour);
		for (int r = 0; r < 16; r++) {
			for (int c = 0; c < 16; c++) {
				if(ReadInput.myColour.equals("WHITE")) {
					if (board[r][c] == 1) {
						utilityValue = utilityValue + Math.sqrt(Math.pow(r - topCornerR, 2) + Math.pow(c - topCornerC, 2));
					}
				}
				else if(ReadInput.myColour.equals("BLACK")) {
					if (board[r][c] == 2) {
						utilityValue = utilityValue + Math.sqrt(Math.pow(r - bottomCornerR, 2) + Math.pow(c - bottomCornerC, 2));
					}
				}

			}
		}
	//	System.out.println((-1.0 * utilityValue));
		if(gameOver(board, colour))
			return 5000;
		//System.out.println(referenceValue-utilityValue);
		return  (-1.0 * utilityValue);

	}



	double alphaBeta(int depth, int board[][],double alpha,double beta, boolean isMaxPlayer, String colour) {
		if(depth == 0 || gameOver(board, colour)) {
			if(this.myPiecesInGoal(board, colour))
				return eval2(board, isMaxPlayer, colour);
			else
				return evaluateBoard(board, isMaxPlayer, colour);
		}
		if(isMaxPlayer) {
			double v = -10000.0;
			double val = -10000.0;
			List<Move> newGameMoves = getMoves(board, colour);
			for(int i = 0; i < newGameMoves.size(); i++) {
				Move newGameMove = newGameMoves.get(i);
				moveUpdate(board, newGameMove);
				 val = alphaBeta(depth-1, board, alpha, beta, false, ReadInput.opponentColour);
				undoMove(board, newGameMove);
				if( depth == Move.iniDepth && v < val) {
					v =val;
					bestMove = newGameMove;
				}
				if(v < val) {
					v = val;
				}
				if(v >= beta) {
					return v;
				}
				alpha = Math.max(alpha, v);
			}
			return v;
		}
		else {
			double v = 10000;
			double val = 0.0;
			List<Move> newGameMoves = getMoves(board, colour);
			for(int i = 0; i < newGameMoves.size(); i++) {
				Move newGameMove = newGameMoves.get(i);
				moveUpdate(board, newGameMove);
				 val = alphaBeta(depth-1, board, alpha, beta, true, ReadInput.myColour);
				undoMove(board, newGameMove);
				if(val < v) {
					v = val;
				}
				if(v <= alpha) {
					noOfPruns++;
					return v;
				}
				beta = Math.min(beta, v);
			}
			return v;
		}
	}

	List<Move> getMoves(int board[][], String colour) {
		Board b = new Board();
		Move m = new Move();
		List<Node> pieces = b.makeBoard(board, colour);
		List<Node> piecesInsideCamp = new ArrayList<Node>();
		List<Node> piecesOutsideCamp = new ArrayList<Node>();
		List<Move> initialMoves = new ArrayList<Move>();
		List<Move> furtherAwayMoves = new ArrayList<Move>();
		for (int i = 0; i < pieces.size(); i++) {
			if (pieces.get(i).insideCamp)
				piecesInsideCamp.add(pieces.get(i));
			else
				piecesOutsideCamp.add(pieces.get(i));
		}
		List<Node> piecesInsideCopy = new ArrayList<Node>();
		piecesInsideCopy.addAll(piecesInsideCamp);
		int f = 0;
		if (piecesInsideCamp.size() != 0) {
			while (!piecesInsideCamp.isEmpty()) {
				for (int r = 0; r < 16; r++)
					for (int c = 0; c < 16; c++)
						Move.visited[r][c] = false;
				Node current = piecesInsideCamp.remove(0);
				boolean flag = false;
				initialMoves = m.makeMove(board, current, current, initialMoves, flag, colour);
			}
		}
		if(initialMoves.size() == 0 && !piecesInsideCopy.isEmpty()) {
			f = 2;
			while(!piecesInsideCopy.isEmpty()) {
				for (int r = 0; r < 16; r++)
					for (int c = 0; c < 16; c++)
						Move.visited[r][c] = false;
				Node current = piecesInsideCopy.remove(0);
				boolean flag = false;
				furtherAwayMoves = m.furtherAwayMoves(board,current, current, furtherAwayMoves, flag, colour);
			}
		}
		if (initialMoves.size() >= 1)
			f = 1;
		if (f == 1)
			return initialMoves;
		else if(f == 2 && !furtherAwayMoves.isEmpty())
			return furtherAwayMoves;
		else {
			while (!piecesOutsideCamp.isEmpty()) {
				for (int r = 0; r < 16; r++)
					for (int c = 0; c < 16; c++)
						Move.visited[r][c] = false;
				Node current = piecesOutsideCamp.remove(0);
				boolean flag = false;
				initialMoves = m.makeMove(board, current, current, initialMoves, flag, colour);
				}
			return initialMoves;
		}
	}

}

class SortbyMoveMax implements Comparator<Move>
{
    public int compare(Move a, Move b)
    {
    	if (a.value < b.value)
			return 1;
		else if (a.value > b.value)
			return -1;
		return 0;
    }
}
class SortbyMoveMin implements Comparator<Move>
{
    public int compare(Move a, Move b)
    {
    	if (a.value > b.value)
			return 1;
		else if (a.value < b.value)
			return -1;
		return 0;
    }
}

class Board {
	boolean isInsideCamp(int r, int c, String colour) {
		if (colour.equals("WHITE")) {
			if (r > 10 && r < 16 && c > 10 && c < 16) {
				if (r == 11 && c == 11)
					return false;
				else if (r == 11 && c == 12)
					return false;
				else if (r == 11 && c == 13)
					return false;
				else if (r == 12 && c == 11)
					return false;
				else if (r == 12 && c == 12)
					return false;
				else if (r == 13 && c == 11)
					return false;
				else
					return true;
			}
		} else {
			if (r < 5 && r >= 0 && c < 5 && c >= 0) {
				if (r == 4 && c == 4)
					return false;
				else if (r == 4 && c == 3)
					return false;
				else if (r == 4 && c == 2)
					return false;
				else if (r == 3 && c == 3)
					return false;
				else if (r == 3 && c == 4)
					return false;
				else if (r == 2 && c == 4)
					return false;
				else
					return true;
			}
		}
		return false;
	}

	boolean isInGoal(int r, int c, String colour) {
		if(colour.contentEquals("WHITE")) {
			if (r < 5 && r >= 0 && c < 5 && c >= 0) {
				if (r == 4 && c == 4)
					return false;
				else if (r == 4 && c == 3)
					return false;
				else if (r == 4 && c == 2)
					return false;
				else if (r == 3 && c == 3)
					return false;
				else if (r == 3 && c == 4)
					return false;
				else if (r == 2 && c == 4)
					return false;
				else
					return true;
			}
		}
		else {
			if (r > 10 && r < 16 && c > 10 && c < 16) {
				if (r == 11 && c == 11)
					return false;
				else if (r == 11 && c == 12)
					return false;
				else if (r == 11 && c == 13)
					return false;
				else if (r == 12 && c == 11)
					return false;
				else if (r == 12 && c == 12)
					return false;
				else if (r == 13 && c == 11)
					return false;
				else
					return true;
			}
		}
		return false;
	}

	List<Node> makeBoard(int[][] board, String colour) {
		List<Node> pieces = new ArrayList<Node>();
		for (int r = 0; r < 16; r++) {
			for (int c = 0; c < 16; c++) {
				if (colour.equals("WHITE")) {
					if (board[r][c] == 1)
						pieces.add(new Node(r, c, isInsideCamp(r, c, colour)));
				} else if (colour.equals("BLACK")) {
					if (board[r][c] == 2)
						pieces.add(new Node(r, c, isInsideCamp(r, c, colour)));
				}
			}
		}
		return pieces;
	}
}

public class homework {

	public static void main(String[] args) {
		ReadInput read = new ReadInput();
		read.readInput();
		Move m = new Move();
		Board b = new Board();
		double alpha = -100000;
		double beta = 100000;
		int board[][] = ReadInput.play_board;
		if(ReadInput.gameType.equals("SINGLE"))
			Move.iniDepth = 1;
		else if(ReadInput.gameType.equals("GAME"))
			Move.iniDepth = 3;
		if(m.gameOver(board, ReadInput.myColour))
		{
			return;
		}
		else {
			double bestVal = m.alphaBeta(Move.iniDepth , board, alpha, beta, true,  ReadInput.myColour);
			m.moveUpdate(board, m.bestMove);
			Node current = m.bestMove.newPoint;
			String s = "";
			while(current.parent!=null) {
				s = m.bestMove.moveType +" " + current.parent.c + "," + current.parent.r+ " " + current.c + "," + current.r + System.lineSeparator() + s;
				current = current.parent;
			}
			System.out.println(s);
			read.writeOutput(s.substring(0,s.length()-1));
		}
	}
}
