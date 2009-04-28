package jbomber;

import org.newdawn.slick.Color;

public class Player {

    private boolean human;

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
        this.firepower = 5;
        this.bombAmt = 5;
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
}