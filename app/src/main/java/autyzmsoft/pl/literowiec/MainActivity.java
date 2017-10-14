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

        //L01:
        lPar = (RelativeLayout.LayoutParams) L01.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) img.getLayoutParams()).leftMargin + img.getLayoutParams().width;
        L01.setLayoutParams(lPar);
        //L02:
        lPar = (RelativeLayout.LayoutParams) L02.getLayoutParams();
        int rob = L01.getLayoutParams().width;
        lPar.leftMargin = ((RelativeLayout.LayoutParams) L01.getLayoutParams()).leftMargin + rob;
        L02.setLayoutParams(lPar);
        L02.requestLayout();









    }


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

