/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package teamld1;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 *
 * @author Epitaph64
 */
public class Main extends BasicGame implements Game {

    private Image boatImage;

    private gameState state = gameState.Game;

    private Input input;

    private Boat boat;


    Main() {
        super("team ld 1");
    }

    public static void main(String[] arguments) {
        try {
            AppGameContainer app = new AppGameContainer(new Main());
            app.setDisplayMode(800, 600, false);
            app.setTargetFrameRate(60);
            app.setShowFPS(false);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(GameContainer container) throws SlickException
    {
        input = container.getInput();
        boatImage = new Image("res/boat.png");
        boat = new Boat(200, 200);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException
    {
        if (input.isKeyPressed(Input.KEY_ESCAPE))
        {
            container.exit();
        }
    }

    public void render(GameContainer container, Graphics g) throws SlickException
    {
        if (state == gameState.Map)
        {
            
        }
        if (state == gameState.Game)
        {
            boatImage.draw(200, 200);
        }
    }
}
