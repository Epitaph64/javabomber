package jbomber;

import org.newdawn.slick.Color;
import org.newdawn.slick.Input;

public class Player {

    private boolean human;

    private int[] safeSpot = new int[2];
    private int directionToSafety;
    private int directionToAttack;
    private int phase;
    private int patience;

    private boolean[] directions;

    private int offsetX, offsetY;
    private int offsetTileX, offsetTileY;

    private int x,y;
    private int pid;

    private int direction = 0;

    private Color color;

    private int firepower;
    private int bombAmt;

    private boolean alive;
    private int clock;

    private int deathClock;

    private boolean[] moving;

    Player(int x, int y, int number, Color color, int type)
    {
        this.x = x;
        this.y = y;
        this.firepower = 1;
        this.bombAmt = 1;
        this.pid = number;
        this.clock = 0;
        this.color = color;
        if (type == 1)
        {
            this.alive = true;
            this.human = true;
            this.deathClock = 100;
        }
        if (type == 2)
        {
            this.alive = true;
            this.human = false;
            this.deathClock = 100;
        }
    }

    public boolean move(int dirX, int dirY, Main main)
    {
        boolean moveTile = false;
        boolean allowMove = false;
        if (alive)
        {
            //I'll probably redo this, since up = 0 and clockwise from there to left being '3' in most other methods.
            //This would be to avoid any confusion with my random number systems.
            // 2
            //1 3
            // 0
            if (human)
            {
                if (clock == 0)
                {
                    allowMove = true;
                }
            }
            else
            {
                allowMove = true;
            }
            if (offsetX == 0 && offsetY == 0 && allowMove)
            {
                if (
                        x + dirX >= 0 &&
                        x + dirX < 19 &&
                        y + dirY >= 0 &&
                        y < 15
                   )
                {
                    if (dirX > 0)
                    {
                        direction = 3;
                    }
                    if (dirX < 0)
                    {
                        direction = 1;
                    }
                    if (dirY > 0)
                    {
                        direction = 0;
                    }
                    if (dirY < 0)
                    {
                        direction = 2;
                    }
                    if (main.getBoard()[x + dirX][y + dirY] == 0 && main.getPlayerBoard()[x + dirX][y + dirY] == 0)
                    {
                        moveTile = true;
                    }
                    else if (main.getBoard()[x + dirX][y + dirY] == 5)
                    {
                        firepower ++;
                        main.playSound("fireup");
                        moveTile = true;
                    }
                    else if (main.getBoard()[x + dirX][y + dirY] == 6)
                    {
                        bombAmt ++;
                        main.playSound("bombup");
                        moveTile = true;
                    }
                    if (moveTile)
                    {
                        offsetTileX = dirX;
                        offsetTileY = dirY;
                        if (human)
                        {
                            if (pid == 1)
                            {
                                if (main.getInput().isKeyDown(Input.KEY_SPACE) && bombAmt > 0)
                                {
                                    clock = 15;
                                }
                            }
                            if (pid == 2)
                            {
                                if (main.getInput().isKeyDown(Input.KEY_SEMICOLON) && bombAmt > 0)
                                {
                                    clock = 15;
                                }
                            }
                        }
                    }
                }
            }
        }
        return moveTile;
    }

