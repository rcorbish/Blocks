import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Blocks {

	public static void main( String args[] ) {
		try {
            BlockSolver sol = new BlockSolver() ;
            // Initialize the game - row by row 
            // -1 is an empty square
            // 0 is the block to move to the target
            // Other numbers represent rectangular blocks
            // A block must be rectangular - this is NOT checked
            Board start = new Board( new int[][]{ 
                {  1,  1, -1,  2, -1 },
                {  0, -1,  5,  2,  3 },
                { -1, -1,  5,  6,  3 },  // << target here - get block 0 here
                {  4,  4,  5,  6, -1 },
                { -1, -1,  7,  7,  7 }
            }) ;

            // Solve given starting board and target ( move block 0 to [4,2] )
            Collection<Board> sequence = sol.solve( start, 2, 4 ) ;

            if( sequence != null ) {
                System.out.println( String.format( "Solved in %d moves", sequence.size() ) ) ;
    
                // Then pretty print out each successive move
                for( Board b : sequence ) {
                    System.out.println( b ) ;
                }
            } else {
                System.out.println( "No solution found" ) ;
            }
    
		} catch( Throwable t ) {
			t.printStackTrace() ;
			System.exit( -2 ) ;
		}
    }    
}

class BlockSolver {
    //
    // This uses BFS to scan the target space to find the first solution
    // i.e. the shortest number of moves from start to target. Because there can be
    // many targets (we only check piee 0) this may not provide the optimum solution
    //
    public Collection<Board> solve( Board start, int xTarget, int yTarget ) {
        // Remember what moves we have completed, and from where 
        // we got to that state.
        final Map<Board,Board> doneMoves = new ConcurrentHashMap<>() ;
        // The q for the BFS
        Deque<Board> q = new LinkedList<>() ;

        // Start by pushing our initial problem into BFS space
        q.addLast( start ) ;
        // And for now assume we got to start from itself
        doneMoves.put( start, start ) ;

        // Main BFS body - keep examining solutions
        while( !q.isEmpty() ) {
            Board a = q.removeFirst() ;

            // Is this a solution? If so print out how we got here
            // and finish
            if( a.blockAt( xTarget, yTarget ) == Board.TARGET_BLOCK ) { 
                // Use a double ended queue to help us reverse the order
                // since we work backwards from target
                Deque<Board> sequence = new LinkedList<>() ;

                // Trace our route through problem space
                Board from = a ;
                do { 
                    sequence.addFirst( from ) ;
                    from = doneMoves.get( from ) ;
                } while( !from.equals(start) ) ;
                // Add the inital starting on there 
                sequence.addFirst( start ) ;
                return sequence ;  // first solution
            }
            // Determine all valid moves from the current position
            // If we have NOT been to that state push onto queue for checking
            List<Board> nextValidMoves = a.nextMoves() ;
            for( Board b : nextValidMoves ) {
                if( !doneMoves.containsKey(b) ) {
                    doneMoves.put( b, a ) ;
                    q.addLast( b ) ;
                }
            }
        }
        return null ;       // see also return above
    }
}

// The board class represents a state of the board. It should be kep small, since
// there may be a few of these on the BFS queue at any time. This could be optimized
// but for now it's not too bad. If we had 1000x1000 grids that might change
class Board {
    public final static int EMPTY = -1 ;
    public final static int TARGET_BLOCK = 0 ;
    
    public final int X  ;   // size of grid
    public final int Y  ;   // size of grid

    public final int board[] ;   // use 1D array so we can use Arrays helper methods

    private int numBlocks = 0 ;     // how may blocks (including zero do we have on board )

    // Use this to keep move easily from one state to another
    public Board( final Board copy ) {
        this.X = copy.X ;
        this.Y = copy.Y ;
        board =  new int[X*Y] ;
        System.arraycopy( copy.board, 0, board, 0, board.length ) ;
        numBlocks = copy.numBlocks ;
    }

    // Our useful helper method to initialize the board
    public Board( int [][] start ) {
        X = start[0].length ;   // not checked assume at least 1 item
        Y = start.length ;
        board =  new int[X*Y] ;
        int ix = 0 ;
        numBlocks = 0 ;
        for( int y=0 ; y<Y ; y++ ) {
            for( int x=0 ; x<X ; x++ ) {
                board[ix] = start[x][y] ;
                // Biggest block index = numBlocks ( need to +1 )
                if( board[ix] > numBlocks ) {
                    numBlocks = board[ix] + 1 ;
                 }
                ix++ ;
            }
        }
    }

