/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jbomber;

public class PlayerAI {

    public void updateAI(Player player, Main main)
    {
        int[][] board = main.getBoard();
        int[][] players = main.getPlayerBoard();
        boolean[][] bombs = main.getBombExistence();
        boolean[][] fire = main.getFireExistence();
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
                        if (fire[x][y] == true)
                        {
                            currentView[x][y] = 1;
                        }
                        if (bombs[x][y] == true)
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
                        randomGrab[i] += Main.mt.nextInt(100) + 1;
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
                            randomGrab[i] += Main.mt.nextInt(100) + 50;
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
                            main.movePlayer(player, 0, -1);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(2);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 1:
                        {
                            main.movePlayer(player, 1, 0);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(3);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 2:
                        {
                            main.movePlayer(player, 0, 1);
                            player.setDirectionToAttack(direction);
                            player.setDirectionToSafety(0);
                            player.setClock(0);
                            player.setPhase(1);
                            break;
                        }
                        case 3:
                        {
                            main.movePlayer(player, -1, 0);
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
                            randomGrab[i] += Main.mt.nextInt(100) + 1;
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
                            main.movePlayer(player, 0, -1);
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            main.movePlayer(player, 1, 0);
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            main.movePlayer(player, 0, 1);
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            main.movePlayer(player, -1, 0);
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
                        if (fire[x][y] == true)
                        {
                            currentView[x][y] = 1;
                        }
                        if (bombs[x][y] == true)
                        {
                            currentView[x][y] = 3;
                        }
                    }
                }
                boolean bombPlaced = false;
                if (Main.mt.nextInt(5) < 3)
                {
                    switch(player.getDirectionToAttack())
                    {
                        case 0:
                        {
                            boolean b = main.movePlayer(player, 0, -1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            boolean b = main.movePlayer(player, 1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            boolean b = main.movePlayer(player, 0, 1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            boolean b = main.movePlayer(player, -1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                    }
                }
                else
                {
                    int newDirection = main.mt.nextInt(4);
                    switch(newDirection)
                    {
                        case 0:
                        {
                            boolean b = main.movePlayer(player, 0, -1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 1:
                        {
                            boolean b = main.movePlayer(player, 1, 0);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 2:
                        {
                            boolean b = main.movePlayer(player, 0, 1);
                            if (!b) bombPlaced = true;
                            player.setClock(0);
                            break;
                        }
                        case 3:
                        {
                            boolean b = main.movePlayer(player, -1, 0);
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
                        main.placeBomb(player);
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
                            main.movePlayer(player, 0, -1);
                            easySolution = true;
                        }
                        if (player.getSafeSpot()[1] > player.getY())
                        {
                            main.movePlayer(player, 0, 1);
                            easySolution = true;
                        }
                    }
                    if (player.getSafeSpot()[1] == player.getY() && Math.abs(player.getSafeSpot()[0] - player.getX()) == 1)
                    {
                        if (player.getSafeSpot()[0] < player.getX())
                        {
                            main.movePlayer(player, -1, 0);
                            easySolution = true;
                        }
                        if (player.getSafeSpot()[0] > player.getX())
                        {
                            main.movePlayer(player, 1, 0);
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
                                boolean b = main.movePlayer(player, 0, -1);
                                if (!b)
                                {
                                    b = main.movePlayer(player, -1, 0);
                                    if (!b)
                                    {
                                        b = main.movePlayer(player, 1, 0);
                                        if (!b)
                                        {
                                            main.movePlayer(player, 0, 1);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 1:
                            {
                                boolean b = main.movePlayer(player, 1, 0);
                                if (!b)
                                {
                                    b = main.movePlayer(player, 0, -1);
                                    if (!b)
                                    {
                                        b = main.movePlayer(player, 0, 1);
                                        if (!b)
                                        {
                                            main.movePlayer(player, -1, 0);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 2:
                            {
                                boolean b = main.movePlayer(player, 0, 1);
                                if (!b)
                                {
                                    b = main.movePlayer(player, -1, 0);
                                    if (!b)
                                    {
                                        b = main.movePlayer(player, 1, 0);
                                        if (!b)
                                        {
                                            main.movePlayer(player, 0, -1);
                                        }
                                    }
                                }
                                player.setClock(0);
                                break;
                            }
                            case 3:
                            {
                                boolean b = main.movePlayer(player, -1, 0);
                                if (!b)
                                {
                                    b = main.movePlayer(player, 0, -1);
                                    if (!b)
                                    {
                                        b = main.movePlayer(player, 0, 1);
                                        if (!b)
                                        {
                                            main.movePlayer(player, 1, 0);
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
}