    public void shift(Main main)
    {
        if (alive)
        {
            if (main.getPlayerBoard()[x+offsetTileX][y+offsetTileY] == 0)
            {
                main.getPlayerBoard()[x+offsetTileX][y+offsetTileY] = pid;
            }
            if (main.getPlayerBoard()[x+offsetTileX][y+offsetTileY] == pid)
            {
                if (offsetTileX == 1)
                {
                    offsetX += 3;
                }
                if (offsetTileX == -1)
                {
                    offsetX += -3;
                }
                if (offsetTileY == 1)
                {
                    offsetY += 3;
                }
                if (offsetTileY == -1)
                {
                    offsetY += -3;
                }
                if (offsetX >= 32)
                {
                    main.getPlayerBoard()[x][y] = 0;
                    main.getBoard()[x+offsetTileX][y+offsetTileY] = 0;
                    offsetX = 0;
                    offsetTileX = 0;
                    x += 1;
                }
                if (offsetY >= 32)
                {
                    main.getPlayerBoard()[x][y] = 0;
                    main.getBoard()[x+offsetTileX][y+offsetTileY] = 0;
                    offsetY = 0;
                    offsetTileY = 0;
                    y += 1;
                }
                if (offsetX <= -32)
                {
                    main.getPlayerBoard()[x][y] = 0;
                    main.getBoard()[x+offsetTileX][y+offsetTileY] = 0;
                    offsetX = 0;
                    offsetTileX = 0;
                    x += -1;
                }
                if (offsetY <= -32)
                {
                    main.getPlayerBoard()[x][y] = 0;
                    main.getBoard()[x+offsetTileX][y+offsetTileY] = 0;
                    offsetY = 0;
                    offsetTileY = 0;
                    y += -1;
                }
            }
            else
            {
                offsetTileX = 0;
                offsetX = 0;
                offsetTileY = 0;
                offsetY = 0;
            }
        }
    }

    public boolean placeBomb(Main main)
    {
        if (alive)
        {
            if (offsetTileX == 0 && offsetTileY == 0)
            {
                if (main.getBoard()[x][y] == 0)
                {
                    if (bombAmt > 0)
                    {
                        main.getBombs()[x][y] = new Bomb(150, firepower, this);
                        main.getBoard()[x][y] = 3;
                        bombAmt --;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getPID()
    {
        return pid;
    }

    public void setFirePower(int firep)
    {
        firepower = firep;
    }

    public int getFirePower()
    {
        return firepower;
    }
    
    public int getX()
    {
        return x;
    }

    public void setX(int i)
    {
        x = i;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int i)
    {
        y = i;
    }

    public void setAlive(boolean aliveValue)
    {
        alive = aliveValue;
    }

    public boolean getAlive()
    {
        return alive;
    }

    public void setClock(int clockTime)
    {
        clock = clockTime;
    }

    public int getClock()
    {
        return clock;
    }

    public void setBombAmt(int amount)
    {
        bombAmt = amount;
    }

    public int getBombAmt()
    {
        return bombAmt;
    }

    public void setDirection(int direction)
    {
        this.direction = direction;
    }

    public int getDirection()
    {
        return direction;
    }

    public Color getColor()
    {
        return color;
    }

    public int getOffSetX()
    {
        return offsetX;
    }

    public int getOffSetY()
    {
        return offsetY;
    }

    public void setOffSetX(int x)
    {
        offsetX = x;
    }

    public void setOffSetY(int y)
    {
        offsetY = y;
    }

    public void setOffSetTileX(int x)
    {
        offsetTileX = x;
    }

    public void setOffSetTileY(int y)
    {
        offsetTileY = y;
    }

    public int getOffSetTileX()
    {
        return offsetTileX;
    }

    public int getOffSetTileY()
    {
        return offsetTileY;
    }

    public boolean getHuman()
    {
        return human;
    }

    public void setHuman(boolean mortal)
    {
        human = mortal;
    }

    public void setDirections(boolean direct[])
    {
        directions = direct;
    }

    public boolean[] getDirections()
    {
        return directions;
    }

    public void setDeathClock(int clock)
    {
        deathClock = clock;
    }

    public int getDeathClock()
    {
        return deathClock;
    }

    public void setSafeSpot(int x, int y)
    {
        safeSpot[0] = x;
        safeSpot[1] = y;
    }

    public int[] getSafeSpot()
    {
        return safeSpot;
    }

    public void setDirectionToSafety(int dir)
    {
        directionToSafety = dir;
    }

    public int getDirectionToSafety()
    {
        return directionToSafety;
    }

    public void setDirectionToAttack(int dir)
    {
        directionToAttack = dir;
    }

    public int getDirectionToAttack()
    {
        return directionToAttack;
    }

    public void setPhase(int p)
    {
        phase = p;
    }

    public int getPhase()
    {
        return phase;
    }

    public void setPatience(int pat)
    {
        patience = pat;
    }

    public int getPatience()
    {
        return patience;
    }
}