package jbomber;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;

public class Main extends BasicGame {

    private int gameState = 0;

    /* Game State List for Reference
     *  0 - Menu
     *  1 - In Game
    */

    private int playerType[] = {1,1,2,2};

    //Menu Resources
    private Image bgSmall;
    private Image bgBig;
    private int bgX = 0;
    private int bgY = 0;
    private Image playButton;
    private Image optionsButton;
    private Image quitButton;
    private Image backButton;
    private Image title;
    private Image cpuCap, humCap, offCap;
    private int menuMouseX, menuMouseY;
    private Rectangle play, options, quit, back;
    private Rectangle p1, p2, p3, p4;

    //Effects Resources
    private Image fog;
    private float fogX = 0;
    
    //These affect the whole screen offsets (for shaking)
    private int jitterX = 16;
    private int jitterY = 0;
    private boolean shake;
    private int shakeMagnitude;
    private boolean shakeRight = true;

    //Audio Resources
    private Sound explosion, bombup, fireup;
    private Music bombsong;

    private Player whiteBomber, blackBomber, redBomber, blueBomber;

    private static MersenneTwisterFast mt = new MersenneTwisterFast();

    private int[][] board = new int[19][15];
    private int[][] players = new int[19][15];
    private Bomb[][] bombs = new Bomb[19][15];
    private Fire[][] fire = new Fire[19][15];

    private SpriteSheet tileset;
    private SpriteSheet bombImage;
    private SpriteSheet deathAnim;
    
    private Input input;
    private boolean changingOptions = false;

    public Main()
    {
        super("jBomber");
    }
    
