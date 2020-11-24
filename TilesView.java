package com.example.memorycanvas;

import android.annotation.SuppressLint;
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

    int color, backColor = Color.argb(255,211, 211, 211), visibility;
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

    public void setVisibility(Card card_one, Card card_two) {
        card_one.visibility = 0;
        card_one.color = Color.TRANSPARENT;

        card_two.visibility = 0;
        card_two.color = Color.TRANSPARENT;
    }
}

public class TilesView extends View {
    // пауза для запоминания карт
    final int PAUSE_LENGTH = 1; // в секундах
    boolean isOnPauseNow = false;

    // число открытых карт
    int openedCard = 0;

    int first_color = 0;
    int second_color = 0;
    Card card_one = null;
    boolean flag = true;

    boolean end = true;

    ArrayList<Card> cards = new ArrayList<>();

    int width = 150;
    int height = 250;
    int left = 50;
    int top = 120;
    int colors[] = { Color.argb(255,255,127,80),
            Color.argb(255,255,215,0),
            Color.argb(255,189,183, 107),
            Color.argb(255,95, 158, 160),
            Color.argb(255,255, 228, 181),
            Color.argb(255,216, 191, 216),
            Color.argb(255,165, 42, 42),
            Color.argb(255,107, 142, 35) };
    ArrayList<Integer> arr = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7, 0,1,2,3,4,5,6,7));

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
                cards.add(new Card(left, top, width + 50, 120 + height, cur_color, 1));
                left = left +width + 110;
                k++;
            }
            left = 50;
            top = top + height + 170;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Card c: cards) {
            if (c.visibility != 0)
                c.draw(canvas);
        }
        end = true;
        for (Card c: cards) {
            if (c.visibility != 0) {
                end = false;
                break;
            }
        }

        if (end) {
            Paint p;
            p = new Paint();
            p.setColor(Color.BLACK);
            p.setTextSize(60);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("ВЫ ВЫИГРАЛИ!", 250, 525, p);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            for (Card c: cards) {
                if (c.visibility == 0)
                    continue;
                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        first_color = c.color;
                        card_one = c;
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }
                if (openedCard == 1) {
                    if (c.flip(x, y)) {
                        openedCard ++;
                        second_color = c.color;
                        if ((c.x == card_one.x) && (c.y == card_one.y)) {
                            c.isOpen = isOnPauseNow = false;
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                            invalidate();
                            return true;
                        }

                        if (first_color == second_color){
                            c.visibility = card_one.visibility = 0;
                            c.setVisibility(c, card_one);
                            flag = isOnPauseNow = false;
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                            invalidate();
                        }
                        else{
                            invalidate();
                            flag = isOnPauseNow = true;
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                        }
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public void newGame() {
        end = false;
        cards.clear();
        int k = 0;
        int cur_color;
        width = 150;
        height = 250;
        left = 50;
        top = 120;

        Collections.shuffle(arr);
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cur_color = colors[arr.get(k)];
                cards.add(new Card(left, top, width + 50, 120 + height, cur_color, 1));
                left = left +width + 110;
                k++;
            }
            left = 50;
            top = top + height + 170;
        }
        invalidate();
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000);
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }

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
