package autyzmsoft.pl.literowiec;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;


/**
 * Wyswietla okienko modalne.
 * Zrodlo - stackoverflow
 * Dotakowo jest jeszcze potrzebny wpis w manifest.xml:
 * action android:name="autyzmsoft.pl.literowiec.DialogModalny"/>   --> it gives the activity the dialog look...
 * Uzywane do startowania aplikacji
 */

public class DialogModalny extends Activity {
    
    ZmienneGlobalne mGlob;

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
        View layoutSki = findViewById(R.id.sv_DialogModalny);
        layoutSki.getLayoutParams().width = (int) (szer*0.85f);
        layoutSki.requestLayout(); //teraz nastepuje zaaplikowanie zmian

        czyscDlaKrzyska(); //jezeli wysylam do Testerow, to zacieram namiary na moje www

        //Tytul na gorze (nie trzeba ustawiac w manifescie(!)):
        String tytul = "LITEROWIEC - układanie z liter. ";
        if (mGlob.PELNA_WERSJA) tytul = tytul + "Wersja pełna.";
        else tytul = tytul + "Wersja darmowa.";
        this.setTitle(tytul);

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
            //ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
            //if (obrazek != null) obrazek.setVisibility(View.INVISIBLE);
            //ski ski ski 2018.06.03 TextView link = (TextView) findViewById(R.id.autyzmsoftpl); //bo na niektorych konfiguracjach nie pokazuje tego linku
            //j.w. if (link != null) link.setVisibility(View.INVISIBLE);
        }
    } //koniec Metody

}