    public static void main(String[] arguments)
    {
        try
        {
            AppGameContainer app = new AppGameContainer(new Main());
            app.setDisplayMode(640, 480, false);
            app.setShowFPS(false);
            app.setTargetFrameRate(75);
            app.setVSync(false);
            app.setFullscreen(false);
            app.start();
        }
        catch (SlickException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void init(GameContainer container) throws SlickException
    {
        //Menu Graphics Loading
        title = new Image("data/menu/title.png");
        playButton = new Image("data/menu/button_play.png");
        optionsButton = new Image("data/menu/button_options.png");
        quitButton = new Image("data/menu/button_quit.png");
        backButton = new Image("data/menu/button_back.png");
        bgSmall = new Image("data/menu/background_small.png");
        bgBig = new Image("data/menu/background_big.png");
        cpuCap = new Image("data/menu/cpu_caption.png");
        humCap = new Image("data/menu/human_caption.png");
        offCap = new Image("data/menu/off_caption.png");
        //Animations Loading
        bombImage = new SpriteSheet("data/bomb.png", 32, 32);
        deathAnim = new SpriteSheet("data/death_animation.png", 32, 32);
        //TileSet Loading
        tileset = new SpriteSheet("data/tileset.png", 32, 32);
        fog = new Image("data/fog.png");
        //Sound and Music Loading
        explosion = new Sound("data/explosion.wav");
        bombsong = new Music("data/bombsong.ogg");
        bombup = new Sound("data/bombup.wav");
        fireup = new Sound("data/fireup.wav");
        play = new Rectangle(200, 140, playButton.getWidth(), playButton.getHeight());
        options = new Rectangle(200, 240, optionsButton.getWidth(), optionsButton.getHeight());
        quit = new Rectangle(200, 340, quitButton.getWidth(), quitButton.getHeight());
        back = new Rectangle(200, 340, backButton.getWidth(), backButton.getHeight());
        p1 = new Rectangle(130, 220, 100, 50);
        p2 = new Rectangle(230, 220, 100, 50);
        p3 = new Rectangle(330, 220, 100, 50);
        p4 = new Rectangle(430, 220, 100, 50);
        //Setup Input
        bombsong.loop();
        input = container.getInput();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException
    {
        if (gameState == 0)
        {
            checkInputMenu(container);
        }
        if (gameState == 1)
        {
            checkInputGame(container);
            //Check on all the bombs
            checkBombs();
            //Check to see if anyone has been killed
            checkFire();
            //Check input or AI for each player
            checkPlayer(whiteBomber);
            checkPlayer(blackBomber);
            checkPlayer(redBomber);
            checkPlayer(blueBomber);
            //Shift any players currently in transition between tiles
            shiftPlayer(whiteBomber);
            shiftPlayer(blackBomber);
            shiftPlayer(redBomber);
            shiftPlayer(blueBomber);
            //Update the screen shake effect if necessary
            checkShake();
            //Update the fog effect
            fogX += -0.3f;
            if (fogX < -640)
            {
                fogX = 0;
            }
        }
    }

    public void render(GameContainer container, Graphics g) throws SlickException
    {
        if (gameState == 0)
        {
            drawMenuBackground(g);
            drawMenuButtons(g);
            g.drawImage(title, 50, 0);
        }
        if (gameState == 1)
        {
            drawTiles(g);
            drawFire(g);
            drawPlayer(g, whiteBomber);
            drawPlayer(g, blackBomber);
            drawPlayer(g, redBomber);
            drawPlayer(g, blueBomber);
            g.drawImage(fog, fogX + jitterX, 0);
            g.drawImage(fog, fogX + 640 + jitterX, 0);
        }
    }

    private void changeMusic(int songNumber) throws SlickException
    {
        bombsong.stop();
        if (songNumber == 1)
        {
            bombsong = new Music("data/bombsong.ogg");
        }
        if (songNumber == 2)
        {
            bombsong = new Music("data/cavesong.ogg");
        }
    }

    private void checkInputMenu(GameContainer container) throws SlickException
    {
        if (input.isKeyPressed(Input.KEY_ESCAPE))
        {
            container.exit();
        }
        if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON))
        {
            menuMouseX = input.getMouseX();
            menuMouseY = input.getMouseY();
            Rectangle mouseClicker = new Rectangle(menuMouseX, menuMouseY, 1,1);
            if (changingOptions)
            {
                if (mouseClicker.intersects(back))
                {
                    int playersOn = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        if (playerType[i] != 0)
                        {
                            playersOn += 1;
                        }
                    }
                    if (playersOn >= 2)
                    {
                        changingOptions = false;
                    }
                }
                if (mouseClicker.intersects(p1))
                {
                    playerType[0] += 1;
                    if (playerType[0] == 3)
                    {
                        playerType[0] = 0;
                    }
                }
                if (mouseClicker.intersects(p2))
                {
                    playerType[1] += 1;
                    if (playerType[1] == 3)
                    {
                        playerType[1] = 0;
                    }
                }
                if (mouseClicker.intersects(p3))
                {
                    playerType[2] += 1;
                    if (playerType[2] == 3)
                    {
                        playerType[2] = 0;
                    }
                }
                if (mouseClicker.intersects(p4))
                {
                    playerType[3] += 1;
                    if (playerType[3] == 3)
                    {
                        playerType[3] = 0;
                    }
                }
            }
            else
            {
                if (mouseClicker.intersects(play))
                {
                    gameState = 1;
                    changeMusic(2);
                    bombsong.loop();
                    newRound(playerType);
                }
                if (mouseClicker.intersects(options))
                {
                    changingOptions = true;
                }
                if (mouseClicker.intersects(quit))
                {
                    container.exit();
                }
            }
        }
    }

    private void checkShake()
    {
        if (shake)
        {
            if (shakeRight)
            {
                jitterX += shakeMagnitude * 2;
                if (jitterX > 16 + shakeMagnitude)
                {
                    shakeRight = false;
                }
            }
            else
            {
                jitterX += -shakeMagnitude * 2;
                if (jitterX < 16 - shakeMagnitude)
                {
                    shakeRight = true;
                }
                else
                {
                    shake = false;
                }
            }
        }
        else
        {
            shakeRight = true;
            jitterX = 16;
            shakeMagnitude = 0;
        }
    }

