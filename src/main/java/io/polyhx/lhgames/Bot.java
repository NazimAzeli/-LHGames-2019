package io.polyhx.lhgames;

import java.util.LinkedList;

import io.polyhx.lhgames.data.*;


public class Bot {
    Path currentPath = new Path();

    public Direction getNextDirection(GameInfo info) {
        /***************************
         * PUT YOUR BOT LOGIC HERE *
         ***************************/
        // call get path dans une linked list
        if (currentPath.moves.isEmpty()) {
            Point lastMove = getPointFromMove(info.getHostPlayer().getLastMove());
            LinkedList<Path> paths = getPath(info.getHostPlayer().getPosition(), info.getHostPlayer().getMoveLeft(), lastMove, info);
            // trouve le plus long
            // find longest path
            int bestSize = 0;
            for (Path p : paths) {
                boolean goodPath = isPathViable(info.getHostPlayer().getPosition(), p);
                if (goodPath) {
                    if (p.moves.size() > bestSize) {
                        currentPath = p;
                        bestSize = currentPath.moves.size();

                    }
                }

            }


            // is path viable

        }


        Point nextMove = currentPath.moves.getFirst();
        currentPath.removeFirst();

        //
        return getDirectionFromPoint(nextMove);

    }

    public boolean isPathViable(Point pos, Path path) {
        if(path.moves.size() > 11)
            return false;
        LinkedList<Point> casesVisites = new LinkedList<Point>();
        for (Point move : path.moves) {
            // si la position est deja dans la liste kill it
            pos = pos.add(move);

            if (casesVisites.contains(pos) || !isInBounds(pos))
                return false;
            casesVisites.addLast(pos);


        }


        return true;

    }

    public Direction getDirectionFromPoint(Point move) {
        if (move.equals(new Point(1, 0)))
            return Direction.RIGHT;
        if (move.equals(new Point(-1, 0)))
            return Direction.LEFT;
        if (move.equals(new Point(0, 1)))
            return Direction.DOWN;
        if (move.equals(new Point(0, -1)))
            return Direction.UP;

        return Direction.UNKNOWN;

    }

    public Point getPointFromMove(Direction move) {
        if (move == Direction.UP)
            return new Point(0, -1);
        if (move == Direction.DOWN)
            return new Point(0, 1);
        if (move == Direction.RIGHT)
            return new Point(1, 0);
        if (move == Direction.LEFT)
            return new Point(-1, 0);

        return new Point(0, 0);
    }
    // recursivite baby
    public Path getSimplePath(Point position, Point target){
        Path path = new Path();
        int distanceX = target.getX() - position.getX();
        int distanceY = target.getY() - position.getY();

        for(int i = 0;i<Math.abs(distanceX);i++){
            if(distanceX < 0)
                path.add(new Point(-1,0));
            else
                path.add(new Point(1,0));
        }

        for(int i =0;i<Math.abs(distanceY);i++){
            if(distanceY < 0)
                path.add(new Point(0,-1));
            else
                path.add(new Point(0,1));
        }

        return path;
    }


