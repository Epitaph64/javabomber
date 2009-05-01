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

    //Ability to turn off music (faster load times) for development reasons.
    //This is not a replacement for sound/music controls for the in game menu,
    //which will be addressed soon.
    private final boolean musicOn = false;

    private int gameState = 0;

    /* Game State List for Reference
     *  0 - Menu
     *  1 - In Game
     *
     * Future States Needed
     *
     *  2 - Match Results
    */

    //0 - off 1 - human 2 - CPU
    private int playerType[] = {2,2,2,2};

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
        //Changed from jbomber to avoid confusion with other projects.
        //Will most likely redo title graphic to reflect this change in an
        //upcoming revision.
        super("javaBomber");
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

        //Effect Loading
        fog = new Image("data/fog.png");

        //Sound Loading
        explosion = new Sound("data/explosion.wav");       
        bombup = new Sound("data/bombup.wav");
        fireup = new Sound("data/fireup.wav");

        //Rectangles to click for menu interaction
        play = new Rectangle(200, 140, playButton.getWidth(), playButton.getHeight());
        options = new Rectangle(200, 240, optionsButton.getWidth(), optionsButton.getHeight());
        quit = new Rectangle(200, 340, quitButton.getWidth(), quitButton.getHeight());
        back = new Rectangle(200, 340, backButton.getWidth(), backButton.getHeight());
        p1 = new Rectangle(130, 220, 100, 50);
        p2 = new Rectangle(230, 220, 100, 50);
        p3 = new Rectangle(330, 220, 100, 50);
        p4 = new Rectangle(430, 220, 100, 50);
        
        //Music Loading
        if (musicOn)
        {
            bombsong = new Music("data/bombsong.ogg");
            bombsong.loop();
        }

        //Input Loading
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
            //These commented out lines are for testing the AI
            drawTarget(g, whiteBomber);
            drawTarget(g, blueBomber);
            drawTarget(g, blackBomber);
            drawTarget(g, redBomber);
            drawPlayerPhase(g, whiteBomber);
            drawPlayerPhase(g, blackBomber);
            drawPlayerPhase(g, redBomber);
            drawPlayerPhase(g, blueBomber);
        }
    }

    private void drawPlayerPhase(Graphics g, Player player)
    {
        g.drawString("" + player.getPhase(), player.getX() * 32, player.getY() * 32);
    }

    private void changeMusic(int songNumber) throws SlickException
    {
        if (musicOn)
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
            bombsong.loop();
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
            //Draw large players for player type adjustment
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
                if (x % 2 == 0 && y % 2 == 0)
                {
                    board[x][y] = 1;
                }
                else
                {
                    board[x][y] = 0;
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
        //Clear player areas (better way?)
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
        whiteBomber = new Player(1, 1, 1, Color.white, playerType[0]);
        blackBomber = new Player(17, 1, 2, Color.black, playerType[1]);
        redBomber = new Player(17, 13, 3, Color.red, playerType[2]);
        blueBomber = new Player(1, 13, 4, Color.blue, playerType[3]);
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
        updateFireSprites();
    }

    private void updateFireSprites()
    {
        //Update the fire graphics as necessary
        for (int x = 0; x < 19; x++)
        {
            for (int y = 0; y < 15; y++)
            {
                boolean up = false;
                boolean down = false;
                boolean right = false;
                boolean left = false;
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

    //AI is using poor logic now, but can use bombs
    private void updateAI(Player player)
    {
        player.setClock(player.getClock()+1);
        if (player.getClock() > 15 && player.getAlive() && player.getOffSetTileX() == 0 && player.getOffSetTileY() == 0)
        {
            if (player.getPhase() == 0)
            {
                boolean targetSet = false;
                int[][] currentView = new int[19][15];
                for (int x = 0; x < 19; x++)
                {
                    for (int y = 0; y < 15; y++)
                    {
                        if (board[x][y] == 1)
                        {
                            currentView[x][y] = 1;
                        }
                        if (board[x][y] == 2)
                        {
                            currentView[x][y] = 2;
                        }
                        if (players[x][y] != 0)
                        {
                            currentView[x][y] = 2;
                        }
                        if (fire[x][y] != null)
                        {
                            currentView[x][y] = 1;
                        }
                        if (bombs[x][y] != null)
                        {
                            currentView[x][y] = 3;
                        }
                    }
                }
                boolean directionBlocked[] = new boolean[4];
                if (player.getX() - 1 >= 0)
                {
                    if (currentView[player.getX()-1][player.getY()] != 0)
                    {
                        directionBlocked[3] = true;
                    }
                }
                if (player.getX() + 1 < 19)
                {
                    if (currentView[player.getX()+1][player.getY()] != 0)
                    {
                        directionBlocked[1] = true;
                    }
                }
                if (player.getY() - 1 >= 0)
                {
                    if (currentView[player.getX()][player.getY()-1] != 0)
                    {
                        directionBlocked[0] = true;
                    }
                }
                if (player.getY() + 1 < 15)
                {
                    if (currentView[player.getX()][player.getY()+1] != 0)
                    {
                        directionBlocked[2] = true;
                    }
                }
                int randomGrab[] = new int[4];
                int amountOfChoices = 0;
                for (int i = 0; i < 4; i++)
                {
                    if ( ! directionBlocked[i])
                    {
                        randomGrab[i] += mt.nextInt(100) + 1;
                        amountOfChoices ++;
                    }
                }
                int best = 0;
                int direction = 0;
                for (int i = 0; i < 4; i++)
                {
                    if (randomGrab[i] > best)
                    {
                        best = randomGrab[i];
                        direction = i;
                    }
                }
                if (amountOfChoices >= 2)
                {
                    switch(direction)
                    {
                        case 0:
                        {
                            player.setSafeSpot(player.getX(), player.getY()-1);
                            break;
                        }
                        case 1:
                        {
                            player.setSafeSpot(player.getX()+1, player.getY());
                            break;
                        }
                        case 2:
                        {
                            player.setSafeSpot(player.getX(), player.getY()+1);
                            break;
                        }
                        case 3:
                        {
                            player.setSafeSpot(player.getX()-1, player.getY());
                            break;
                        }
                    }
                    for (int i = 0; i < 4; i++)
                    {
                        if ( ! directionBlocked[i] && i != direction)
                        {
                            randomGrab[i] += mt.nextInt(100) + 50;
                            amountOfChoices ++;
                        }

                    }
                    best = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        if (randomGrab[i] > best)
                        {
                            best = randomGrab[i];
                            direction = i;
                        }
                    }
                    switch(direction)
                    {
                        case 0:
                        {
                            movePlayer(player, 0, -1);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(2);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 1:
                        {
                            movePlayer(player, 1, 0);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(3);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 2:
                        {
                            movePlayer(player, 0, 1);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(0);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 3:
                        {
                            movePlayer(player, -1, 0);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(1);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                    }
                }
                else
                {
                    for (int i = 0; i < 4; i++)
                    {
                        if ( ! directionBlocked[i] && i != direction)
                        {
                            randomGrab[i] += mt.nextInt(100) + 1;
                            amountOfChoices ++;
                        }

                    }
                    best = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        if (randomGrab[i] > best)
                        {
                            best = randomGrab[i];
                            direction = i;
                        }
                    }
                    switch(direction)
                    {
                        case 0:
                        {
                            movePlayer(player, 0, -1);
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            movePlayer(player, 1, 0);
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            movePlayer(player, 0, 1);
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            movePlayer(player, -1, 0);
                            player.setClock(0);
                            break;
                        }
                    }
                }
            }
            if (player.getPhase() == 1)
            {
                int currentView[][] = new int[19][15];
                for (int x = 0; x < 19; x++)
                {
                    for (int y = 0; y < 15; y++)
                    {
                        if (board[x][y] == 1)
                        {
                            currentView[x][y] = 1;
                        }
                        if (board[x][y] == 2)
                        {
                            currentView[x][y] = 2;
                        }
                        if (players[x][y] != 0)
                        {
                            currentView[x][y] = 2;
                        }
                        if (fire[x][y] != null)
                        {
                            currentView[x][y] = 1;
                        }
                        if (bombs[x][y] != null)
                        {
                            currentView[x][y] = 3;
                        }
                    }
                }
                boolean bombPlaced = false;
                if (mt.nextInt(5) < 3)
                {
                    switch(player.getDirectionToAttack())
                    {
                        case 0:
                        {
                            boolean b = movePlayer(player, 0, -1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            boolean b = movePlayer(player, 1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            boolean b = movePlayer(player, 0, 1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            boolean b = movePlayer(player, -1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                    }
                }
                else
                {
                    int newDirection = mt.nextInt(4);
                    switch(newDirection)
                    {
                        case 0:
                        {
                            boolean b = movePlayer(player, 0, -1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            boolean b = movePlayer(player, 1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            boolean b = movePlayer(player, 0, 1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            boolean b = movePlayer(player, -1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                    }
                }
                if (bombPlaced)
                {
                    if (player.getX() != player.getSafeSpot()[0] && player.getY() != player.getSafeSpot()[1])
                    {
                        placeBomb(player);
                        player.setPhase(2);
                        player.setPatience(10);
                    }
                    else
                    {
                        player.setPhase(0);
                    }
                }
            }
            if (player.getPhase() == 2)
            {
                if (player.getPatience() <= 0)
                {
                    player.setPhase(0);
                }
                //The AI becomes a bit obfuscated from here down. This is unintentional, but since this was
                //added in around 30 minutes is why. Most likely, a lot of the AI script will be replaced
                //as this is just a "works for now" kind of method.
                if ( ! (player.getX() == player.getSafeSpot()[0] && player.getY() == player.getSafeSpot()[1]))
                {
                    boolean easySolution = false;
                    if (player.getSafeSpot()[0] == player.getX() && Math.abs(player.getSafeSpot()[1] - player.getY()) == 1)
                    {
                        if (player.getSafeSpot()[1] < player.getY())
                        {
                            movePlayer(player, 0, -1);
                            easySolution = true;
                        }
                        if (player.getSafeSpot()[1] > player.getY())
                        {
                            movePlayer(player, 0, 1);
                            easySolution = true;
                        }
                    }
                    if (player.getSafeSpot()[1] == player.getY() && Math.abs(player.getSafeSpot()[0] - player.getX()) == 1)
                    {
                        if (player.getSafeSpot()[0] < player.getX())
                        {
                            movePlayer(player, -1, 0);
                            easySolution = true;
                        }
                        if (player.getSafeSpot()[0] > player.getX())
                        {
                            movePlayer(player, 1, 0);
                            easySolution = true;
                        }
                    }
                    if (! easySolution)
                    {
                        player.setPatience(player.getPatience()-1);
                        switch(player.getDirectionToSafety())
                        {
                            case 0:
                            {
                                boolean b = movePlayer(player, 0, -1);
                                if (!b)
                                {
                                    b = movePlayer(player, -1, 0);
                                    if (!b)
                                    {
                                        b = movePlayer(player, 1, 0);
                                        if (!b)
                                        {
                                            movePlayer(player, 0, 1);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 1:
                            {
                                boolean b = movePlayer(player, 1, 0);
                                if (!b)
                                {
                                    b = movePlayer(player, 0, -1);
                                    if (!b)
                                    {
                                        b = movePlayer(player, 0, 1);
                                        if (!b)
                                        {
                                            movePlayer(player, -1, 0);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 2:
                            {
                                boolean b = movePlayer(player, 0, 1);
                                if (!b)
                                {
                                    b = movePlayer(player, -1, 0);
                                    if (!b)
                                    {
                                        b = movePlayer(player, 1, 0);
                                        if (!b)
                                        {
                                            movePlayer(player, 0, -1);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 3:
                            {
                                boolean b = movePlayer(player, -1, 0);
                                if (!b)
                                {
                                    b = movePlayer(player, 0, -1);
                                    if (!b)
                                    {
                                        b = movePlayer(player, 0, 1);
                                        if (!b)
                                        {
                                            movePlayer(player, 1, 0);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                        }
                    }
                    else
                    {
                        player.setClock(0);
                    }
                }
                else
                {
                    if (player.getClock() > 200)
                    {
                        player.setPhase(0);
                    }
                }
            }
        }
    }

    private void drawTarget(Graphics g, Player player)
    {
        if (player.getAlive())
        {
            g.setColor(player.getColor());
            g.drawRect(player.getSafeSpot()[0] * 32 + jitterX, player.getSafeSpot()[1] * 32 + jitterY, 32, 32);
        }
    }

    private boolean movePlayer(Player player, int dirX, int dirY)
    {
        boolean moveTile = false;
        boolean allowMove = false;
        if (player.getAlive())
        {
            //I'll probably redo this, since up = 0 and clockwise from there to left being '3' in most other methods.
            //This would be to avoid any confusion with my random number systems.
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
            gameState = 0;
        }
        if (input.isKeyPressed(Input.KEY_F2))
        {
            newRound(playerType);
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
                        updateFireSprites();
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
            if (player.getDeathClock() >= 85)
            {
                deathAnim.getSprite(1, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(0, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 85 && player.getDeathClock() >= 65)
            {
                deathAnim.getSprite(3, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(2, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 65 && player.getDeathClock() >= 45)
            {
                deathAnim.getSprite(5, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(4, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 45 && player.getDeathClock() >= 30)
            {
                deathAnim.getSprite(7, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(6, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 30 && player.getDeathClock() >= 15)
            {
                deathAnim.getSprite(9, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY);
                deathAnim.getSprite(8, 0).draw(player.getX() * 32 + jitterX, player.getY() * 32 + jitterY, player.getColor());
            }
            if (player.getDeathClock() < 15 && player.getDeathClock() >= 0)
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

    //According to profiler, this is the most CPU intensive method (for good reason) but could use optimization
    //If anyone has any suggestions, be sure to suggest them!
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