package autyzmsoft.pl.literowiec;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.File;

/**
 * Wyswietla okienko modalne.
 * Zrodlo - stackoverflow
 * Dotakowo jest jeszcze potrzebny wpis w manifest.xml:
 * action android:name="autyzmsoft.pl.literowiec.DialogModalny"/>   --> it gives the activity the dialog look...
 * Uzywane do startowania aplikacji
 */

public class DialogModalny extends Activity {
    
    ZmienneGlobalne mGlobalne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mGlobalne = (ZmienneGlobalne) getApplication();

        //setFinishOnTouchOutside (false);  //to make it behave like a modal dialog

        setContentView(R.layout.activity_dialog_modalny);

        //Ustawienie szerokosci okna DialogModalny:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int szer = displaymetrics.widthPixels;
        View layoutSki = findViewById(R.id.sv_DialogModalny);
        layoutSki.getLayoutParams().width = (int) (szer*0.85f);
        layoutSki.requestLayout(); //teraz nastepuje zaaplikowanie zmian

        //Pobranie zapisanych ustawien i zaladowanie do -> ZmiennychGlobalnych, (if any) gdy startujemy aplikacje :
        if (savedInstanceState == null) { //ten warunek oznacza, ze to nie obrot, tylko startujemy odpoczatku
            pobierzSharedPreferences();
        }

        czyscDlaKrzyska(); //jezeli wysylam do Testerow, to zacieram namiary na moje www

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

    private void pobierzSharedPreferences() {
    /* ******************************************************** */
    /* Zapisane ustawienia wczytywane sa do ZmiennychGlobalnych */
    /* ******************************************************** */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //na zapisanie ustawien na next. sesję

        mGlobalne.ROZNICUJ_OBRAZKI = sharedPreferences.getBoolean("ROZNICUJ_OBRAZKI",true);

        //Ponizej zapewniamy, ze apka obudzi sie zawsze z obrazkiem i dzwiekiem (inaczej user bylby zdezorientowany):
        mGlobalne.BEZ_OBRAZKOW = false;
        mGlobalne.BEZ_DZWIEKU  = false;

        mGlobalne.BEZ_KOMENT    = sharedPreferences.getBoolean("BEZ_KOMENT",false);
        mGlobalne.TYLKO_OKLASKI = sharedPreferences.getBoolean("TYLKO_OKLASKI", false);
        mGlobalne.TYLKO_GLOS    = sharedPreferences.getBoolean("TYLKO_GLOS", false);
        mGlobalne.CISZA         = sharedPreferences.getBoolean("CISZA", false);

        mGlobalne.Z_NAZWA       = sharedPreferences.getBoolean("Z_NAZWA", true);
        mGlobalne.DELAYED       = sharedPreferences.getBoolean("DELAYED", true);
        mGlobalne.ODMOWA_DOST   = sharedPreferences.getBoolean("ODMOWA_DOST", false);

        //mGlobalne.ZRODLEM_JEST_KATALOG = sharedPreferences.getBoolean("ZRODLEM_JEST_KATALOG", false);

        //Jesli zrodlem miałby byc katalog, to potrzebne dotatkowe sprawdzenie,bo gdyby pomiedzy uruchomieniami
        // zlikwidowano wybrany katalog to mamy problem, i wtedy przelaczamy sie na zrodlo z zasobow aplikacji:
        //Sprawdzam też, czy w wersji Demo user nie dorzucił >5 obrazków do ostatniego katalogu.
        if (mGlobalne.ZRODLEM_JEST_KATALOG) {
            String katalog = sharedPreferences.getString("WYBRANY_KATALOG", "*^5%dummy");
            File file = new File(katalog);
            if (!file.exists()) {
                mGlobalne.ZRODLEM_JEST_KATALOG = false;
            }
            //gdyby nie zlikwidowano katalogu, ale tylko 'wycieto' obrazki (lub dorzucono > 5) - przelaczenie na Zasoby applikacji:
            else {

                /* ski ski 2018.06.03 na razie wylaczam, zeby sie skompilowalo

                int lObr = SplashKlasa.policzObrazki(katalog);//liczba obrazkow
                if ((lObr == 0) || (!mGlob.PELNA_WERSJA && lObr > 5)) {
                    mGlob.ZRODLEM_JEST_KATALOG = false;

                }
                else {
                    mGlob.WYBRANY_KATALOG = katalog;
                }
                do powyzej: ski ski 2018.06.03 na razie wylaczam, zeby sie skompilowalo */
            }
        }
    } //koniec Metody()

    public void czyscDlaKrzyska() {
    /* Ukrywanie obrazkow i 'śladów' do strony www - przed przekazanie do Krzyska; Potem usunac */
        if (mGlobalne.DLA_KRZYSKA) {
            //ImageView obrazek = (ImageView) findViewById(R.id.imageView1);
            //if (obrazek != null) obrazek.setVisibility(View.INVISIBLE);
            //ski ski ski 2018.06.03 TextView link = (TextView) findViewById(R.id.autyzmsoftpl); //bo na niektorych konfiguracjach nie pokazuje tego linku
            //j.w. if (link != null) link.setVisibility(View.INVISIBLE);
        }
    } //koniec Metody

}
