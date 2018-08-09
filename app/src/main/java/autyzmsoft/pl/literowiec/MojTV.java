package autyzmsoft.pl.literowiec;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Klasa do etykiet-liter do ukladania
 */



public class MojTV extends android.support.v7.widget.AppCompatTextView {

    private boolean inArea = false; //czy jest w Obszarze
    private String  origL  = "*";   //Litera z oryginalu; rozwiazuje problem Ola->OLA->Ola
    private int mBlink = 0;         //do mrugania, zeby mozna bylo odwolac sie z klasy wewnetrznej Runnable

    public MojTV(Context context) { super(context); }

    //Potrzebny w xml'u:
    public MojTV(Context context, AttributeSet attrs) {super(context, attrs);}

    public boolean isInArea() {
        return inArea;
    }

    public void setInArea(boolean inArea) {
        this.inArea = inArea;
    }

    public String getOrigL() {return origL; }

    public void setOrigL(String origL) {
        this.origL = origL;
    }


    public void blink(int ile) {
    /* Mruganie etykietą */

        mBlink = 2*ile-1;  //nieparzysta, zeby pozostal na RED
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currColor = MojTV.this.getCurrentTextColor();

                if (currColor== Color.BLACK)
                  currColor = Color.RED;
                else
                  currColor = Color.BLACK;

                MojTV.this.setTextColor(currColor);
                mBlink--;
                if (mBlink>0) {
                    h.postDelayed(this,200);
                }
            }
        }, 500);
    } //koniec metody()



    /**
     * Make a View Blink for a desired duration
     *
     * @param duration for how long in ms will it blink
     * @param offset   start offset of the animation
     * @param ileRazy  ile razy ma mrugnac
     * zrodlo: https://gist.github.com/cesarferreira/4fcae632b18904035d3b
     */
    public void makeMeBlink(int duration, int offset, int ileRazy) {

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(duration);
        anim.setStartOffset(offset);
        anim.setRepeatMode(Animation.REVERSE);
        //anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatCount(ileRazy);
        this.startAnimation(anim);
    }





}  //koniec Klasy




    // proby z blinkiem,,,,,,
// Obiekt i metoda do realizowania kroku bGlowy :
    /*

    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (PAUZA) return;
            krokGlowy();
            krokOgona();
            handler.postDelayed(this, 250);
        }
    };

    */

