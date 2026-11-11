package com.homeworkboard.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** A scale-independent, touch-friendly rendering of the Homework Board dashboard. */
public class HomeworkBoardView extends View {
    private static final float DESIGN_W = 1600f;
    private static final float DESIGN_H = 900f;
    private static final int NAVY = Color.rgb(4, 23, 77);
    private static final int BLUE = Color.rgb(16, 101, 235);
    private static final int GREEN = Color.rgb(0, 169, 53);
    private static final int SOFT_BLUE = Color.rgb(238, 244, 255);

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final SharedPreferences prefs;
    private int tuesdayCompleted;

    private final String[][] tasks = {
            {"Math – Pg. 12–14", "Reading – Ch. 3", "Science – Worksheet"},
            {"Math – Pg. 15–17", "Reading – Ch. 4", "Science – Experiment\nNotes"},
            {"Math – Pg. 18–20", "Reading – Ch. 5", "Science – Worksheet"},
            {"Math – Pg. 21–23", "Reading – Ch. 6", "Science – Project Step 1"},
            {"Math – Review", "Reading – Ch. 7", "Science – Project Step 2"}
    };

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            invalidate();
            clockHandler.postDelayed(this, 30_000);
        }
    };

    public HomeworkBoardView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        prefs = context.getSharedPreferences("homework-board", Context.MODE_PRIVATE);
        tuesdayCompleted = prefs.getInt("tuesday-completed", 1);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        setContentDescription("My Homework Board dashboard. Tap Tuesday task rows to mark them complete.");
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        clockHandler.post(clockTick);
    }

    @Override protected void onDetachedFromWindow() {
        clockHandler.removeCallbacks(clockTick);
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.min(getWidth() / DESIGN_W, getHeight() / DESIGN_H);
        float dx = (getWidth() - DESIGN_W * scale) / 2f;
        float dy = (getHeight() - DESIGN_H * scale) / 2f;
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        drawBackground(canvas);
        drawHeader(canvas);
        drawDayCards(canvas);
        drawFooter(canvas);
        canvas.restore();
    }

    private void drawBackground(Canvas c) {
        p.setShader(new LinearGradient(0, 0, DESIGN_W, DESIGN_H,
                new int[]{Color.rgb(248, 251, 255), Color.rgb(225, 236, 255), Color.WHITE},
                null, Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);
        c.drawRect(0, 0, DESIGN_W, DESIGN_H, p);
        p.setShader(null);
        p.setColor(Color.argb(28, 25, 96, 215));
        for (int i = 0; i < 7; i++) c.drawCircle(1380 + i * 35, 40 + i * 20, 120, p);
    }

    private void drawHeader(Canvas c) {
        drawBookIcon(c, 54, 38, 72, 61);
        text(c, "My Homework Board", 140, 90, 55, NAVY, true, Paint.Align.LEFT);

        Date now = new Date();
        String date = new SimpleDateFormat("EEE, MMM d", Locale.US).format(now);
        String time = new SimpleDateFormat("h:mm a", Locale.US).format(now);
        text(c, date, 1185, 80, 29, NAVY, true, Paint.Align.LEFT);
        p.setColor(NAVY); p.setStrokeWidth(2); c.drawLine(1365, 50, 1365, 86, p);
        text(c, time, 1400, 80, 29, NAVY, true, Paint.Align.LEFT);
        drawWifi(c, 1545, 68);
        p.setColor(Color.rgb(198, 216, 247)); p.setStrokeWidth(2); c.drawLine(30, 118, 1570, 118, p);
    }

    private void drawDayCards(Canvas c) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        float gap = 12, startX = 22, cardW = (1556 - gap * 4) / 5f;
        for (int i = 0; i < 5; i++) {
            float x = startX + i * (cardW + gap);
            drawDayCard(c, i, days[i], x, 132, cardW, 493);
        }
    }

    private void drawDayCard(Canvas c, int day, String title, float x, float y, float w, float h) {
        boolean monday = day == 0;
        boolean tuesday = day == 1;
        boolean locked = day >= 2 && tuesdayCompleted < 3;
        int border = monday ? Color.rgb(22, 183, 76) : (tuesday ? BLUE : Color.rgb(116, 143, 190));

        shadowRoundRect(c, x, y, x + w, y + h, 21, Color.WHITE, border, 2.2f, 0, 8, 18);

        p.setShader(new LinearGradient(x, y, x, y + 104,
                monday ? new int[]{Color.rgb(18, 187, 67), Color.rgb(0, 151, 49)}
                        : tuesday ? new int[]{Color.rgb(19, 126, 244), Color.rgb(0, 86, 218)}
                        : new int[]{Color.rgb(112, 137, 181), Color.rgb(73, 99, 146)},
                null, Shader.TileMode.CLAMP));
        roundRect(c, x, y, x + w, y + 106, 20, p, true);
        p.setShader(null);
        p.setColor(monday ? Color.rgb(0, 151, 49) : tuesday ? Color.rgb(0, 86, 218) : Color.rgb(73, 99, 146));
        c.drawRect(x, y + 75, x + w, y + 107, p);

        if (locked) {
            drawLock(c, x + 34, y + 38, 28, Color.rgb(227, 235, 251));
            text(c, title, x + 88, y + 48, 31, Color.WHITE, true, Paint.Align.LEFT);
            text(c, "Locked", x + w / 2 + 22, y + 82, 24, Color.WHITE, false, Paint.Align.CENTER);
        } else if (monday) {
            text(c, title, x + w / 2 - 7, y + 48, 34, Color.WHITE, true, Paint.Align.CENTER);
            drawCheck(c, x + w - 74, y + 36, 25, Color.WHITE, false);
            text(c, "Completed", x + w / 2, y + 82, 24, Color.WHITE, false, Paint.Align.CENTER);
        } else {
            text(c, title, x + w / 2, y + 62, 34, Color.WHITE, true, Paint.Align.CENTER);
        }

        float rowY = y + 106;
        for (int r = 0; r < 3; r++) {
            boolean done = monday || (tuesday && r < tuesdayCompleted);
            boolean active = tuesday && r == tuesdayCompleted && tuesdayCompleted < 3;
            drawTaskRow(c, day, r, x + 7, rowY + r * 73, w - 14, 69, tasks[day][r], done, active, locked);
        }

        float messageY = y + 325;
        if (monday) {
            p.setShader(new LinearGradient(x, messageY, x + w, y + h,
                    Color.rgb(217, 250, 217), Color.rgb(181, 244, 177), Shader.TileMode.CLAMP));
            roundRect(c, x + 2, messageY, x + w - 2, y + h - 2, 17, p, true); p.setShader(null);
            drawStar(c, x + 53, messageY + 62, 23, Color.rgb(255, 196, 0), GREEN);
            text(c, "Day Complete!", x + 94, messageY + 70, 27, Color.rgb(0, 31, 20), true, Paint.Align.LEFT);
        } else if (tuesday && tuesdayCompleted < 3) {
            p.setColor(Color.rgb(225, 231, 244)); roundRect(c, x + 3, messageY, x + w - 3, y + h - 59, 17, p, true);
            drawLock(c, x + 28, messageY + 42, 25, Color.rgb(80, 108, 153));
            text(c, "Opens after the", x + 77, messageY + 36, 21, NAVY, false, Paint.Align.LEFT);
            text(c, "incomplete homework", x + 77, messageY + 62, 21, NAVY, false, Paint.Align.LEFT);
            text(c, "is complete.", x + 77, messageY + 88, 21, NAVY, false, Paint.Align.LEFT);
            drawProgress(c, x + 22, y + h - 54, w - 44, tuesdayCompleted / 3f, BLUE);
            text(c, tuesdayCompleted + " of 3 completed", x + w / 2, y + h - 16, 19, Color.rgb(10, 20, 42), true, Paint.Align.CENTER);
        } else if (tuesday) {
            p.setColor(Color.rgb(217, 250, 217)); roundRect(c, x + 2, messageY, x + w - 2, y + h - 2, 17, p, true);
            drawStar(c, x + 53, messageY + 62, 23, Color.rgb(255, 196, 0), GREEN);
            text(c, "Day Complete!", x + 94, messageY + 70, 27, Color.rgb(0, 31, 20), true, Paint.Align.LEFT);
        } else {
            p.setColor(Color.rgb(225, 231, 244)); roundRect(c, x + 3, messageY, x + w - 3, y + h - 59, 17, p, true);
            drawLock(c, x + 28, messageY + 42, 25, Color.rgb(80, 108, 153));
            text(c, "Opens after the", x + 77, messageY + 36, 20, NAVY, false, Paint.Align.LEFT);
            text(c, "incomplete homework", x + 77, messageY + 61, 20, NAVY, false, Paint.Align.LEFT);
            text(c, "is complete.", x + 77, messageY + 86, 20, NAVY, false, Paint.Align.LEFT);
            drawProgress(c, x + 22, y + h - 54, w - 44, 0, BLUE);
            text(c, "0 of 3 completed", x + w / 2, y + h - 16, 19, Color.rgb(10, 20, 42), true, Paint.Align.CENTER);
        }
    }

    private void drawTaskRow(Canvas c, int day, int row, float x, float y, float w, float h,
                             String label, boolean done, boolean active, boolean lockedDay) {
        if (active) {
            p.setShader(new LinearGradient(x, y, x + w, y + h,
                    Color.rgb(255, 246, 199), Color.rgb(255, 223, 116), Shader.TileMode.CLAMP));
        } else {
            p.setShader(new LinearGradient(x, y, x + w, y + h,
                    Color.rgb(251, 253, 255), Color.rgb(231, 238, 252), Shader.TileMode.CLAMP));
        }
        shadowRoundRect(c, x, y + 3, x + w, y + h, 15, Color.WHITE, Color.TRANSPARENT, 0, 0, 3, 5);
        roundRect(c, x, y + 3, x + w, y + h, 15, p, true);
        p.setShader(null);

        float iconX = x + 20, cy = y + h / 2 + 2;
        if (day == 1 && row == 2 && tuesdayCompleted < 2) {
            drawLock(c, iconX + 1, cy - 1, 22, Color.rgb(80, 108, 153));
        } else if (done) {
            p.setColor(GREEN); roundRect(c, iconX, cy - 19, iconX + 38, cy + 19, 5, p, true);
            drawCheck(c, iconX + 19, cy, 21, Color.WHITE, false);
        } else if (active) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7); p.setColor(BLUE);
            c.drawArc(new RectF(iconX, cy - 20, iconX + 40, cy + 20), -85, 280, false, p);
            p.setStyle(Paint.Style.FILL);
        } else {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.5f); p.setColor(Color.rgb(157, 177, 211));
            roundRect(c, iconX, cy - 18, iconX + 35, cy + 18, 5, p, false); p.setStyle(Paint.Style.FILL);
        }

        float tx = x + 72;
        if (label.contains("\n")) {
            String[] lines = label.split("\\n");
            text(c, lines[0], tx, cy - 5, 21, NAVY, false, Paint.Align.LEFT);
            text(c, lines[1], tx, cy + 22, 21, NAVY, false, Paint.Align.LEFT);
        } else {
            text(c, label, tx, active ? cy - 3 : cy + 8, 22, active ? Color.rgb(35, 28, 3) : NAVY, active, Paint.Align.LEFT);
        }
        if (active) text(c, "In Progress", tx, cy + 24, 18, Color.rgb(45, 35, 0), false, Paint.Align.LEFT);
    }

    private void drawFooter(Canvas c) {
        float y = 646, h = 225;
        shadowRoundRect(c, 22, y, 1578, y + h, 22, Color.rgb(249, 251, 255), Color.rgb(151, 181, 241), 2, 0, 6, 14);
        p.setColor(Color.rgb(39, 58, 118)); p.setStrokeWidth(2); c.drawLine(512, y + 18, 512, y + h - 18, p);
        c.drawLine(1115, y + 18, 1115, y + h - 18, p);

        drawRefresh(c, 87, y + 79, 42);
        text(c, "Automatic Update", 150, y + 53, 27, Color.rgb(6, 31, 139), true, Paint.Align.LEFT);
        text(c, "at 12:00 AM", 150, y + 88, 27, Color.rgb(6, 31, 139), true, Paint.Align.LEFT);
        text(c, "Moves pending tasks to the next day", 150, y + 121, 17, NAVY, false, Paint.Align.LEFT);
        drawPeople(c, 520, y + 51);

        p.setShader(new LinearGradient(650, y, 1115, y + h,
                Color.rgb(255, 247, 248), Color.rgb(255, 216, 222), Shader.TileMode.CLAMP));
        c.drawRect(650, y + 1, 1114, y + h - 1, p); p.setShader(null);
        drawBell(c, 702, y + 52, Color.rgb(204, 28, 32));
        text(c, "Parent Alert", 750, y + 52, 29, Color.rgb(190, 17, 25), true, Paint.Align.LEFT);
        text(c, "If 3 consecutive days have tasks", 750, y + 88, 19, Color.rgb(35, 11, 20), false, Paint.Align.LEFT);
        text(c, "in Not Started or In Progress,", 750, y + 116, 19, Color.rgb(35, 11, 20), false, Paint.Align.LEFT);
        text(c, "parents will be notified.", 750, y + 144, 19, Color.rgb(35, 11, 20), false, Paint.Align.LEFT);

        drawTrophy(c, 1147, y + 33);
        text(c, "Streaks & Motivation", 1242, y + 54, 26, Color.rgb(6, 31, 139), true, Paint.Align.LEFT);
        text(c, "2-Day Completion Streak!", 1242, y + 89, 20, NAVY, false, Paint.Align.LEFT);
        drawStar(c, 1263, y + 135, 17, Color.rgb(255, 190, 0), Color.rgb(234, 148, 0));
        drawStar(c, 1308, y + 135, 17, Color.rgb(255, 190, 0), Color.rgb(234, 148, 0));
        drawStar(c, 1353, y + 135, 17, Color.rgb(196, 207, 226), Color.rgb(107, 130, 167));
        drawProgress(c, 1396, y + 125, 156, .58f, BLUE);
        text(c, "Tap Tuesday tasks to update", 1242, y + 188, 16, Color.rgb(85, 103, 140), false, Paint.Align.LEFT);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float scale = Math.min(getWidth() / DESIGN_W, getHeight() / DESIGN_H);
        float x = (event.getX() - (getWidth() - DESIGN_W * scale) / 2f) / scale;
        float y = (event.getY() - (getHeight() - DESIGN_H * scale) / 2f) / scale;
        float cardW = (1556 - 12 * 4) / 5f;
        float tuesdayX = 22 + cardW + 12;
        if (x >= tuesdayX && x <= tuesdayX + cardW && y >= 238 && y <= 457) {
            int tapped = Math.min(2, Math.max(0, (int) ((y - 238) / 73)));
            if (tapped == tuesdayCompleted && tuesdayCompleted < 3) tuesdayCompleted++;
            else if (tapped < tuesdayCompleted) tuesdayCompleted = tapped;
            prefs.edit().putInt("tuesday-completed", tuesdayCompleted).apply();
            performClick();
            invalidate();
        }
        return true;
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void text(Canvas c, String s, float x, float baseline, float size, int color, boolean bold, Paint.Align align) {
        p.setShader(null); p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(size);
        p.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextAlign(align); c.drawText(s, x, baseline, p);
    }

    private void roundRect(Canvas c, float l, float t, float r, float b, float radius, Paint paint, boolean fill) {
        paint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
        c.drawRoundRect(new RectF(l, t, r, b), radius, radius, paint);
    }

    private void shadowRoundRect(Canvas c, float l, float t, float r, float b, float radius,
                                 int fill, int stroke, float strokeW, float sx, float sy, float blur) {
        p.setShader(null); p.setStyle(Paint.Style.FILL); p.setColor(fill); p.setShadowLayer(blur, sx, sy, Color.argb(45, 25, 61, 125));
        c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p); p.clearShadowLayer();
        if (stroke != Color.TRANSPARENT && strokeW > 0) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(strokeW); p.setColor(stroke);
            c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p); p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawProgress(Canvas c, float x, float y, float w, float progress, int color) {
        p.setColor(Color.rgb(198, 210, 235)); roundRect(c, x, y, x + w, y + 16, 8, p, true);
        if (progress > 0) { p.setColor(color); roundRect(c, x, y, x + w * progress, y + 16, 8, p, true); }
    }

    private void drawCheck(Canvas c, float cx, float cy, float size, int color, boolean unused) {
        p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(size * .18f);
        path.reset(); path.moveTo(cx - size * .48f, cy); path.lineTo(cx - size * .12f, cy + size * .35f); path.lineTo(cx + size * .55f, cy - size * .45f);
        c.drawPath(path, p); p.setStyle(Paint.Style.FILL);
    }

    private void drawLock(Canvas c, float cx, float cy, float size, int color) {
        p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(size * .16f);
        c.drawArc(new RectF(cx - size * .38f, cy - size * .6f, cx + size * .38f, cy + size * .2f), 180, 180, false, p);
        p.setStyle(Paint.Style.FILL); roundRect(c, cx - size * .55f, cy - size * .12f, cx + size * .55f, cy + size * .7f, size * .12f, p, true);
        p.setColor(Color.rgb(66, 91, 133)); c.drawCircle(cx, cy + size * .2f, size * .1f, p); c.drawRect(cx - size * .055f, cy + size * .25f, cx + size * .055f, cy + size * .49f, p);
    }

    private void drawBookIcon(Canvas c, float x, float y, float w, float h) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.rgb(20, 67, 233));
        path.reset(); path.moveTo(x + w/2, y + 9); path.cubicTo(x + 24, y - 2, x + 5, y + 3, x + 5, y + 11); path.lineTo(x + 5, y + h - 2); path.cubicTo(x + 18, y + h - 9, x + 28, y + h - 8, x + w/2, y + h); c.drawPath(path, p);
        path.reset(); path.moveTo(x + w/2, y + 9); path.cubicTo(x + w - 24, y - 2, x + w - 5, y + 3, x + w - 5, y + 11); path.lineTo(x + w - 5, y + h - 2); path.cubicTo(x + w - 18, y + h - 9, x + w - 28, y + h - 8, x + w/2, y + h); c.drawPath(path, p);
        c.drawLine(x+w/2,y+9,x+w/2,y+h,p); p.setStrokeWidth(2.5f);
        for (int i=0;i<3;i++){ c.drawLine(x+14,y+20+i*11,x+28,y+18+i*11,p); c.drawLine(x+w-28,y+18+i*11,x+w-14,y+20+i*11,p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void drawWifi(Canvas c, float cx, float cy) {
        p.setColor(Color.rgb(26, 159, 45)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7);
        c.drawArc(new RectF(cx-27,cy-20,cx+27,cy+30),218,104,false,p); c.drawArc(new RectF(cx-17,cy-8,cx+17,cy+23),218,104,false,p);
        p.setStyle(Paint.Style.FILL); c.drawCircle(cx,cy+17,6,p);
    }

    private void drawStar(Canvas c, float cx, float cy, float radius, int fill, int stroke) {
        path.reset();
        for (int i=0;i<10;i++){ double a=-Math.PI/2+i*Math.PI/5; float rr=i%2==0?radius:radius*.46f; float px=cx+(float)Math.cos(a)*rr, py=cy+(float)Math.sin(a)*rr; if(i==0)path.moveTo(px,py);else path.lineTo(px,py); }
        path.close(); p.setStyle(Paint.Style.FILL); p.setColor(fill); c.drawPath(path,p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(stroke); c.drawPath(path,p); p.setStyle(Paint.Style.FILL);
    }

    private void drawRefresh(Canvas c, float cx, float cy, float r) {
        p.setColor(BLUE); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(8); c.drawArc(new RectF(cx-r,cy-r,cx+r,cy+r),-65,240,false,p);
        p.setStyle(Paint.Style.FILL); path.reset(); path.moveTo(cx+r-2,cy-r+2);path.lineTo(cx+r-5,cy-r+26);path.lineTo(cx+r-25,cy-r+9);path.close();c.drawPath(path,p);
        p.setStyle(Paint.Style.STROKE); c.drawArc(new RectF(cx-r,cy-r,cx+r,cy+r),115,110,false,p); p.setStyle(Paint.Style.FILL);
    }

    private void drawPeople(Canvas c, float x, float y) {
        p.setColor(Color.rgb(32, 56, 137)); c.drawCircle(x+40,y+20,18,p); c.drawCircle(x+78,y+17,21,p); roundRect(c,x+13,y+46,x+66,y+98,27,p,true); roundRect(c,x+52,y+43,x+103,y+91,25,p,true);
    }

    private void drawBell(Canvas c, float cx, float cy, int color) {
        p.setColor(color); path.reset(); path.moveTo(cx-20,cy+19);path.quadTo(cx-12,cy+10,cx-12,cy-5);path.quadTo(cx-12,cy-25,cx,cy-27);path.quadTo(cx+12,cy-25,cx+12,cy-5);path.quadTo(cx+12,cy+10,cx+20,cy+19);path.close();c.drawPath(path,p);c.drawCircle(cx,cy+23,6,p);
    }

    private void drawTrophy(Canvas c, float x, float y) {
        int gold=Color.rgb(255,184,0), orange=Color.rgb(225,126,0); p.setColor(gold); roundRect(c,x+20,y,x+70,y+45,5,p,true); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(orange);c.drawArc(new RectF(x,y+4,x+35,y+39),80,210,false,p);c.drawArc(new RectF(x+55,y+4,x+90,y+39),-110,210,false,p);p.setStyle(Paint.Style.FILL);p.setColor(gold);c.drawRect(x+41,y+42,x+50,y+66,p);roundRect(c,x+25,y+64,x+66,y+75,4,p,true);
    }
}