    public LinkedList<Path> getPath(Point currentPos, int nbMoveLeft, Point movePrec, GameInfo info) {

        LinkedList<Path> paths = new LinkedList<Path>();

        // si on se trouve sur notre territoire et ce n'est pas notre premier move
        boolean onFriendlyTerritoire = surNotreTerritoire(info, info.getMap().getTiles(), currentPos);

        if(onFriendlyTerritoire && info.getHostPlayer().getMoveLeft() == info.getHostPlayer().getMaxMove()){
            LinkedList<Point> movesDispo = trouverMovesDispo(currentPos, movePrec, nbMoveLeft, info);
            LinkedList<Path>pathsToClosestTile = new LinkedList<>();
            for(Point move : movesDispo){
                // Find closest empty case
                for(int i =0;i<16;i++){
                    for(int j=0;j<16;j++){
                        Tile tile = info.getMap().getTiles()[i][j];
                        if(tile.isEmpty()){
                            // found tile and its path
                            Path path = getSimplePath(currentPos,tile.getPosition());
                            pathsToClosestTile.addLast(path);
                        }
                    }
                }

                // find closest path
                int minSize = 100;
                Path minPath = new Path();
                for(Path p : pathsToClosestTile){
                    if(p.moves.size() < minSize){
                        minPath = p;
                        minSize = p.moves.size();
                    }
                }
                // return closest path
                LinkedList<Path>temp = new LinkedList<>();
                temp.add(minPath);
                return temp;
                 // go
            }
        }


        if (onFriendlyTerritoire && nbMoveLeft != info.getHostPlayer().getMaxMove()) {
            Path path = new Path();
            path.add(movePrec);
            paths.add(path);
        } else {
            // Verifier les cases disponibles
            LinkedList<Point> movesDispo = trouverMovesDispo(currentPos, movePrec, nbMoveLeft, info);
            for (Point move : movesDispo) {
                LinkedList<Path> pathsTemp = getPath(currentPos.add(move), nbMoveLeft - 1, move, info);
                for (Path pathTemp : pathsTemp) {
                    pathTemp.addFirst(move);
                    paths.addLast(pathTemp);
                }

            }
        }
        return paths;
    }

    public LinkedList<Point> trouverMovesDispo(Point currentCase, Point moveToGetHere, int nbMoveLeft, GameInfo info) {
        LinkedList<Point> movesDispo = new LinkedList<Point>();

        if (nbMoveLeft != 0) {
            Point moveDroite = new Point(1, 0);
            Point moveGauche = new Point(-1, 0);
            Point moveBas = new Point(0, 1);
            Point moveHaut = new Point(0, -1);
            Team ourTeam = info.getHostPlayer().getTeam();

            // Verifier si on est dans le territoire
            Tile[][] tiles = info.getMap().getTiles();
            boolean seTrouveSurLeTerritoire = surNotreTerritoire(info, tiles, currentCase);


            // Verifier droite
            if (!moveToGetHere.equals(moveGauche) || seTrouveSurLeTerritoire) {

                Point caseDroite = currentCase.add(moveDroite);
                // si case a droite est trailed dont go

                if (isInBounds(caseDroite))
                    if (tiles[caseDroite.getY()][caseDroite.getX()].getTailOwner() != ourTeam) {
                        movesDispo.add(moveDroite);
                    }
            }
            // Verifier case gauche
            if (!moveToGetHere.equals(moveDroite) || seTrouveSurLeTerritoire) {
                Point caseGauche = currentCase.add(moveGauche);
                if (isInBounds(caseGauche)) {
                    if (tiles[caseGauche.getY()][caseGauche.getX()].getTailOwner() != ourTeam)
                        movesDispo.add(moveGauche);
                }
            }
            // Verifier case bas
            if (!moveToGetHere.equals(moveHaut) || seTrouveSurLeTerritoire) {
                Point caseBas = currentCase.add(moveBas);
                if (isInBounds(caseBas)) {
                    if (tiles[caseBas.getY()][caseBas.getX()].getTailOwner() != ourTeam)
                        movesDispo.add(moveBas);
                }
            }

            // Verifier case haut
            if (!moveToGetHere.equals(moveBas) || seTrouveSurLeTerritoire) {
                Point caseHaut = currentCase.add(moveHaut);
                if (isInBounds(caseHaut)) {
                    if (tiles[caseHaut.getY()][caseHaut.getX()].getTailOwner() != ourTeam)
                        movesDispo.add(moveHaut);
                }
            }

        }

        return movesDispo;

    }

    public boolean isInBounds(Point coord) {

        if (coord.getX() < 1 || coord.getX() > 14)
            return false;
        if (coord.getY() < 1 || coord.getY() > 14)
            return false;
        return true;
    }

    public boolean surNotreTerritoire(GameInfo info, Tile[][] tiles, Point currentCase) {
        Team caseTeam = tiles[currentCase.getY()][currentCase.getX()].getTeamOwner();
        Team ourTeam = info.getHostPlayer().getTeam();
        if (caseTeam == ourTeam)
            return true;

        return false;
    }
}


