package co.torri.scalamines;


import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JFrame;

public class MinesGameGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Integer BOARD_SIZE = 10;
	private final MinesGame game;
    private JButton[][] buttons;

    public MinesGameGUI(MinesGame game) {
    	this.game = game;
        buttons = new JButton[BOARD_SIZE][BOARD_SIZE];
        GridLayout layout = new GridLayout(BOARD_SIZE,BOARD_SIZE);
        setLayout(layout);
        setSize(400, 400);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                JButton newButton = new JButton();
                newButton.addKeyListener(new SquareKeyListener(i, j, game.board().getSquare(i,j), newButton));
                buttons[i][j] = newButton;
                add(newButton);
            }
        }
        
        setVisible(true);
    }
    
    private void updateAll() {
    	for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
            	((SquareKeyListener) buttons[i][j].getKeyListeners()[0]).updateText();
            	buttons[i][j].setVisible(false);
            	buttons[i][j].setVisible(true);
            }
    	}
    }

    private class SquareKeyListener implements KeyListener {

    	private final Integer i,j;
    	private final Square square;
    	private final JButton button;
    	
    	public SquareKeyListener(Integer i, Integer j, Square square, JButton button) {
    		this.i = i;
    		this.j = j;
    		this.square = square;
    		this.button = button;
    		updateText();
    	}

		public void updateText() {
			this.button.setText(this.square.toString());
			if (game.won()) {
				System.out.println("YOU WIN!");
			}
		}

		@Override
		public void keyPressed(KeyEvent ke) {
			JButton b = button;
			if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
				if (j != 0) {
					b = buttons[i][j-1];//.requestFocus();
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (j != (BOARD_SIZE - 1)) {
					b = buttons[i][j+1];//.requestFocus();
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_UP) {
				if (i != 0) {
					b = buttons[i-1][j];//.requestFocus();
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
				if (i != (BOARD_SIZE - 1)) {
					b = buttons[i+1][j];//.requestFocus();
				}
			} else if (ke.getKeyCode() == KeyEvent.VK_F) {
				square.flag_$eq(!square.flag());
				updateText();
			} else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
				try {
					square.reveal();
				} catch (Exception e) {
					e.printStackTrace();
				}
				updateAll();
			} else if (ke.getKeyCode() == KeyEvent.VK_S) {
				square.revealNeighbours();
				updateAll();
				/*for (JButton[] buttonLine : buttons) {
					for (JButton b : buttonLine) {
						((SquareKeyListener) b.getKeyListeners()[0]).updateText();
						b.setVisible(false);
						b.setVisible(true);
					}
				}
				button.requestFocus();*/
			}
			//updateText();
			b.requestFocus();
		}

		@Override
		public void keyReleased(KeyEvent ke) {
			
		}

		@Override
		public void keyTyped(KeyEvent ke) {
			
		}
    	

    }
    
    public static void main(String[] args) {
    	MinesGame game = new MinesGame(BOARD_SIZE);
    	new MinesGameGUI(game);
    }

}