    private void drawMenuBackground(Graphics g) {
        bgX += 1;
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                g.drawImage(bgSmall, x * 80, y * 60);
            }
        }
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 4; y++)
            {
                if (y % 2 == 0)
                {
                    g.drawImage(bgBig, x * 160 + bgX - 160, y * 120);
                }
                else
                {
                    g.drawImage(bgBig, x * 160 - bgX - 160, y * 120);
                }
            }
        }
        if (bgX > 160)
        {
            bgX = 0;
        }
    }

    private void drawMenuButtons(Graphics g)
    {
        if (changingOptions)
        {
            g.drawImage(backButton, 200, 340);
            tileset.getSprite(1, 1).draw(150, 150, 2.0f);
            tileset.getSprite(1, 1).draw(250, 150, 2.0f);
            tileset.getSprite(1, 1).draw(350, 150, 2.0f);
            tileset.getSprite(1, 1).draw(450, 150, 2.0f);
            tileset.getSprite(0, 1).draw(250, 150, 2.0f, new Color(50, 50, 50));
            tileset.getSprite(0, 1).draw(350, 150, 2.0f, new Color(255, 50, 50));
            tileset.getSprite(0, 1).draw(450, 150, 2.0f, new Color(50, 50, 255));
            for (int humanCheck = 0; humanCheck < 4; humanCheck ++)
            {
                if (playerType[humanCheck] == 1)
                {
                    g.drawImage(humCap, humanCheck * 100 + 130, 200);
                }
                if (playerType[humanCheck] == 2)
                {
                    g.drawImage(cpuCap, humanCheck * 100 + 130, 200);
                }
                if (playerType[humanCheck] == 0)
                {
                    g.drawImage(offCap, humanCheck * 100 + 130, 200);
                }
            }
        }
        else
        {
            g.drawImage(playButton, 200, 150);
            g.drawImage(optionsButton, 200, 240);
            g.drawImage(quitButton, 200, 340);
        }
    }

    private void newRound(int[] playerType)
    {
        board = new int[19][15];
        players = new int[19][15];
        bombs = new Bomb[19][15];
        fire = new Fire[19][15];
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                board[x][y] = 1;
            }
        }
        for (int x = 1; x < 18; x++)
        {
            for (int y = 1; y < 14; y++)
            {
                board[x][y] = 0;
            }
        }
        for (int x = 2; x < 18; x++)
        {
            for (int y = 2; y < 14; y++)
            {
                if (x % 2 == 0 && y % 2 == 0)
                {
                    board[x][y] = 1;
                }
            }
        }
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (board[x][y] != 1 && mt.nextInt(5) > 1)
                {
                    board[x][y] = 2;
                }
            }
        }
        //Clear player areas
        board[1][1] = 0;
        board[1][2] = 0;
        board[2][1] = 0;
        board[16][1] = 0;
        board[17][1] = 0;
        board[17][2] = 0;
        board[16][13] = 0;
        board[17][13] = 0;
        board[17][12] = 0;
        board[1][13] = 0;
        board[1][12] = 0;
        board[2][13] = 0;
        //Place Players
        if (playerType[0] == 0)
        {
            whiteBomber = new Player(1, 1, 1, Color.white, false);
            whiteBomber.setAlive(false);
        }
        if (playerType[0] == 1)
        {
            whiteBomber = new Player(1, 1, 1, Color.white, true);
        }
        if (playerType[0] == 2)
        {
            whiteBomber = new Player(1, 1, 1, Color.white, false);
        }
        if (playerType[1] == 0)
        {
            blackBomber = new Player(17, 1, 2, Color.black, false);
            blackBomber.setAlive(false);
        }
        if (playerType[1] == 1)
        {
            blackBomber = new Player(17, 1, 2, Color.black, true);
        }
        if (playerType[1] == 2)
        {
            blackBomber = new Player(17, 1, 2, Color.black, false);
        }
        if (playerType[2] == 0)
        {
            redBomber = new Player(17, 13, 3, Color.red, false);
            redBomber.setAlive(false);
        }
        if (playerType[2] == 1)
        {
            redBomber = new Player(17, 13, 3, Color.red, true);
        }
        if (playerType[2] == 2)
        {
            redBomber = new Player(17, 13, 3, Color.red, false);
        }
        if (playerType[3] == 0)
        {
            blueBomber = new Player(1, 13, 4, Color.blue, false);
            blueBomber.setAlive(false);
        }
        if (playerType[3] == 1)
        {
            blueBomber = new Player(1, 13, 4, Color.blue, true);
        }
        if (playerType[3] == 2)
        {
            blueBomber = new Player(1, 13, 4, Color.blue, false);
        }
    }

    private void makeExplosion(int locX, int locY, int size, boolean up, boolean right, boolean left, boolean down)
    {
        shake = true;
        shakeMagnitude += 1;
        int[][] explodefield = new int[19][15];
        /* Diagram to show which numbers equal which directions (confusing, I know)
         *     5
         *     1
         * 8 4 0 2 6
         *     3
         *     7
        */
        for (int i = locX - 1; i >= locX - size; i--)
        {
            if (i >= 0 && i < 19)
            {
                if (left)
                {
                    switch( board[i][locY] )
                    {
                        case 0:
                        {
                            explodefield[i][locY] = 4;
                            break;
                        }
                        case 1:
                        {
                            left = false;
                            break;
                        }
                        case 2:
                        {
                            spawnPowerUps(i, locY);
                            explodefield[i][locY] = 8;
                            left = false;
                            break;
                        }
                        case 3:
                        {
                            if (bombs[i][locY] != null)
                            {
                                boolean[] b = {true, false, true, true};
                                bombs[i][locY].setDirections(b);
                                bombs[i][locY].explode();
                                left = false;
                            }
                            else
                            {
                                explodefield[i][locY] = 4;
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i = locX + 1; i <= locX + size; i++)
        {
            if (i >= 0 && i < 19)
            {
                if (right)
                {
                    switch( board[i][locY] )
                    {
                        case 0:
                        {
                            explodefield[i][locY] = 2;
                            break;
                        }
                        case 1:
                        {
                            right = false;
                            break;
                        }
                        case 2:
                        {
                            spawnPowerUps(i, locY);
                            explodefield[i][locY] = 6;
                            right = false;
                            break;
                        }
                        case 3:
                        {
                            if (bombs[i][locY] != null)
                            {
                                boolean[] b = {true, true, false, true};
                                bombs[i][locY].setDirections(b);
                                bombs[i][locY].explode();
                                right = false;
                            }
                            else
                            {
                                explodefield[i][locY] = 2;
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i = locY - 1; i >= locY - size; i--)
        {
            if (i >= 0 && i < 19)
            {
                if (up)
                {
                    switch( board[locX][i] )
                    {
                        case 0:
                        {
                            explodefield[locX][i] = 1;
                            break;
                        }
                        case 1:
                        {
                            up = false;
                            break;
                        }
                        case 2:
                        {
                            spawnPowerUps(locX, i);
                            explodefield[locX][i] = 5;
                            up = false;
                            break;
                        }
                        case 3:
                        {
                            if (bombs[locX][i] != null)
                            {
                                boolean[] b = {true, true, true, false};
                                bombs[locX][i].setDirections(b);
                                bombs[locX][i].explode();
                                up = false;
                            }
                            else
                            {
                                explodefield[locX][i] = 1;
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i = locY + 1; i <= locY + size; i++)
        {
            if (i >= 0 && i < 19)
            {
                if (down)
                {
                    switch( board[locX][i] )
                    {
                        case 0:
                        {
                            explodefield[locX][i] = 3;
                            break;
                        }
                        case 1:
                        {
                            down = false;
                            break;
                        }
                        case 2:
                        {
                            spawnPowerUps(locX, i);
                            explodefield[locX][i] = 7;
                            down = false;
                            break;
                        }
                        case 3:
                        {
                            if (bombs[locX][i] != null)
                            {
                                boolean[] b = {false, true, true, true};
                                bombs[locX][i].setDirections(b);
                                bombs[locX][i].explode();
                                down = false;
                            }
                            else
                            {
                                explodefield[locX][i] = 3;
                            }
                            break;
                        }
                    }
                }
            }
        }
        explodefield[locX][locY] = 10;
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (explodefield[x][y] != 0)
                {
                    if (x - 1 >= 0)
                    {
                        if (explodefield[x][y] == 4)
                        {
                            if (explodefield[x-1][y] == 0)
                            {
                                explodefield[x][y] = 8;
                            }
                        }
                    }
                    if (x + 1 < 19)
                    {
                        if (explodefield[x][y] == 2)
                        {
                            if (explodefield[x+1][y] == 0)
                            {
                                explodefield[x][y] = 6;
                            }
                        }
                    }
                    if (y - 1 >= 0)
                    {
                        if (explodefield[x][y] == 1)
                        {
                            if (explodefield[x][y-1] == 0)
                            {
                                explodefield[x][y] = 5;
                            }
                        }
                    }
                    if (y + 1 < 15)
                    {
                        if (explodefield[x][y] == 3)
                        {
                            if (explodefield[x][y+1] == 0)
                            {
                                explodefield[x][y] = 7;
                            }
                        }
                    }
                    fire[x][y] = new Fire(explodefield[x][y]);
                }
            }
        }
        //Update the fire graphics as necessary
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                up = false;
                down = false;
                right = false;
                left = false;
                if (fire[x][y] != null)
                {
                    if (x - 1 >= 0)
                    {
                        if (fire[x-1][y] != null)
                        {
                            left = true;
                        }
                    }
                    if (x + 1 < 19)
                    {
                        if (fire[x+1][y] != null)
                        {
                            right = true;
                        }
                    }
                    if (y + 1 < 15)
                    {
                        if (fire[x][y+1] != null)
                        {
                            down = true;
                        }
                    }
                    if (y - 1 >= 0)
                    {
                        if (fire[x][y-1] != null)
                        {
                            up = true;
                        }
                    }
                    if (fire[x][y] != null)
                    {
                        if (up && !down && !left && !right)
                        {
                            fire[x][y].setDirection(7);
                        }
                        if (up && down && !left && !right)
                        {
                            fire[x][y].setDirection(3);
                        }
                        if (!up && !down && !left && right)
                        {
                            fire[x][y].setDirection(8);
                        }
                        if (!up && !down && left && right)
                        {
                            fire[x][y].setDirection(4);
                        }
                        if ((up || down) && (left || right))
                        {
                            fire[x][y].setDirection(10);
                        }
                    }
                }
            }
        }
    }

    private void spawnPowerUps(int x, int y)
    {
        int chanceForPowerUp = mt.nextInt(5);
        switch(chanceForPowerUp)
        {
            case 1:
            {
                board[x][y] = 5;
                break;
            }
            case 2:
            {
                board[x][y] = 6;
                break;
            }
            default:
            {
                board[x][y] = 0;
                break;
            }
        }
    }

    // Pretty much just placeholder script, because the AI is useless at the moment
    private void updateAI(Player player)
    {
        boolean aim[] = {false, false, false, false};
        player.setClock(player.getClock()+1);
        if (player.getClock() > 50)
        {
            int randomDirection = mt.nextInt(4);
            for (int x = 0; x < 19; x++)
            {
                for (int y = 0; y < 15; y++)
                {
                    if (players[x][y] != 0 && players[x][y] != player.getPID())
                    {
                        if (x > player.getX())
                        {
                            aim[1] = true;
                        }
                        if (x < player.getX())
                        {
                            aim[0] = true;
                        }
                        if (y > player.getY())
                        {
                            aim[2] = true;
                        }
                        if (y < player.getY())
                        {
                            aim[3] = true;
                        }
                    }
                }
            }
            int dirScore[] = {1000, 1000, 1000, 1000};
            //Reduce score for walls and fire
            if (player.getX() - 1 >= 0)
            {
                if (fire[player.getX()-1][player.getY()] == null)
                {
                    dirScore[0] -= 400;
                }
                if (board[player.getX()-1][player.getY()] == 1)
                {
                    dirScore[0] = 0;
                }
                if (aim[0])
                {
                    dirScore[0] += 250;
                }
            }
            else
            {
                dirScore[0] = 0;
            }
            if (player.getX() + 1 < 19)
            {
                if (fire[player.getX()+1][player.getY()] == null)
                {
                    dirScore[1] -= 400;
                }
                if (board[player.getX()+1][player.getY()] == 1)
                {
                    dirScore[1] = 0;
                }
                if (aim[1])
                {
                    dirScore[1] += 250;
                }
            }
            else
            {
                dirScore[0] = 0;
            }
            if (player.getY() - 1 >= 0)
            {
                if (fire[player.getX()][player.getY()-1] == null)
                {
                    dirScore[2] -= 400;
                }
                if (board[player.getX()][player.getY()-1] == 1)
                {
                    dirScore[2] = 0;
                }
                if (aim[2])
                {
                    dirScore[2] += 250;
                }
            }
            else
            {
                dirScore[0] = 0;
            }
            if (player.getY() + 1 < 15)
            {
                if (fire[player.getX()][player.getY()+1] == null)
                {
                    dirScore[3] -= 400;
                }
                if (board[player.getX()][player.getY()+1] == 1)
                {
                    dirScore[3] = 0;
                }
                if (aim[3])
                {
                    dirScore[3] += 250;
                }
            }
            else
            {
                dirScore[0] = 0;
            }
            int directionChosen = 0;
            int priorChoice = 0;
            for (int i = 0; i < 4; i++)
            {
                if (dirScore[i] > priorChoice)
                {
                    directionChosen = i;
                    priorChoice = dirScore[i];
                }
            }
            switch(directionChosen)
            {
                case 0:
                {
                    if (player.getX() - 1 >= 0)
                    {
                        boolean b = movePlayer(player, -1, 0);
                        if (b) player.setClock(0);
                    }
                    break;
                }
                case 1:
                {
                    if (player.getX() + 1 < 19)
                    {
                        boolean b = movePlayer(player, 1, 0);
                        if (b) player.setClock(0);
                    }
                    break;
                }
                case 2:
                {
                    if (player.getY() - 1 >= 0)
                    {
                        boolean b = movePlayer(player, 0, -1);
                        if (b) player.setClock(0);
                    }
                    break;
                }
                case 3:
                {
                    if (player.getY() + 1 < 15)
                    {
                        boolean b = movePlayer(player, 0, 1);
                        if (b) player.setClock(0);
                    }
                    break;
                }
            }
        }
    }

    private boolean movePlayer(Player player, int dirX, int dirY)
    {
        boolean moveTile = false;
        boolean allowMove = false;
        if (player.getAlive())
        {
            // 2
            //1 3
            // 0
            if (player.getHuman())
            {
                if (player.getClock() == 0)
                {
                    allowMove = true;
                }
            }
            else
            {
                allowMove = true;
            }
            if (player.getOffSetX() == 0 && player.getOffSetY() == 0 && allowMove)
            {
                if (
                        player.getX() + dirX >= 0 &&
                        player.getX() + dirX < 19 &&
                        player.getY() + dirY >= 0 &&
                        player.getY() < 15
                   )
                {
                    if (dirX > 0)
                    {
                        player.setDirection(3);
                    }
                    if (dirX < 0)
                    {
                        player.setDirection(1);
                    }
                    if (dirY > 0)
                    {
                        player.setDirection(0);
                    }
                    if (dirY < 0)
                    {
                        player.setDirection(2);
                    }
                    if (board[player.getX() + dirX][player.getY() + dirY] == 0 && players[player.getX() + dirX][player.getY() + dirY] == 0)
                    {
                        moveTile = true;
                    }
                    else if (board[player.getX() + dirX][player.getY() + dirY] == 5)
                    {
                        player.setFirePower(player.getFirePower()+1);
                        fireup.play();
                        moveTile = true;
                    }
                    else if (board[player.getX() + dirX][player.getY() + dirY] == 6)
                    {
                        player.setBombAmt(player.getBombAmt()+1);
                        bombup.play();
                        moveTile = true;
                    }
                    if (moveTile)
                    {
                        player.setOffSetTileX(dirX);
                        player.setOffSetTileY(dirY);
                        if (player.getHuman())
                        {
                            if (player.getPID() == 1)
                            {
                                if (input.isKeyDown(Input.KEY_SPACE) && player.getBombAmt() > 0)
                                {
                                    player.setClock(15);
                                }
                            }
                            if (player.getPID() == 2)
                            {
                                if (input.isKeyDown(Input.KEY_SEMICOLON) && player.getBombAmt() > 0)
                                {
                                    player.setClock(15);
                                }
                            }
                        }
                    }
                }
            }
        }
        return moveTile;
    }

    private boolean placeBomb(Player player)
    {
        if (player.getAlive())
        {
            if (player.getOffSetTileX() == 0 && player.getOffSetTileY() == 0)
            {
                if (board[player.getX()][player.getY()] == 0 && player.getAlive())
                {
                    if (player.getBombAmt() > 0)
                    {
                        bombs[player.getX()][player.getY()] = new Bomb(150, player.getFirePower(), player);
                        board[player.getX()][player.getY()] = 3;
                        player.setBombAmt(player.getBombAmt()-1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void checkInputGame(GameContainer container) throws SlickException
    {
        if (input.isKeyPressed(Input.KEY_ESCAPE))
        {
            changeMusic(1);
            bombsong.loop();
            gameState = 0;
        }
        if (input.isKeyPressed(Input.KEY_F4))
        {
            container.setFullscreen( ! container.isFullscreen());
        }
        if (whiteBomber != null)
        {
            if (whiteBomber.getHuman() && whiteBomber.getAlive())
            {
                if (input.isKeyDown(Input.KEY_W))
                {
                    movePlayer(whiteBomber, 0, -1);
                }
                if (input.isKeyDown(Input.KEY_A))
                {
                    movePlayer(whiteBomber, -1, 0);
                }
                if (input.isKeyDown(Input.KEY_S))
                {
                    movePlayer(whiteBomber, 0, 1);
                }
                if (input.isKeyDown(Input.KEY_D))
                {
                    movePlayer(whiteBomber, 1, 0);
                }
                if (input.isKeyDown(Input.KEY_SPACE))
                {
                    placeBomb(whiteBomber);
                }
            }
        }
        if (blackBomber != null)
        {
            if (blackBomber.getHuman() && blackBomber.getAlive())
            {
                if (input.isKeyDown(Input.KEY_I))
                {
                    movePlayer(blackBomber, 0, -1);
                }
                if (input.isKeyDown(Input.KEY_J))
                {
                    movePlayer(blackBomber, -1, 0);
                }
                if (input.isKeyDown(Input.KEY_K))
                {
                    movePlayer(blackBomber, 0, 1);
                }
                if (input.isKeyDown(Input.KEY_L))
                {
                    movePlayer(blackBomber, 1, 0);
                }
                if (input.isKeyDown(Input.KEY_SEMICOLON))
                {
                    placeBomb(blackBomber);
                }
            }
        }
    }

    private void checkBombs()
    {
        for (int x = 0; x < 19; x ++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (bombs[x][y] != null)
                {
                    if (bombs[x][y].getExploded())
                    {
                        if ( ! explosion.playing())
                        {
                            explosion.play();
                        }
                        makeExplosion(x, y, bombs[x][y].getSize(),
                            bombs[x][y].getDirections()[0],
                            bombs[x][y].getDirections()[1],
                            bombs[x][y].getDirections()[2],
                            bombs[x][y].getDirections()[3]);
                        board[x][y] = 0;
                        bombs[x][y] = null;
                    }
                    else
                    {
                        bombs[x][y].update();
                    }
                }
            }
        }
    }

    private void checkFire()
    {
        for (int x = 0; x < 19; x ++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (fire[x][y] != null)
                {
                    if (fire[x][y].getDead())
                    {
                        fire[x][y] = null;
                    }
                    else
                    {
                        fire[x][y].update();
                        if (players[x][y] != 0)
                        {
                            switch(players[x][y])
                            {
                                case 1:
                                {
                                    flushPlayerReferences(1);
                                    whiteBomber.setAlive(false);
                                    players[x][y] = 0;
                                    break;
                                }
                                case 2:
                                {
                                    flushPlayerReferences(2);
                                    blackBomber.setAlive(false);
                                    players[x][y] = 0;
                                    break;
                                }
                                case 3:
                                {
                                    flushPlayerReferences(3);
                                    redBomber.setAlive(false);
                                    players[x][y] = 0;
                                    break;
                                }
                                case 4:
                                {
                                    flushPlayerReferences(4);
                                    blueBomber.setAlive(false);
                                    players[x][y] = 0;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkPlayer(Player player)
    {
        if (player.getAlive())
        {
            players[player.getX()][player.getY()] = player.getPID();
            if (player.getHuman())
            {
                if (player.getClock() > 0)
                {
                    player.setClock(player.getClock()-1);
                }
            }
            else
            {
                updateAI(player);
            }
        }
    }

    private void drawDead(Graphics g, Player player)
    {
        if (player.getDeathClock() > 0)
        {
            if (player.getDeathClock() >= 80)
            {
                deathAnim.getSprite(1, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(0, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 80 && player.getDeathClock() >= 60)
            {
                deathAnim.getSprite(3, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(2, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 60 && player.getDeathClock() >= 40)
            {
                deathAnim.getSprite(5, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(4, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 40 && player.getDeathClock() >= 20)
            {
                deathAnim.getSprite(7, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(6, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 20 && player.getDeathClock() >= 10)
            {
                deathAnim.getSprite(9, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(8, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 10 && player.getDeathClock() >= 0)
            {
                deathAnim.getSprite(11, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(10, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            player.setDeathClock(player.getDeathClock() - 1);
        }
    }

    private void shiftPlayer(Player player)
    {
        if (player.getAlive())
        {
            if (players[player.getX()+player.getOffSetTileX()][player.getY()+player.getOffSetTileY()] == 0)
            {
                players[player.getX()+player.getOffSetTileX()][player.getY()+player.getOffSetTileY()] = player.getPID();
            }
            if (players[player.getX()+player.getOffSetTileX()][player.getY()+player.getOffSetTileY()] == player.getPID())
            {
                if (player.getOffSetTileX() == 1)
                {
                    player.setOffSetX(player.getOffSetX() + 3);
                }
                if (player.getOffSetTileX() == -1)
                {
                    player.setOffSetX(player.getOffSetX() - 3);
                }
                if (player.getOffSetTileY() == 1)
                {
                    player.setOffSetY(player.getOffSetY() + 3);
                }
                if (player.getOffSetTileY() == -1)
                {
                    player.setOffSetY(player.getOffSetY() - 3);
                }
                if (player.getOffSetX() >= 32)
                {
                    players[player.getX()][player.getY()] = 0;
                    board[player.getX() + player.getOffSetTileX()][player.getY() + player.getOffSetTileY()] = 0;
                    player.setOffSetX(0);
                    player.setOffSetTileX(0);
                    player.setX(player.getX()+1);
                }
                if (player.getOffSetY() >= 32)
                {
                    players[player.getX()][player.getY()] = 0;
                    board[player.getX() + player.getOffSetTileX()][player.getY() + player.getOffSetTileY()] = 0;
                    player.setOffSetY(0);
                    player.setOffSetTileY(0);
                    player.setY(player.getY()+1);
                }
                if (player.getOffSetX() <= -32)
                {
                    players[player.getX()][player.getY()] = 0;
                    board[player.getX() + player.getOffSetTileX()][player.getY() + player.getOffSetTileY()] = 0;
                    player.setOffSetX(0);
                    player.setOffSetTileX(0);
                    player.setX(player.getX()-1);
                }
                if (player.getOffSetY() <= -32)
                {
                    players[player.getX()][player.getY()] = 0;
                    board[player.getX() + player.getOffSetTileX()][player.getY() + player.getOffSetTileY()] = 0;
                    player.setOffSetY(0);
                    player.setOffSetTileY(0);
                    player.setY(player.getY()-1);
                }
            }
            else
            {
                player.setOffSetTileX(0);
                player.setOffSetX(0);
            }
        }
    }

    private void drawFire(Graphics g)
    {
        // A diagram to show which numbers coordinate to which bomb direction graphic
        /*     5
         *     1
         * 8 4 0 2 6
         *     3
         *     7
        */
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (fire[x][y] != null)
                {
                    switch(fire[x][y].getDirection())
                    {
                        case 10:
                        {
                            tileset.getSprite(8, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 1:
                        {
                            tileset.getSprite(12, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 2:
                        {
                            tileset.getSprite(9, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 3:
                        {
                            tileset.getSprite(13, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 4:
                        {
                            tileset.getSprite(7, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 5:
                        {
                            tileset.getSprite(11, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 6:
                        {
                            tileset.getSprite(10, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 7:
                        {
                            tileset.getSprite(14, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                        case 8:
                        {
                            tileset.getSprite(6, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void drawTiles(Graphics g)
    {
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                tileset.getSprite(5, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                switch(board[x][y])
                {
                    case 1:
                    {
                        tileset.getSprite(4, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        break;
                    }
                    case 2:
                    {
                        tileset.getSprite(3, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        break;
                    }
                    case 3:
                    {
                        if (bombs[x][y].getTimeLeft() > 80)
                        {
                            bombImage.getSprite(0, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        if (bombs[x][y].getTimeLeft() <= 80 && bombs[x][y].getTimeLeft() > 50)
                        {
                            bombImage.getSprite(1, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        if (bombs[x][y].getTimeLeft() <= 50 && bombs[x][y].getTimeLeft() > 20)
                        {
                            bombImage.getSprite(2, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        if (bombs[x][y].getTimeLeft() <= 20)
                        {
                            bombImage.getSprite(3, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        break;
                    }
                    //4 is player
                    case 5:
                    {
                        if (fire[x][y] == null)
                        {
                            tileset.getSprite(2, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        break;
                    }
                    case 6:
                    {
                        if (fire[x][y] == null)
                        {
                            tileset.getSprite(15, 0).draw(x * 32 + jitterX, y * 32 + jitterY);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void drawPlayer(Graphics g, Player player)
    {
        if (player.getAlive())
        {
            int tileOpaque = 2 * player.getDirection() + 1;
            int tileColored = 2 * player.getDirection();
            tileset.getSprite((tileOpaque), 1).draw(
                    player.getX() * 32 + jitterX + player.getOffSetX(),
                    player.getY() * 32 + jitterY + player.getOffSetY());
            tileset.getSprite((tileColored),1).draw(
                    player.getX() * 32 + jitterX + player.getOffSetX(),
                    player.getY() * 32 + jitterY + player.getOffSetY(),
                    player.getColor());
        }
        else if (player.getDeathClock() > 0)
        {
            drawDead(g, player);
        }
    }

    private void flushPlayerReferences(int PID)
    {
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                if (players[x][y] == PID)
                {
                    players[x][y] = 0;
                }
            }
        }
    }
}