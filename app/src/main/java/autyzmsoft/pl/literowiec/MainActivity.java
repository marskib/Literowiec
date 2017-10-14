package autyzmsoft.pl.literowiec;

//Wykonalem na podstawie: https://github.com/delaroy/DragNDrop
//YouTube: https://www.youtube.com/watch?v=H3qr1yK6u3M   szukać:android drag and drop delaroy

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static autyzmsoft.pl.literowiec.R.id.tvInfo;
import static autyzmsoft.pl.literowiec.R.id.tvInfo1;

public class MainActivity extends Activity {

    private ViewGroup rootLayout;
    private ImageView img;

    //Placeholders'y na etykiety:
    TextView L01, L02, L03,
             L04, L05, L06,
             L07, L08, L09,
             L10, L11, L12;

    TextView tvInfo, tvInfo1;

    private int _xDelta;
    private int _yDelta;

    private RelativeLayout.LayoutParams lParams, layoutParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //na caly ekran:
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        rootLayout = (ViewGroup) findViewById(R.id.view_root);
        img = (ImageView) rootLayout.findViewById(R.id.imageView);

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvInfo1 = (TextView) findViewById(R.id.tvInfo1);

        L01 = (TextView) findViewById(R.id.L01);
        L02 = (TextView) findViewById(R.id.L02);
        L03 = (TextView) findViewById(R.id.L03);
        L04 = (TextView) findViewById(R.id.L04);
        L05 = (TextView) findViewById(R.id.L05);
        L06 = (TextView) findViewById(R.id.L06);
        L07 = (TextView) findViewById(R.id.L07);
        L08 = (TextView) findViewById(R.id.L08);
        L09 = (TextView) findViewById(R.id.L09);
        L10 = (TextView) findViewById(R.id.L10);
        L11 = (TextView) findViewById(R.id.L11);
        L12 = (TextView) findViewById(R.id.L12);

        //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
        //img.setLayoutParams(layoutParams);
        //img.setOnTouchListener(new ChoiceTouchListener());

        L01.setOnTouchListener(new ChoiceTouchListener());
        L02.setOnTouchListener(new ChoiceTouchListener());
        L03.setOnTouchListener(new ChoiceTouchListener());
        L04.setOnTouchListener(new ChoiceTouchListener());
        L05.setOnTouchListener(new ChoiceTouchListener());
        L06.setOnTouchListener(new ChoiceTouchListener());
        L07.setOnTouchListener(new ChoiceTouchListener());
        L08.setOnTouchListener(new ChoiceTouchListener());
        L09.setOnTouchListener(new ChoiceTouchListener());
        L10.setOnTouchListener(new ChoiceTouchListener());
        L11.setOnTouchListener(new ChoiceTouchListener());
        L12.setOnTouchListener(new ChoiceTouchListener());

        //Poprawienie wydajnosci? (zeby w onTouch nie tworzyc stale obiektow) L01 - placeholder
        lParams      =  (RelativeLayout.LayoutParams) L01.getLayoutParams();
        layoutParams =  (RelativeLayout.LayoutParams) L01.getLayoutParams();

        dostosujDoUrzadzen();

    }


    private void dostosujDoUrzadzen() {
        int width, height;
        RelativeLayout.LayoutParams lPar;

        //Pobieram wymiary ekranu na potrzeby dostosowania wielkosci Ludzika do aktualnego ekranu:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        width = displaymetrics.widthPixels;
        height= displaymetrics.heightPixels;

        //Obrazek:
        lPar = (RelativeLayout.LayoutParams) img.getLayoutParams();
        lPar.width=width/3;
        lPar.height=height/2;
        lPar.topMargin  = 5;
        lPar.leftMargin = 10;
        img.setLayoutParams(lPar);

        /* Ustawiam Literki/Etykiety L0n wzgledem obrazka i wzgledem siebie - na lewo od obrazka: */

        //L01 (1-szy rząd):
        lPar = (RelativeLayout.LayoutParams) L01.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) img.getLayoutParams()).leftMargin + img.getLayoutParams().width;
        L01.setLayoutParams(lPar);

        //L02:  //dalej trzeba uzywac Runnable - czekanie az obiekt L01 'usadowi' sie - inaczej wartosci nieustalobe, czyli ok. 0....
        L01.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L01.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L02.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L01.getLayoutParams()).leftMargin + 2*width1;
            }
        });

        //L03:
        L02.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L02.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L03.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L02.getLayoutParams()).leftMargin + 2*width1;
            }
        });

        //L04:
        L03.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L03.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L04.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L03.getLayoutParams()).leftMargin + 2*width1;
            }
        });


        //L05: (2-gi rząd):
        lPar = (RelativeLayout.LayoutParams) L05.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) img.getLayoutParams()).leftMargin + img.getLayoutParams().width;
        L05.setLayoutParams(lPar);

        //L06
        L05.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L05.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L06.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L05.getLayoutParams()).leftMargin + (int) (1.5*width1);
            }
        });

        //L07:
        L06.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L06.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L07.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L06.getLayoutParams()).leftMargin + (int)(1.5*width1);
            }
        });

        //L08:
        L07.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L07.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L08.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L07.getLayoutParams()).leftMargin + (int)(1.5*width1);
            }
        });


        //L09: (3-ci rząd):
        lPar = (RelativeLayout.LayoutParams) L09.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) img.getLayoutParams()).leftMargin + img.getLayoutParams().width;
        L09.setLayoutParams(lPar);

        //L10
        L09.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L09.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L10.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L09.getLayoutParams()).leftMargin + 2*width1;
            }
        });

        //L11:
        L10.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L10.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L11.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L10.getLayoutParams()).leftMargin + 2*width1;
            }
        });

        //L11:
        L10.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L10.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L11.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L10.getLayoutParams()).leftMargin + 2*width1;
            }
        });

        //L12:
        L11.post(new Runnable() {
            @Override
            public void run() {
                final int width1;
                width1 = L11.getWidth();
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L12.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L11.getLayoutParams()).leftMargin + 2*width1;
            }
        });

//        //dodatkowe rozsunięcie wzgledem siebie:
//        TextView[] lbs = {L01,L02,L03,L04};
//        for (int i = 1; i < lbs.length; i++) {
//            lPar = (RelativeLayout.LayoutParams) lbs[i].getLayoutParams();
//            lPar.leftMargin = 500;//lPar.leftMargin + 250;//*i;
//            lbs[i].setLayoutParams(lPar);
//        }

    } //koniec Metody()


    private final class ChoiceTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;
                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(layoutParams);
                    //tvInfo.setText("ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_DOWN:
                    lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    tvInfo.setText("ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    int Xstop = X;
                    //tvInfo.setText("x="+Integer.toString(Xstop));
                    tvInfo.setText("xKontrolki="+Integer.toString(layoutParams.leftMargin));
                    tvInfo1.setText("xPalca="+Integer.toString(Xstop));
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
            rootLayout.invalidate();
            return true;
        }
    }



}

