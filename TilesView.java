package com.example.memorycanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class Card {
    Paint p = new Paint();


    public Card(float x, float y, float width, float height, int color, int visibility) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visibility = visibility;
    }

    int color, backColor = Color.DKGRAY, visibility;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;

    public void draw(Canvas c) {
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x,y, x+width, y+height, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }

    public void setVisibility(Card c, Card card_one) {
        c.color = Color.TRANSPARENT;
        card_one.color = Color.TRANSPARENT;
    }
}

public class TilesView extends View {
    // пауза для запоминания карт
    final int PAUSE_LENGTH = 1; // в секундах
    boolean isOnPauseNow = false;

    // число открытых карт
    int openedCard = 0;

    int first_color = 0; // первая карточка
    int second_color = 0; // вторая карточка
    Card card_one = null;
    boolean flag = true;

    ArrayList<Card> cards = new ArrayList<>();

    //int width, height; // ширина и высота канвы

    int width = 150;
    int height = 250;
    int left = 50;
    int top = 100;
    int colors[] = { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED,
            Color.CYAN, Color.GRAY, Color.BLACK, Color.LTGRAY };
    ArrayList<Integer> arr = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7, 0,1,2,3,4,5,6,7));
    //int[] arr = new int[]{1,2,3,4,5,6,7,8, 1,2,3,4,5,6,7,8}

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int k = 0;
        int cur_color;

        Collections.shuffle(arr);
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cur_color = colors[arr.get(k)];
                cards.add(new Card(left, top, width + 50, 100 + height, cur_color, 1));
                left = left +width + 110;
                k++;
            }
            left = 50;
            top = top + height + 150;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Card c: cards) {
            c.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3) получить координаты касания
        int x = (int) event.getX();
        int y = (int) event.getY();

        // 4) определить тип события
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            // палец коснулся экрана

            for (Card c: cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        Log.d("mytag", "card flipped: " + openedCard);
                        first_color = c.color;
                        card_one = c;
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {
                    // перевернуть карту с задержкой
                    if (c.flip(x, y)) {
                        openedCard ++;
                        second_color = c.color;
                        Log.d("mytag", "first color: " + first_color);
                        Log.d("mytag", "second color: " + second_color);

                        if (first_color == second_color){
                            c.visibility = 0;
                            card_one.visibility = 0;
                            c.setVisibility(c, card_one);
                            flag = false;
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                            isOnPauseNow = false;
                            invalidate();
                        }
                        else{
                        invalidate();
                        flag = true;
                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;
                        }
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public void newGame() {
        // запуск новой игры
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }

        // после паузы, перевернуть все карты обратно


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            flag = false;
            invalidate();
        }
    }
}
