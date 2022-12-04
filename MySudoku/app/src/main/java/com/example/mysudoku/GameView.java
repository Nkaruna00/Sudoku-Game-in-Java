package com.example.mysudoku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class GameView extends View implements GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private GameBoard gameBoard = GameBoard.getGameBoard( GameLevel.MEDIUM );

    private float gridWidth;
    private float gridSeparatorSize;
    private float cellWidth;
    private float buttonWidth;
    private float buttonRadius;
    private float buttonMargin;

    private Bitmap eraserBitmap;
    private Bitmap pencilBitmap;
    private Bitmap littlePencilBitmap;

    public GameView(Context context) { // constructor si n ajoute lla vue par programmation
        super(context);
        this.init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) { // constructor utilise quannd on a jouter des elements a partir du designer
        super(context, attrs);
        this.init();
    }

    private void init(){
        gestureDetector = new GestureDetector( getContext(),  this );
    }

    // ... onSizeChanged, onDraw, ...

    // --- Events handlers ---

    // Override from View
    @Override
    public boolean onTouchEvent(MotionEvent event) { // CETTE METHODE VIENT DE VIEW ET QUON OVVERIDE PAS DE ONGESTURE
        return gestureDetector.onTouchEvent(event);
    }

    // Override from OnGestureDectector
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {  // PRINCIPAL
        RectF rectF;

        // --- Check grid cell click ---  // POUR VOI SI JE CLIQUE SUR LES BOUTTONS
        if ( e.getY() < gridWidth ) {
            int cellX = (int)( e.getX() / cellWidth );
            int cellY = (int)( e.getY() / cellWidth );

            gameBoard.currentCellX = cellX;
            gameBoard.currentCellY = cellY;
            postInvalidate(); // POUR FORCER UNE ACTUALISATION GRAPHIQUE SANS 9A ON MET A JOUR SEULEMENT LES DONNEESS
            return true;
        }

        float buttonLeft = buttonMargin; // POUR VOIR SI CLIQUE SUR LES BOUTONS
        float buttonTop = 9 * cellWidth + gridSeparatorSize / 2;

        if ( gameBoard.currentCellX != -1 && gameBoard.currentCellY != -1 ) {

            // --- Check number buttons ---
            for (int i = 1; i <= 9; i++) {
                rectF = new RectF(buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonWidth);
                if (rectF.contains(e.getX(), e.getY())) {
                    gameBoard.pushValue(i);
                    postInvalidate();
                    return true;
                }

                if (i != 6) {
                    buttonLeft += buttonWidth + buttonMargin;
                } else {
                    buttonLeft = buttonMargin;
                    buttonTop += buttonWidth + buttonMargin;
                }
            }

            // --- eraser button ---
            rectF = new RectF(buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonWidth);
            if (rectF.contains(e.getX(), e.getY())) {
                gameBoard.clearCell();
                this.invalidate();
                return true;
            }
            buttonLeft += buttonWidth + buttonMargin;
        }

        // --- pencil button ---
        rectF = new RectF( buttonLeft, buttonTop, buttonLeft+buttonWidth, buttonTop+buttonWidth );
        if ( rectF.contains( e.getX(), e.getY() ) ) {
            gameBoard.bigNumber = ! gameBoard.bigNumber; // SI IL EST GRAND IL DEVIENT PETIT ET VICE VERSA
            this.invalidate();
            return true;
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    // A l'ouverture de l'activite
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // We compute some sizes
        gridSeparatorSize = (w / 9f) / 20f; // trait de separation entre chaque mini case

        gridWidth = w;    // grille taille de l'écren                              // Size of the grid (it's a square)
        cellWidth = gridWidth / 9f;                     // Size of a cell (it's a square too)

        // CES INFORMATIONS LA PAS BESOIN SI ON UTILISE LES BUTTON ANDROID
        buttonWidth = w / 7f;                           // Size of a button division par 7 car 7 separateurs de button
        buttonRadius = buttonWidth / 10f;               // Size of the rounded corner for a button
        buttonMargin = (w - 6*buttonWidth) / 7f;        // Margin between two buttons


        // We resize for this screen the two images
        eraserBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eraser);
        eraserBitmap = Bitmap.createScaledBitmap(eraserBitmap,
                (int) (buttonWidth*0.8f), (int) (buttonWidth*0.8f), false);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pencil);
        pencilBitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (buttonWidth*0.8f), (int) (buttonWidth*0.8), false);
        littlePencilBitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (buttonWidth/3), (int) (buttonWidth/3), false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // --- Draw cells ---

        paint.setTextAlign( Paint.Align.CENTER ); // POUR ALIGNER LE TEXTE AU CENTRE

        for( int y=0; y<9; y++ ) {
            for( int x=0; x<9; x++ ) {
                int backgroundColor = Color.WHITE;

                // Highlight the current row, current column and the current block
                // A value can be appeared only one time into all highlighted cells.
                if ( gameBoard.currentCellX != -1 && gameBoard.currentCellY != -1 ) {
                    if ( (x / 3 == gameBoard.currentCellX / 3 && y / 3 == gameBoard.currentCellY / 3) ||
                            (x == gameBoard.currentCellX && y != gameBoard.currentCellY) ||
                            (x != gameBoard.currentCellX && y == gameBoard.currentCellY)  ) {
                        backgroundColor = 0xFF_FF_F0_F0;
                    }
                }

                // Check if cell is initially proposed: in this case, the background is grey
                if ( gameBoard.cells[y][x].isInitial ) {
                    if ( backgroundColor == 0xFF_FF_F0_F0 ) {
                        backgroundColor = 0xFF_F4_F0_F0;
                    } else {
                        backgroundColor = 0xFF_F0_F0_F0;
                    }
                }
                // Change the color for the currently selected value
                if ( gameBoard.getSelectedValue() > 0 &&
                        gameBoard.cells[y][x].assumedValue == gameBoard.getSelectedValue() ) {
                    backgroundColor = 0xFF_C7_DA_F8;
                }
                // Display errors (conflicts) in red color: an error appear if a value is present
                // at least two times in the same line, column or block.
                if ( gameBoard.cells[y][x].assumedValue > 0 ) { // SI YA DEJA UN CHIFFRE SUR LA MME LIGNE
                    for( int tx=0; tx<9; tx++ ) {
                        if ( tx != x &&
                                gameBoard.cells[y][tx].assumedValue == gameBoard.cells[y][x].assumedValue ) {
                            backgroundColor = 0xFF_FF_00_00;
                            break;
                        }
                    }
                    if ( backgroundColor != 0xFF_FF_00_00 ) { // SI YA DEJA UN CHIFRE SUR LA MM COLONNE
                        for (int ty = 0; ty < 9; ty++) {
                            if ( ty != y &&
                                    gameBoard.cells[ty][x].assumedValue == gameBoard.cells[y][x].assumedValue ) {
                                backgroundColor = 0xFF_FF_00_00;
                                break;
                            }
                        }
                    }
                    if ( backgroundColor != 0xFF_FF_00_00 ) { // SI YA UN CHIFFRE SUR LE MEME BLOC
                        int bx = x / 3;
                        int by = y / 3;
                        for (int dy = 0; dy < 3; dy++) {
                            for (int dx = 0; dx < 3; dx++) {
                                int tx = bx * 3 + dx;
                                int ty = by * 3 + dy;
                                if ( tx != x && ty != y &&
                                        gameBoard.cells[ty][tx].assumedValue == gameBoard.cells[y][x].assumedValue ) {
                                    backgroundColor = 0xFF_FF_00_00;
                                    break;
                                }
                            }
                        }
                    }
                }


                // Draw the background for the current cell   LE BACKGROUND GRIS
                paint.setColor( backgroundColor );
                canvas.drawRect(x * cellWidth,
                        y * cellWidth ,
                        (x+1) * cellWidth,
                        (y+1) * cellWidth,
                        paint);

                if (gameBoard.cells[y][x].assumedValue != 0) {

                    // Draw the assumed value for the cell.
                    paint.setColor(0xFF000000);
                    paint.setTextSize( cellWidth*0.7f );
                    canvas.drawText("" + gameBoard.cells[y][x].assumedValue,
                            x * cellWidth + cellWidth / 2,
                            y * cellWidth + cellWidth * 0.75f, paint);

                } else {   //  CA CEST POUR LES MARQUES

                    // Draw each mark if exists
                    paint.setTextSize( cellWidth*0.33f ); //TAILLE 33 % EN GRIS
                    paint.setColor( 0xFFA0A0A0 );
                    if ( gameBoard.cells[y][x].marks[0] ) { // ON REGARDE SI YA UNE MARQUE A APRTIR DU TABLEAU BOOLEAN MARK
                        paint.setColor(gameBoard.getSelectedValue()==1 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("1",
                                x * cellWidth + cellWidth * 0.2f,
                                y * cellWidth + cellWidth * 0.3f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[1] ) {
                        paint.setColor(gameBoard.getSelectedValue()==2 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("2",
                                x * cellWidth + cellWidth * 0.5f,
                                y * cellWidth + cellWidth * 0.3f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[2] ) {
                        paint.setColor(gameBoard.getSelectedValue()==3 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("3",
                                x * cellWidth + cellWidth * 0.8f,
                                y * cellWidth + cellWidth * 0.3f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[3] ) {
                        paint.setColor(gameBoard.getSelectedValue()==4 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("4",
                                x * cellWidth + cellWidth * 0.2f,
                                y * cellWidth + cellWidth * 0.6f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[4] ) {
                        paint.setColor(gameBoard.getSelectedValue()==5 ? 0xFF4084EF : 0xFFA0A0A0);

                        canvas.drawText("5",
                                x * cellWidth + cellWidth * 0.5f,
                                y * cellWidth + cellWidth * 0.6f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[5] ) {
                        paint.setColor(gameBoard.getSelectedValue()==6 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("6",
                                x * cellWidth + cellWidth * 0.8f,
                                y * cellWidth + cellWidth * 0.6f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[6] ) {
                        paint.setColor(gameBoard.getSelectedValue()==7 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("7",
                                x * cellWidth + cellWidth * 0.2f,
                                y * cellWidth + cellWidth * 0.9f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[7] ) {
                        paint.setColor(gameBoard.getSelectedValue()==8 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("8",
                                x * cellWidth + cellWidth * 0.5f,
                                y * cellWidth + cellWidth * 0.9f, paint);
                    }
                    if ( gameBoard.cells[y][x].marks[8] ) {
                        paint.setColor(gameBoard.getSelectedValue()==9 ? 0xFF4084EF : 0xFFA0A0A0);
                        canvas.drawText("9",
                                x * cellWidth + cellWidth * 0.8f,
                                y * cellWidth + cellWidth * 0.9f, paint);
                    }
                }
            }
        }

        // --- Draw the grid lines ---
        paint.setColor( Color.GRAY );
        paint.setStrokeWidth( gridSeparatorSize/2 );
        for( int i=0; i<=9; i++ ) {  // 9 INCLUS CAR IL FAUT 10 LIGNES POUR UNE LIGNE (36:35) DANS LE PROGRAMME
            canvas.drawLine( i*cellWidth, 0, i*cellWidth, cellWidth*9, paint );
            canvas.drawLine( 0,i*cellWidth, cellWidth*9, i*cellWidth, paint );
        }

        // LA ON FAIT LES NOIRS  SEPAREMENT APRRES LES BLANCS POUR QUE LE NOIR SOIT PAR DESSUS ET NON LE GRIS
        paint.setColor( Color.BLACK );
        paint.setStrokeWidth( gridSeparatorSize );
        for( int i=0; i<=3; i++ ) {
            canvas.drawLine( i*(cellWidth*3), 0, i*(cellWidth*3), cellWidth*9, paint );
            canvas.drawLine( 0,i*(cellWidth*3), cellWidth*9, i*(cellWidth*3), paint );
        }

        //  HIGHTLIGHT LA CELLULE SELECTIONNE
        // --- Draw border for the current selected cell ---
        if ( gameBoard.currentCellX != -1 && gameBoard.currentCellY != -1 ) {
            paint.setColor( 0xFF_30_3F_9F );
            paint.setStrokeWidth( gridSeparatorSize * 1.5f ); // TAILLE DU STYLO PLUS GRANDE  ON MULTIPLIE OAR 1.5F CAR LA FONCTION PREND DES FLOTTANT ET PAS DES DOUBLES
            paint.setStyle( Paint.Style.STROKE ); // POUR NE COLORIER LINTERIEUR DE LA CELLULE ET CACHER LES CHIFFRES A LINTERIEUR
            canvas.drawRect( gameBoard.currentCellX * cellWidth, // POUR FAIRE RESORTIR LA CELLULE
                    gameBoard.currentCellY * cellWidth,
                    (gameBoard.currentCellX+1) * cellWidth,
                    (gameBoard.currentCellY+1) * cellWidth,
                    paint);
            paint.setStyle( Paint.Style.FILL_AND_STROKE );
            paint.setStrokeWidth( 1 );
        }

        // --- Buttons bar ---

        float buttonsTop = 9*cellWidth + gridSeparatorSize/2;  // A QUELLE HAUTEUR ON PLACE LE BUTTON + SEPERAATOR POUR PAS ECRASER SUR LA LIGNE

        paint.setColor(0xFFC7DAF8);  // BLEU EN ARRIERE PLAN
        canvas.drawRect(0, buttonsTop, gridWidth, getHeight(), paint); // DESSINE A PARTIR DU TOP ET TOUT LE RESTE DU COMPOSANT VIEW EN BLEU

        float buttonLeft = buttonMargin;
        float buttonTop = buttonsTop + buttonMargin;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(buttonWidth * 0.7f); // TAILLE DU TEXTE

        for (int i = 1; i <= 9; i++) {
            paint.setColor( 0xFFFFFFFF ); // CEST DU BLANC
            // Attention aux new !!! Mais ici, on n'est pas trop gourmand
            // Il existe une autre version de drawRoundRect, mais elle necessite
            // que vous modifiez la version minimale supportee pour Android :-(
            RectF rectF = new RectF(buttonLeft, buttonTop,
                    buttonLeft + buttonWidth, buttonTop + buttonWidth);
            canvas.drawRoundRect(rectF, buttonRadius, buttonRadius, paint); // ROUND RECT POUR AVOIR DES ANGLES ARRONDIS A PARTIR DU BUTTON RADIUS

            paint.setColor( 0xFF000000 );
            canvas.drawText("" + i, rectF.centerX(), rectF.top + rectF.height() * 0.75f, paint);

            if (i != 6) { // ON DESSINE 1 BOUTON ? ON DECALE ET QUAND ON EN A MIS 6 ON RESET LE LEFT ET DESCEND DE HAUTEUR
                buttonLeft += buttonWidth + buttonMargin;
            } else {
                buttonLeft = buttonMargin;
                buttonTop += buttonWidth + buttonMargin;
            }
        }

        int imageWidth = (int) (buttonWidth * 0.8f);
        int imageMargin = (int) (buttonWidth * 0.1f);

        // --- eraser ---
        paint.setColor(0xFFFFFFFF);
        RectF rectF = new RectF( buttonLeft, buttonTop,
                buttonLeft + buttonWidth, buttonTop + buttonWidth );
        canvas.drawRoundRect( rectF, buttonRadius, buttonRadius, paint );
        canvas.drawBitmap( eraserBitmap,
                buttonLeft + imageMargin, buttonTop + imageMargin, paint );
        buttonLeft += buttonWidth + buttonMargin;

        // --- pencil ---
        paint.setColor(0xFFFFFFFF);
        rectF = new RectF( buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonWidth );
        canvas.drawRoundRect( rectF, buttonRadius, buttonRadius, paint );
        Bitmap bitmap = gameBoard.bigNumber ? pencilBitmap : littlePencilBitmap;
        canvas.drawBitmap( bitmap, buttonLeft + imageMargin, buttonTop + imageMargin, paint );



    }
}
