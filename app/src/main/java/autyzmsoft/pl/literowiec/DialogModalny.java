package autyzmsoft.pl.literowiec;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Wyswietla okienko modalne.
 * Zrodlo - stackoverflow
 * Dotakowo jest jeszcze potrzebny wpis w manifest.xml:
 * action android:name="autyzmsoft.pl.literowiec.DialogModalny"/>   --> it gives the activity the dialog look...
 * Uzywane do startowania aplikacji
 */

public class DialogModalny extends Activity {
    
    ZmienneGlobalne mGlob;
    private TextView tvSettInfo;  //tym bedziemy 'mrugac', zeby unaocznic userowi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mGlob = (ZmienneGlobalne) getApplication();

        //setFinishOnTouchOutside (false);  //to make it behave like a modal dialog

        setContentView(R.layout.activity_dialog_modalny);

        //Ustawienie szerokosci okna DialogModalny:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int szer = displaymetrics.widthPixels;
        int wys  = displaymetrics.heightPixels;
        View layoutSki = findViewById(R.id.sv_DialogModalny);
        layoutSki.getLayoutParams().width  = (int) (szer*0.95f); //(szer*0.85f);

        //zeby zmiescil sie w pionie na b. malych ekranach
        if (wys<400)
          layoutSki.getLayoutParams().height = (int) (wys*0.99f);

        layoutSki.requestLayout(); //teraz nastepuje zaaplikowanie zmian

        czyscDlaKrzyska(); //jezeli wysylam do Testerow, to zacieram namiary na moje www

        //Tytul na gorze (nie trzeba ustawiac w manifescie(!)):
        String tytul = "LITEROWIEC - ułóż wyraz z liter. ";
        if (mGlob.PELNA_WERSJA) tytul = tytul + "Wersja pełna.";
        else tytul = tytul + "Wersja darmowa.";
        this.setTitle(tytul);

        //Mrugamy textem objasniajacym sposob wejscia do Ustawien (zeby uwidocznic userowi):
        tvSettInfo =(TextView) findViewById(R.id.tvSettInfo);
        tvSettInfo.postDelayed(new Runnable() {
          @Override
          public void run() {
            makeMeBlink(tvSettInfo,500,10,10, Color.GREEN);
          }
        },1000);

    }  //koniec Metody()


    @Override
    public void onBackPressed() { //to make it behave like a modal dialog
        // prevent "back" from leaving this activity
        //zwroc uwage, ze tutaj nie ma super() - nadpisanie/zlikwidowanie metody macierzystej i potomnej
     }

     public void onClickbtnOK(View v) {
         //zamkniecie activity, zeby przejsc do MainActivity (wywolywacza)
         finish();
     }

    @Override
    protected void onPause() {
    /* ****************************************************** */
    /* MainActivity bedzie wiedziala, ze trzeba odegrac wyraz */
    /* ****************************************************** */
        super.onPause();
        mGlob.PO_DIALOGU_MOD = true;
        //Toast.makeText(this, "onPause - DialogModalny", Toast.LENGTH_SHORT).show();
    }



    public void czyscDlaKrzyska() {
    /* Ukrywanie obrazkow i 'śladów' do strony www - przed przekazanie do Krzyska; Potem usunac */
        if (mGlob.DLA_KRZYSKA) {
            ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
            if (obrazek != null) obrazek.setVisibility(View.INVISIBLE);
            TextView link = (TextView) findViewById(R.id.autyzmsoftpl); //bo na niektorych konfiguracjach nie pokazuje tego linku
            if (link != null) link.setVisibility(View.INVISIBLE);
        }
    } //koniec Metody



  /**
   * Make a View Blink for a desired duration
   *
   * @param obiekt   textView we blink
   * @param duration for how long in ms will it blink
   * @param offset   start offset of the animation
   * @param ileRazy  ile razy ma mrugnac
   * @param kolor    jakim kolorem ma mrugac
   * zrodlo: https://gist.github.com/cesarferreira/4fcae632b18904035d3b
   */
  private static void makeMeBlink(TextView obiekt, int duration, int offset, int ileRazy, int kolor) {

    final int savedColor = obiekt.getCurrentTextColor();

    obiekt.setTextColor(kolor);
    Animation anim = new AlphaAnimation(0.0f, 1.0f);
    anim.setDuration(duration);
    anim.setStartOffset(offset);
    anim.setRepeatMode(Animation.REVERSE);
    //anim.setRepeatCount(Animation.INFINITE);
    anim.setRepeatCount(ileRazy);
    obiekt.startAnimation(anim);

    //Przywrocenie pierwotnego koloru klawiszowi po skonczonej animacji:
    final TextView finalTV = obiekt;
    Handler h = new Handler();
    h.postDelayed(new Runnable() {
      @Override
      public void run() {
        finalTV.setTextColor(savedColor);
      }
    },duration*(ileRazy+2)+offset);  //wyr. arytm. - doswiadczalnie....


  } //koniec Metody()


}
