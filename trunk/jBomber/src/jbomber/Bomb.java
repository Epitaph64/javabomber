package jbomber;

public class Bomb {

    private int timeleft;
    private int size;
    private boolean exploded;
    private Player creator;
    private boolean[] directions = new boolean[4];

    Bomb(int timeleft, int size, Player player)
    {
        this.timeleft = timeleft;
        this.size = size;
        this.exploded = false;
        this.creator = player;
        this.directions[0] = true;
        this.directions[1] = true;
        this.directions[2] = true;
        this.directions[3] = true;
    }

    public void update()
    {
        timeleft --;
        if (timeleft < 1)
        {
            explode();
        }
    }

    public int getTimeLeft()
    {
        return timeleft;
    }

    public void explode()
    {
        if (creator != null)
        {
            creator.setBombAmt(creator.getBombAmt()+1);
        }
        exploded = true;
    }

    public boolean getExploded()
    {
        return exploded;
    }

    public int getSize()
    {
        return size;
    }

    public boolean[] getDirections()
    {
        return directions;
    }

    public void setDirections(boolean[] b)
    {
        directions = b;
    }

}
