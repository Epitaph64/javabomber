package jbomber;

public class Fire {

    private int direction;
    private int timeleft;
    private boolean dead;

    Fire(int direction)
    {
        this.direction = direction;
        this.timeleft = 50;
        this.dead = false;
    }

    public void update()
    {
        timeleft --;
        if (timeleft < 1)
        {
            dead = true;
        }
    }

    public void setDirection(int dir)
    {
        direction = dir;
    }

    public int getDirection()
    {
        return direction;
    }

    public boolean getDead()
    {
        return dead;
    }

}