    // Helper for printing a board to text
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        for( int y=0 ; y<Y ; y++ ) {
            for( int x=0; x<X ; x++ ) {
                int n = blockAt(x, y) ;
                if( n<0 ) {
                    sb.append( "_ " ) ;
                } else {
                    sb.append( n ).append( " " ) ;
                }
            }
            sb.append( System.lineSeparator() ) ;
        }
        return sb.toString() ;
    }

    // Look at all valid moves. It is assumed a piece
    // may move in any direction (one step) provided 
    // the squares it moves into are empty
    public List<Board> nextMoves() {
        // We'll return all valid moves here
        List<Board> validMoves = new ArrayList<>() ;

        // All blocks are considered
        for( int b=0 ; b<numBlocks ; b++ ) {
            if( canMoveRight(b) ) {
                Board newBoard = new Board( this ) ;
                newBoard.moveRight(b);
                validMoves.add( newBoard ) ;
            }
            if( canMoveUp(b) ) {
                Board newBoard = new Board( this ) ;
                newBoard.moveUp(b);
                validMoves.add( newBoard ) ;
            }
            if( canMoveDown(b) ) {
                Board newBoard = new Board( this ) ;
                newBoard.moveDown(b);
                validMoves.add( newBoard ) ;
            }
            if( canMoveLeft(b) ) {  // can block b move left ?
                // it can so copy board & shift the block one step left
                Board newBoard = new Board( this ) ;
                newBoard.moveLeft(b);
                validMoves.add( newBoard ) ;
            }
        }

        return validMoves ;
    }

    // These next methods check to see if there's space to the left of a piece
    // and it's not on the LHS edge. No checking of right, top & below is needed
    // 
    // The other canMovexxx work similarly
    public boolean canMoveLeft( int block ) { 
        search:
            for( int x=0 ; x<X ; x++ ) {
                for( int y=0 ; y<Y ; y++ ) {
                    if( blockAt(x, y)==block ) {
                        while( y<Y && blockAt(x, y)==block ) {
                            if( x == 0 || blockAt(x-1, y) != EMPTY ) { 
                                return false ;
                            }
                            y++ ;
                        }
                        break search ;
                    }
                }
            }
        return true ;
    }

    // This executes the move left. No checking is done - it assumes
    // canMoveLeft() returned true.
    public void moveLeft( int block ) { 
        for( int x=0 ; x<X-1 ; x++ ) {
            for( int y=0 ; y<Y ; y++ ) {
                if( x<X-1 && blockAt(x+1, y) == block ) {
                    setBlockAt(x, y, block ) ;
                } else if( blockAt(x, y) == block ) {
                    setBlockAt(x, y, EMPTY ) ;
                }
            }
        }
    }

    // Same comments as for left - just search from RHS instead of LHS
    public boolean canMoveRight( int block ) { 
        search:
            for( int x=X-1 ; x>=0 ; x-- ) {
                for( int y=0 ; y<Y ; y++ ) {
                    if( blockAt(x, y)==block ) {
                    while( y<Y && blockAt(x, y)==block ) {
                        if( x==X-1 || blockAt(x+1, y) != EMPTY ) { 
                            return false ;
                        }
                        y++ ;
                    }
                    break search ;
                }
            }
        }
        return true ;
    }

    public void moveRight( int block ) { 
        for( int x=X-1 ; x>=0 ; x-- ) {
            for( int y=0 ; y<Y ; y++ ) {
                if( x>0 && blockAt(x-1, y) == block ) {
                    setBlockAt(x, y, block ) ;
                } else if( blockAt(x, y) == block ) {
                    setBlockAt(x, y, EMPTY ) ;
                }
            }
        }
    }

    public boolean canMoveDown( int block ) { 
        search: 
            for( int y=Y-1 ; y>=0 ; y-- ) {
                for( int x=0 ; x<X ; x++ ) {
                    if( blockAt(x, y)==block ) {
                        while( x<X && blockAt(x, y)==block ) {
                            if( y == Y-1 || blockAt(x, y+1) != EMPTY ) { 
                                return false ;
                            }
                            x++ ;
                        }
                        break search ;
                    }
                }
            }
        return true ;
    }

    public void moveDown( int block ) { 
        for( int x=0 ; x<X ; x++ ) {
            for( int y=Y-1 ; y>=0 ; y-- ) {
                if( y>0 && blockAt(x, y-1) == block ) {
                    setBlockAt(x, y, block ) ;
                } else if( blockAt(x, y) == block ) {
                    setBlockAt(x, y, EMPTY ) ;
                }
            }
        }
    }


    public boolean canMoveUp( int block ) { 
        search: 
            for( int y=0 ; y<Y ; y++ ) {
                for( int x=0 ; x<X ; x++ ) {
                    if( blockAt(x, y)==block ) {
                        while( x<X && blockAt(x, y)==block ) {
                            if( y == 0 || blockAt(x, y-1) != EMPTY ) { 
                                return false ;
                            }
                            x++ ;
                        }
                        break search ;
                    }
                }
            }

        return true ;
    }

    public void moveUp( int block ) { 
        for( int x=0 ; x<X ; x++ ) {
            for( int y=0 ; y<Y ; y++ ) {
                if( y<Y-1 && blockAt(x, y+1) == block ) {
                    setBlockAt(x, y, block ) ;
                } else if( blockAt(x, y) == block ) {
                    setBlockAt(x, y, EMPTY ) ;
                }
            }
        }
    }

    // COnvenience to access a 1D array using X,Y coords
    public int blockAt( int x, int y ) {
        return board[ x * Y + y ] ;
    }
    // Convenience to write to 1D array using [X,Y] coords
    public void setBlockAt( int x, int y, int value ) {
        board[ x * Y + y ] = value ;
    }

    // Since we put a board into a HashMap - we better do these 2

    @Override
    public int hashCode() {
        return Arrays.hashCode( board ) ;   // Nice hash :)
    }

    @Override
    public boolean equals( Object other ) {
        if( !(other instanceof Board) ) return false ;
        Board o = (Board)other ;
        if( o.X != X || o.Y != Y ) return false ;

        return Arrays.equals( board, o.board ) ;
    }
}
