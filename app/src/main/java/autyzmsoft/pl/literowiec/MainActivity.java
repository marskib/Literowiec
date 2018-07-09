package autyzmsoft.pl.literowiec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;





//Prowadzenie litery po ekranie Wykonalem na podstawie: https://github.com/delaroy/DragNDrop
//YouTube: https://www.youtube.com/watch?v=H3qr1yK6u3M   szukać:android drag and drop delaroy

public class MainActivity extends Activity {

    Intent intModalDialog;  //Na okienko dialogu 'modalnego' orzy starcie aplikacji
    static MediaPlayer mp = null;

    private ViewGroup rootLayout;

    //Obrazek:
    private ImageView imageView;

    //Placeholders'y na etykiety:
    MojTV L00, L01, L02, L03,
          L04, L05, L06, L07,
          L08, L09, L10, L11;


    public  static MojTV[] lbs;     //tablica zawierajaca (oryginalne) litery wyrazu; onomastyka: lbs = 'labels'
    private static MojTV[] lbsRob;  //tablica robocza, do dzialań (m.in. latwego wykrycia prawidlowego porzadku ulozenia etykiet w Obszarze); podzbior tab. lbs


    TextView tvInfo, tvInfo1, tvInfo2, tvInfo3, tvInfoObszar;

    TextView tvShownWord; //na umieszczenie wyrazu po Zwyciestwie

    private int sizeH, sizeW;    //wymiary Urzadzenia

    private int _xDelta;
    private int _yDelta;

    private int yLg,yLd,xLl,xLp; //wspolrzedne pionowe ygrek Linij Górnej i Dolnej oraz wspolrzedne poziome x linij Lewej i Prawej obszaru 'gorącego'
    private int yLtrim;          ///polozenie  linii 'Trimowania' - srodek Obszaru, do tej linii dosuwam etykiety (kosmetyka znaczaca)

    private RelativeLayout.LayoutParams lParams, layoutParams;

    public  boolean toUp = false; //czy jestesmy w trybie duzych/malych liter
    private Button bUpperLower;   //wielkie/male litery
    private Button bAgain;        //wymieszanie liter


    private LinearLayout lObszar;
    private Button bDalej;                              //button na przechodzenie po kolejne cwiczenie
    private Button bPomin;                              //na pominiecie/ucieczke z cwiczenia nie konczac go

    public static File   dirObrazkiNaSD;                 //katalog z obrazkami na SD (internal i external)
    public static ArrayList<File> myObrazkiSD;           //lista obrazkow w SD    //katalog z obrazkami na SD (internal i external)
    boolean nieGraj = true;
    //przelacznik(semafar) : grac/nie grac - jesli start apk. to ma nie grac slowa (bo glupio..)
    public static String katalog = null;                 //Katalogu w Assets, w ktorym trzymane beda obrazki
    public static String listaObrazkowAssets[] = null;   //lista obrazkow z Assets/obrazki - dla wersji demo (i nie tylko...)

    public int     currImage = -1;      //indeks biezacego obrazka
    public String  currWord  = "*";     //bieżacy, wygenerowany wyraz, wziety z currImage; sluzy do porownan; nie jest wyswietlany (w starych wersjach byl...)
    public String  shownWord = "*";     //currWord widziane/pokazywane na ekranie (różne od currWord, bo mogą być wielkie litery)

    public static int inAreaLicznik = 0;     //licznik liter znajdujacych sie aktualnie w Obszarze


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //na caly ekran:
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        rootLayout = (ViewGroup) findViewById(R.id.view_root);
        imageView = (ImageView) rootLayout.findViewById(R.id.imageView);
        lObszar = (LinearLayout) findViewById(R.id.l_Obszar);
        bDalej  = (Button) findViewById(R.id.bDalej);
        bPomin  = (Button) findViewById(R.id.bPomin);
        bAgain = (Button) findViewById(R.id.bAgain);
        tvShownWord = (TextView) findViewById(R.id.tvShownWord);
        bUpperLower =(Button) findViewById(R.id.bUpperLower);

        //kontrolki do sledzenia:
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvInfo1 = (TextView) findViewById(R.id.tvInfo1);
        tvInfo2 = (TextView) findViewById(R.id.tvInfo2);
        tvInfo3 = (TextView) findViewById(R.id.tvInfo3);
        tvInfoObszar = (TextView) findViewById(R.id.tvoInfoObszar);

        przypiszLabelsyAndListenery();

        //Poprawienie wydajnosci? (zeby w onTouch nie tworzyc stale obiektow) L01 - placeholder
        lParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();
        layoutParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();

        //ustalam polozenie obrazkow - przy pelnej wersji - duuzo więcej... ;):
        katalog = "obrazki_demo_ver";
        if (ZmienneGlobalne.getInstance().PELNA_WERSJA) {
            katalog = "obrazki_pelna_ver";
        }



        dostosujDoUrzadzen();

        dajWspObszaruInfo();

        pokazUkryjEtykietySledzenia(false);

        resetujLabelsy();

        //Trzeba czekac, bo problemy (doswiadczalnie):
        lObszar.post(new Runnable() {
            @Override
            public void run() {
                ustawLadnieEtykiety();
                ustawWymiaryKlawiszy();
            }
        });



        tworzListyObrazkow();
        dajNextObrazek();                   //daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod indeksem currImage
        setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
        rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currImage

        //pokazModal();


    }  //koniec Metody()



    public void setCurrentImage() {

        String nazwaObrazka; //zawiera rozrzerzenie (.jpg , .bmp , ...)

        try {
            if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) { //pobranie z Directory
                nazwaObrazka = "aaaa";//myObrazkiSD[currImage];
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                String robAbsolutePath = dirObrazkiNaSD + "/" + nazwaObrazka;
                Bitmap bm = BitmapFactory.decodeFile(robAbsolutePath, options);
                imageView.setImageBitmap(bm);
            } else {  //pobranie obrazka z Assets
                nazwaObrazka = listaObrazkowAssets[currImage];
                InputStream stream = getAssets().open(katalog + "/" + nazwaObrazka);
                Drawable drawable = Drawable.createFromStream(stream, null);
                imageView.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            Log.e("4321", e.getMessage());
            Toast.makeText(this, "Problem z wyswietleniem obrazka...", Toast.LENGTH_SHORT).show();
        }

        //ODEGRANIE DŹWIĘKU
        odegrajWyraz(400);

    }  //koniecMetody()


    private void odegrajWyraz(int opozniacz) {
        /*************************************************/
        /* Odegranie prezentowanego wyrazu               */
        /*************************************************/
        //najpierw sprawdzam, czy trzeba:
        //Jezeli w ustawieniech jest, zeby nie grac - to wychodzimy:
        if (ZmienneGlobalne.getInstance().BEZ_DZWIEKU == true) {
            return;
        }
        //zeby nie gral zaraz po po starcie apki:
        if (nieGraj) {
            nieGraj = false;
            return;
        }
        //Granie wlasciwe:

        if (!ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG) {
            //odeggranie z Assets (tam TYLKO ogg):
            String nazwaObrazka = listaObrazkowAssets[currImage];
            String rdzenNazwy = usunLastDigitIfAny(getRemovedExtensionName(nazwaObrazka));
            String sciezka_do_pliku_dzwiekowego = "nagrania/" + rdzenNazwy + ".ogg";
            odegrajZAssets(sciezka_do_pliku_dzwiekowego, opozniacz);
        } else {  //pobranie nagrania z directory
            //odegranie z SD (na razie nie zajmujemy sie rozszerzeniem=typ pliku dzwiekowego jest (prawie) dowolny):

            /* ski ski 2018.06.04
            String nazwaObrazka =  .getAktWybrZasob();  //zawiera rozrzerzenie (.jpg , .bmp , ...)
            String rdzenNazwy = Rozdzielacz.getRemovedExtensionName(nazwaObrazka);
            rdzenNazwy = Rozdzielacz.usunLastDigitIfAny(rdzenNazwy); //zakladam, ze plik dźwiękowy nie ma cyfry na koncu: pies1.jpg,pies1.jpg,pies2.jpg --> pies.ogg


            String sciezka_do_pliku_dzwiekowego = dirObrazkiNaSD + "/" + rdzenNazwy; //tutaj przekazujemy rdzen nazwy, bez rozszerzenia, bo mogą być różne (.mp3, ogg, .wav...)
            odegrajZkartySD(sciezka_do_pliku_dzwiekowego, opozniacz);
            */
        }
        return;
    }  //koniec Metody()

    public void odegrajZAssets(final String sciezka_do_pliku_parametr, int delay_milisek) {
        /* ***************************************************************** */
        // Odegranie dzwieku umieszczonego w Assets (w katalogu 'nagrania'):
        /* ***************************************************************** */

        if (ZmienneGlobalne.getInstance().nieGrajJestemW105) return; //na czas developmentu....

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    if (mp != null) {
                        mp.release();
                        mp = new MediaPlayer();
                    } else {
                        mp = new MediaPlayer();
                    }
                    final String sciezka_do_pliku = sciezka_do_pliku_parametr; //udziwniam, bo klasa wewn. i kompilator sie czepia....
                    AssetFileDescriptor descriptor = getAssets().openFd(sciezka_do_pliku);
                    mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();
                    mp.prepare();
                    mp.setVolume(1f, 1f);
                    mp.setLooping(false);
                    mp.start();
                    //Toast.makeText(getApplicationContext(),"Odgrywam: "+sciezka_do_pliku,Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), "Nie można odegrać pliku z dźwiękiem.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, delay_milisek);
    } //koniec Metody()


    public void odegrajZkartySD(final String sciezka_do_pliku_parametr, int delay_milisek) {
        /* ************************************** */
        /* Odegranie pliku dzwiekowego z karty SD */
        /* ************************************** */

        if (ZmienneGlobalne.getInstance().nieGrajJestemW105) return; //na czas developmentu....

        //Na pdst. parametru metody szukam odpowiedniego pliku do odegrania:
        //(typuję, jak moglby sie nazywac plik i sprawdzam, czy istbieje. jezeli istnieje - OK, wychodze ze sprawdzania majac wytypowaną nazwe pliku)
        String pliczek;
        pliczek = sciezka_do_pliku_parametr + ".m4a";
        File file = new File(pliczek);
        if (!file.exists()) {
            pliczek = sciezka_do_pliku_parametr + ".mp3";
            file = new File(pliczek);
            if (!file.exists()) {
                pliczek = sciezka_do_pliku_parametr + ".ogg";
                file = new File(pliczek);
                if (!file.exists()) {
                    pliczek = sciezka_do_pliku_parametr + ".wav";
                    file = new File(pliczek);
                    if (!file.exists()) {
                        pliczek = sciezka_do_pliku_parametr + ".amr";
                        file = new File(pliczek);
                        if (!file.exists()) {
                            pliczek = ""; //to trzeba zrobic, zeby 'gracefully wyjsc z metody (na Android 4.4 sie wali, jesli odgrywa plik nie istniejacy...)
                            //dalej nie sprawdzam/nie typuję... (na razie) (.wma nie sa odtwarzane na Androidzie)
                        }
                    }
                }
            }
        }
        //Odegranie znalezionego (if any) poliku:
        if (pliczek.equals("")) {
            return;  //bo Android 4.2 wali sie, kiedy próbujemy odegrac plik nie istniejący
        }
        Handler handler = new Handler();
        final String finalPliczek = pliczek; //klasa wewnetrzna ponizej - trzeba "kombinowac"...
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    Uri u = Uri.parse(finalPliczek); //parse(file.getAbsolutePath());
                    mp = MediaPlayer.create(getApplicationContext(), u);
                    mp.start();
                } catch (Exception e) {
                    //toast("Nie udalo się odegrać pliku z podanego katalogu...");
                    Log.e("4321", e.getMessage()); //"wytłumiam" komunikat
                } finally {
                    //Trzeba koniecznie zakonczyc Playera, bo inaczej nie slychac dzwieku:
                    //mozna tak:
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    //albo mozna tak:
                    //mPlayer.setOnCompletionListener(getApplicationContext()); ,
                    //a dalej w kodzie klasy zdefiniowac tego listenera, czyli public void onCompletion(MediaPlayer xx) {...}
                }
            }
        }, delay_milisek);
    } //koniec metody odegrajZkartySD


      private void dajNextObrazek() {
     //Daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod indeksem currImage

        currImage = dajLosowyNumerObrazka();

        //Nazwe odpowiadajacego pliku oczyszczamy z nalecialosci:
        String nazwaPliku = listaObrazkowAssets[currImage];
        nazwaPliku = getRemovedExtensionName(nazwaPliku);
        nazwaPliku = usunLastDigitIfAny(nazwaPliku);
        nazwaPliku = usunLastDigitIfAny(nazwaPliku); //jak by byly 2 cyfry...

        currWord  = nazwaPliku;
        shownWord = currWord;

      } //koniec Metody()



   private void rozrzucWyraz() {
   /* Rozrzucenie currWord po tablicy lbs (= po Ekranie) */

       int k;  //na losową pozycję

       //currWord = "ABCDEFGHIJKL";
       //currWord = "cytryna";
       //currWord = "************";
       //currWord   = "abcdefghijkl";
       //currWord   = "wwwwwwwwwwww";
       //currWord   = "pomarańczowy";
       //currWord   = "rękawiczki";
       //currWord   = "jękywiłzkóśp";
       //currWord   = "mmmmmmmmmmmm";
       //currWord   = "mikrofalówka";
       //currWord   = "pies";
       //currWord   = "mmmm";
       //currWord   = "Mikołaj";
       //currWord   = "lalka";
       //currWord   = "jabłko";
       //currWord   = "słoneczniki";
       //currWord   = "podkoszulek";


       //Pobieramy wyraz do rozrzucenia:
       char[] wyraz = currWord.toCharArray();       //bo latwiej operowac na Char'ach

       Random rand = new Random();

       //Kazda litera wyrazu ląduje w losowej komorce tablicy lbs :
       String z;
       for (int i = 0; i < wyraz.length; i++) {

           z = Character.toString(wyraz[i]); //pobranie litery z wyrazu

           //Losowanie pozycji w tablicy lbs:
           do {
               k = rand.nextInt(lbs.length);
           }
           while (lbs[k].getVisibility() == View.VISIBLE); //petla gwarantuje, ze trafiamy tylko w puste (=niewidoczne) etykiety

           //Umieszczenie litery na wylosowanej pozycji (i w strukturze obiektu MojTV) + pokazanie:
           lbs[k].setOrigL(z);
           lbs[k].setText(z);
           lbs[k].setVisibility(View.VISIBLE);

       } //for

       //Ulozylismy z malych (oryginalnych) liter. Jesli trzeba - podnosimy:
       if (toUp) podniesLabels();

   } //koniecMetody();

    private void resetujLabelsy() {
    //Resetowanie tablicy i tym samym zwiazanycyh z nia kontrolek ekranowych:
        for (MojTV lb : lbs) {
            lb.setText("*");
            lb.setInArea(false);
            lb.setVisibility(View.INVISIBLE);
        }
    }


    public void bDalejOnClick(View v) {
        //sledzenie:
        //bAgain.setText("*");
        bUpperLower.setText(sizeW+"x"+sizeH);

        resetujLabelsy();
        ustawLadnieEtykiety();
        dajNextObrazek();                   //daje indeks currImage obrazka do prezentacji oraz currWord = nazwa obrazka bez nalecialosci)
        setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
        rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currWord

        tvShownWord.setVisibility(View.INVISIBLE);

        bDalej.setVisibility(View.INVISIBLE);

        pokazKlawiszeDodatkowe();

    } //koniecMetody()



    public void bAgainOnClick(View v) {

        ustawLadnieEtykiety();
        resetujLabelsy();
        rozrzucWyraz();
        tvShownWord.setVisibility(View.INVISIBLE);

        bDalej.setVisibility(View.INVISIBLE); //gdyby byl widoczny
    }


    public void bPominClick(View v) {
        bDalej.callOnClick();
    }


    public void bUpperLowerOnClick(View v) {
    //Zmiana male/duze litery (w obie strony)

        toUp = !toUp;

        //1.Wyraz juz ulozony:
        if (tvShownWord.getVisibility()== View.VISIBLE) {
            if (toUp) podniesWyraz();
            else restoreOriginalWyraz();
        }
        //2.Wyraz jeszcze nie ulozony:
        else {
            if (toUp) podniesLabels();
            else restoreOriginalLabels();
        }

    } //koniec Metody()

    private void podniesLabels() {
    //Etykiety podnoszone są do Wielkich liter

        String coWidac;
        for (MojTV lb : lbs) {
            if (lb.getVisibility()== View.VISIBLE) {
                coWidac = lb.getText().toString();
                coWidac = coWidac.toUpperCase(Locale.getDefault());
                lb.setText(coWidac);
            }
        }
    } //koniec Metody()

    private void restoreOriginalLabels() {
    //Etykiety przywracane są do oryginalnych (małych) liter
    //Uwzględnia problem MIKOŁAJ->Mikołaj

        String coPokazac;
        for (MojTV lb : lbs) {
            if (lb.getVisibility()==View.VISIBLE) {
                coPokazac = lb.getOrigL();
                lb.setText(coPokazac);
            }
        }
    } //koniec Metody()

    private void podniesWyraz() {
    //Poprawny wyraz z Obszaru podnoszony do Wielkich liter
    //Ewentualne odsuniecie, jezeli wyraz po powiekszeniu wychodzi za prawa krawedz Obszaru

        String coWidac = tvShownWord.getText().toString();
        coWidac = coWidac.toUpperCase(Locale.getDefault());
        tvShownWord.setText(coWidac);

        //Ewentualne odsuniecie:
        //Trzeba czekac, bo problemy (doswiadczalnie):
     /*   tvShownWord.post(new Runnable() {
            @Override
            public void run() {
                int wystaje = (int) (tvShownWord.getX()+tvShownWord.getWidth()+2*tvShownWord.getPaddingStart()  -xLp);
                if (wystaje>-10) {
                    LinearLayout.LayoutParams lPar;
                    //lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
                    //lPar.leftMargin -= Math.abs(30*wystaje);
                    //tvShownWord.setLayoutParams(lPar);
                }
            }
        });*/

        //Jezeli po powiekszeniu liter wyraz wystawalby za Obszar - cofniecie wyrazu w lewo podobnie jak przy malych literach w uporzadkujObszar):
        //(uwaga: korekcja jest w if-ie, wiec nie zawsze bedzie wykonywana)
        LinearLayout.LayoutParams lPar;
        lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
        int leftMost= tvShownWord.getLeft();
        //jak za bardzo na lewo, to korygujemy:
        int n = currWord.length();
        if (n>10) //zmniejszam fontsize lub sciesniam jezeli b.dlugi wyraz, bo wychodzi za Obszar no mater what...
            ewentualniePomniejszLubSciesnij();
        //szacowana szerokosc wyrazu; //uwaga - ta funkcja dziala de facto na malych literach, wiec potem korekcja
        int szer = (int) (n*dajSredniaSzerLitery()*1.25);    //1.25 - doswiadczalny wsp. o jaki duza litera jest wieksza od malej
        if ( (leftMost + szer) > xLp ) {                     //wyraz wyszedłby za prawą krawędz Obszaru
            leftMost = xLp - szer;                           //gdzie wuraz powinien sie rozpoczac, zeby sie zmiescić
            if (leftMost<10)
              leftMost=2;
            lPar.leftMargin = leftMost;
            tvShownWord.setLayoutParams(lPar);
        }
    }  //koniec Metody()


    public static int getSzer(Context context, String text, int textSize, int deviceWidth) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        //return textView.getMeasuredHeight();

        return textView.getMeasuredWidth();
    }

    private void restoreOriginalWyraz() {
    //Wyraz z Obszaru zmniejszany jest do małych liter.
    //Uwzględnia problem MIKOŁAJ->Mikołaj

        String coPokazac = currWord;
        restoreParams(tvShownWord);
        tvShownWord.setText(coPokazac);
    } //koniec Metody()


    public void bDajGestoscOnClick(View v) {
        int screenSize = getResources().getConfiguration().screenLayout &
            Configuration.SCREENLAYOUT_SIZE_MASK;

        int density= getResources().getDisplayMetrics().densityDpi;
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                Toast.makeText(this, "LDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Toast.makeText(this, "MDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Toast.makeText(this, "HDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Toast.makeText(this, "XHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                Toast.makeText(this, "XXHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                Toast.makeText(this, "XXXHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_560:
                Toast.makeText(this, "560 ski ski", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "nie znalazłem...", Toast.LENGTH_SHORT).show();
        }
    } //koniec Metody()


    public void bDajWielkoscEkranuOnClick(View v) {

        bDalej.getLayoutParams().height = yLg;
        bDalej.requestLayout();

        int screenSize = getResources().getConfiguration().screenLayout &
            Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                toastMsg = "XLarge screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither xlarge, large, normal or small";
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
    } //koniec Metody()


    private void dajWspObszaruInfo() {
    //Daje wspolrzedne prostokatnego Obszaru, gdzie ukladany jest wyraz

        lObszar.post(new Runnable() { //czekanie az obszar sie narysuje
            @Override
            public void run() {
                int[] location = new int[2];
                lObszar.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];

                //Przekazanie do zmiennych Klasy parametrow geograficznych Obszaru:
                xLl = x;
                yLg = y;
                xLp = xLl + lObszar.getWidth();
                yLd = yLg + lObszar.getHeight();
                //tvInfoObszar.setText(Integer.toString(xLp)+","+Integer.toString(yLg)); //sledzenie
                //Przekazanie do zmiennek klasy współrzędnej y linii 'Trymowania':
                yLtrim = yLg+ ((int) ((yLd-yLg)/2.0));
            }
        });
   } //koniec Metody()



    private void pokazUkryjEtykietySledzenia(boolean czyPokazac) {
        int rob;
        rob = TextView.INVISIBLE;
        if (czyPokazac) rob = TextView.VISIBLE;
        tvInfo.setVisibility(rob);
        tvInfo1.setVisibility(rob);
        tvInfo2.setVisibility(rob);
        tvInfo3.setVisibility(rob);
        tvInfoObszar.setVisibility(View.VISIBLE);
    } //koniec Metody();


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
                    break;
                case MotionEvent.ACTION_DOWN:
                    lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;

                    //sledzenie:
                    //Pokazanie szerokosci kontrolki:
                    tvInfo.setText(Integer.toString(view.getWidth()));

                    //action_down wykonuje sie (chyba) ZAWSZE, wiec zakladam:
                    ((MojTV) view).setInArea(false);
                    policzInAreasy(); //sledzenie
                    //a potem sie to ww. zmodyfikuje na action up....

                    break;
                case MotionEvent.ACTION_UP:
                    //sledzenie:
                    int Xstop = X;
                    tvInfo.setText("xKontrolki=" + Integer.toString(layoutParams.leftMargin));
                    tvInfo1.setText("xPalca=" + Integer.toString(Xstop));

                    /* Sprawdzenie, czy srodek etykiety jest w Obszarze; Jezeli tak - dosuniecie do lTrim. : */
                    //1.Policzenie wspolrzednych srodka Litery: (zakladam, ze srodek litery jest w srodku kontrolki o szer w i wys. h)
                    int w  = view.getWidth();
                    int lm = layoutParams.leftMargin;
                    int h = view.getHeight();
                    int tm = layoutParams.topMargin;

                    //srodek litery:
                    int xLit = lm + (int) (w/2.0);
                    int yLit = tm + (int) (h/2.0);
                    //2.Dosunirecie Litery na poziomy srodek Obszaru (linia yLtrim); srodek etykiety ma wypasc na yLtrim:
                    if ((yLit>yLg && yLit<yLd) && (xLit>xLl && xLit<xLp)) {
                        layoutParams.topMargin = yLtrim - (int) (h/2.0);  //odejmowanie zeby srodek etykiety wypadl na lTrim

                        //Bylo 'trimowanie' a wiec na pewno jestesmy w Obszarze- dajemy znac i badanie ewentualnego ZWYCIESTWA :
                        ((MojTV) view).setInArea(true);
                        if (policzInAreasy() == currWord.length()) {
                            if (poprawnieUlozono()) {
                                //Toast.makeText(MainActivity.this, "ZWYCIESTWO!!!", Toast.LENGTH_LONG).show();
                                odegrajZAssets("nagrania/komentarze/ding.mp3",10);
                                odegrajZAssets("nagrania/komentarze/oklaski.ogg",400);
                                //uporzadkowanie w Obszarze z lekkim opoznieniem:
                                Handler mHandl = new Handler();
                                mHandl.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        UporzadkujObszar();
                                         } },600);
                            } else {
                                //Toast.makeText(MainActivity.this, "Żle.....", Toast.LENGTH_SHORT).show();
                                odegrajZAssets("nagrania/komentarze/zle.mp3",50);
                            }

                        }

                    }
                    //3.Jesli srodek litery zostala wyciagnieta za bande - dosuwam z powrotem:
                    if (xLit < xLl) {   //dosuniecie w prawo
                        //Toast.makeText(MainActivity.this, "Wyszedl za bande...", Toast.LENGTH_SHORT).show();
                        layoutParams.leftMargin = xLl - view.getPaddingLeft()+2 ; //dosuniecie w prawo
                        rootLayout.invalidate();
                        //Ponowne wywolanie eventa - spowoduje, ze wykona sie onTouch na tym samym view z zastanym (=ACTION_UP) eventem/parametrem, ale na innym polozeniu litery,
                        //litera bedzie w Obszarze i zostanie 'dotrimowana'"
                        view.dispatchTouchEvent(event); // Dispatch touch event to view
                    }
                    if (xLit > xLp) {   //dosuniecie w lewo
                        //Toast.makeText(MainActivity.this, "Wyszedl za bande...", Toast.LENGTH_SHORT).show();
                        layoutParams.leftMargin = xLp - w + view.getPaddingRight(); //dosuniecie w lewo
                        rootLayout.invalidate();
                        view.dispatchTouchEvent(event);
                    }
                    //3.Jezeli srodek litery za górnym lub dolnym brzegiem ekranu - dosuwam z powrotem:
                    if (yLit<0) {
                        //layoutParams.topMargin += Math.abs(layoutParams.topMargin);
                        layoutParams.topMargin = 0;
                    }
                    if (yLit>sizeH) {
                        layoutParams.topMargin = sizeH - (int) (0.7*h);
                    }

                    //sledzenie:
                    tvInfo2.setText("xLit="+Integer.toString(xLit)+" yLit="+Integer.toString(yLit));
                    break;
                /*
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                */
            }
            rootLayout.invalidate();
            return true;
        }
    } //koniec Metody()

    private int dajLeftmostX() {
    //Daje wspolrzedną X najbardziej na lewo polozonej etykiety z Obszaru; pomocnicza

        int min = Integer.MAX_VALUE;
        for (MojTV lb : lbs) {
            if (lb.isInArea())
              if (lb.getLeft() < min) min = (int) lb.getLeft();
        }
        return min;
    }

    private int dajWspYetykiet() {
    //Daje wspolrzedną Y dowolnej (=wszystkich, bo trimowanych) etykiet z Obszaru; pomocnicza
        int wsp = 0;
        for (MojTV lb : lbs) {
            if (lb.isInArea()) {
                wsp = (int) lb.getY();
                break; //bo reszta tak samo (trim) -> "panzerfaust rwie na pierwszej przeszkodzie" ;)
            }
        }
        return  wsp;
    }


    private int dajSredniaSzerLitery() {
    //daje srednia szerokosc etykiety w Obszarze (=szr.lit. w currWord)
        int sum=0;
        for (MojTV lb : lbs) {
            if (lb.isInArea()) {
                sum += lb.getWidth();
                //sum -= lb.getPaddingRight(); //na razie nie wiem, dlaczego odejmowac tylko z jednej strony...
            }
        }
        return (int)  sum / currWord.length();
    }



    private void UporzadkujObszar() {
    /* ******************************************************************************************* */
    /* Po Zwyciestwie:                                                                             */
    /* Gasimy wszysko (litery w obszarze); wyswietlamy zwycieski wyraz, przywracamy klawisz bDalej */
    /* Jesli trzeba - robimy korekcje miejsca wyswietlania (zeby wyraz sie miescil w Obszarze)     */
    /* Gasimy niektore klawisze pod Obszarem.                                                      */
    /* ******************************************************************************************* */

        //Przywracamy wielkosc fontow i letterSpacing, bo mogly byc zmienione przy b. dlugich wyrazach (length>10) o wielkich literach:
        restoreParams(tvShownWord);

        //Wyswietlenie wyrazu rozpoczynajac od miejsca, gdzie user umiescil 1-sza litere (z ewentualnymi poprawkami):
        LinearLayout.LayoutParams lPar;
        lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
        int leftMost = dajLeftmostX();
        //jak za bardzo na lewo, to korygujemy:


        //Rozwiazanie roblemu, jesli wyraz ukladamy zbyt blisko prawej krawedzi Obszaru -
        //Badamy, gdzie skończyłby się wyraz, gdyby nie korygowac niczego, a nastepnie ewentualna korekcja:
        //(TRZEBA PO LITERACH-Etykietach lbs, BO ISTNIEJA NA EKRANIE, A CURRWORD JESZCZE NIE MA w obszarze)
        int n = currWord.length();
        int szer = n*dajSredniaSzerLitery();  //szacowana szerokosc wyrazu
        if ( (leftMost + szer) > xLp ) {      //wyraz wyszedłby za prawą krawędz Obszaru
            leftMost = xLp - szer;            //gdzie wuraz powinien sie rozpoczac, zeby sie zmiescił
            bDalej.setText("Odsunalem o "+szer); //sledzenie
        }
        if (leftMost<10) leftMost=20;

        tvInfoObszar.setText(Integer.toString(xLp)+", leftmost="+Integer.toString(leftMost)); //sledzenie

        lPar.leftMargin = leftMost;
        lPar.topMargin  = dajWspYetykiet()-yLg + dpToPx(3) + 6; //uwzgledniam border width

        tvShownWord.setLayoutParams(lPar);

        if (toUp) ewentualniePomniejszLubSciesnij();  //reakcja na b.dlugi wyraz wielkimi literami (>10)
        pokazWyraz();                                 //w Obszarze pokazany zostaje ulozony wyraz

        //Gasimy wszystkie etykiety:
        for (MojTV lb : lbs) { lb.setVisibility(View.INVISIBLE);}

        ukryjKlawiszeDodatkowe();
        //Przywrocenie/pokazanie klawisza bDalej (z lekkim opoznieniem):
        Handler mHandl = new Handler();
        mHandl.postDelayed(new Runnable() {
            @Override
            public void run() {
                bDalej.setVisibility(View.VISIBLE);
            } },2000); //zeby dziecko mialo czas na 'podziwianie' ;)

    } //koniec Metody()

    private void restoreParams(TextView mTV) {
    //Przywracamy wielkosc fontow i letterSpacing

        int lsize = (int) getResources().getDimension(R.dimen.litera_size);
        mTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, lsize);
        if (Build.VERSION.SDK_INT >= 21) {
            //float lspacing = getResources().getDimension(R.dimen.lspacing_ski); nie dziala, ustawiam na żywca....
            //tvShownWord.setLetterSpacing(lspacing);
            mTV.setLetterSpacing((float) 0.1);
        }
    }

    private void pokazWyraz() {
    //Pokazanie ulozonego wyrazu w Obszarze;
    //Wyraz skladam z tego, co widac na ekranie, nie uzywając currWord (bo duze/male litery)

      StringBuilder sb = new StringBuilder();
      for (MojTV lb : lbsRob) {
        sb.append(lb.getText());
      }
      tvShownWord.setText(sb);
      tvShownWord.setVisibility(View.VISIBLE);
    } //koniec Metody()

    private void ewentualniePomniejszLubSciesnij() {
    //Jesli wyraz dluzszy niz 10, to albo zmniejszam litery, albo sciesniam (jesli szeroki)
    //Sciesniania API dependent, wiec badam.
    //Zakladam, ze wywolywana tylko, gdy duze litery; przy malych- wszystko sie miesci

        if (currWord.length()<11) return;

        int versionINT = Build.VERSION.SDK_INT;

//        String manufacturer = Build.MANUFACTURER;
//        String model = Build.MODEL;
//        String versionREL = Build.VERSION.RELEASE;
//        String versionRelease = Build.VERSION.RELEASE;
//
//        Log.e("MyActivity", "manufacturer " + manufacturer
//                + " \n model " + model
//                + " \n version " + versionINT
//                + " \n version " + versionREL
//                + " \n versionRelease " + versionRelease
//        );

        if (versionINT >= 21) tvShownWord.setLetterSpacing(0);
        else {
            int lsize = (int) getResources().getDimension(R.dimen.litera_size);
            lsize = 1*(lsize/2);
            lsize = pxToSp(lsize);
            tvShownWord.setTextSize(lsize);
        }
    } //koniec Metody()


    private boolean poprawnieUlozono() {
    /* **************************************** */
    /* zalozenie wejsciowe:                     */
    // Wszystkie litery znajduja sie w Obszarze */
    /* Sprawdzenie, czy poprawnie ulozone.      */
    /* **************************************** */

        //najpierw przepisanie do tab. roboczej - bedzie krotsza...; potem manipulacje na roboczej:
        lbsRob = new MojTV[currWord.length()];
        int i = 0;
        for (MojTV lb : lbs) {
            if (lb.isInArea()) {
                lbsRob[i] = lb;
                i++;
            }
        }
        //Tablicę roboczą sortujemy rosnaco babelkowo wg. wspolrzednej X.
        //Wynikiem jest tablica rob. lbsRob, w ktorej kolejne elementy odpowiadają etykietom w Obszarze, ulozonym od lewej do prawej:
        MojTV elRob = new MojTV(this);    //element roboczy
        boolean bylSort = true;
        while (bylSort) {
            bylSort = false;
            for (int j = 0; j < (currWord.length()-1);  j++) {
                if (lbsRob[j].getX() > lbsRob[j +1].getX()) {
                   elRob       = lbsRob[j +1];
                   lbsRob[j+1] = lbsRob[j];
                   lbsRob[j]   = elRob;
                   bylSort = true;
                }
            }
        }

        //Na pdst. tablicy lbsRob skladam wyraz jaki "widac" w Obszarze:
        StringBuilder sb = new StringBuilder();
        for (MojTV el : lbsRob) {
            sb.append(el.getOrigL());
        }
        String wyrazWObszarze = sb.toString();
        return wyrazWObszarze.equals(currWord); //porownywane są ORYGINAŁY, więc aktualna wielkosc liter nie ma znaczenia
    } //koniec Metody();




    private int policzInAreasy() {
    //Zlicza, ile elementow znajduje sie aktualnie w Obszarze
        int licznik = 0;
        for (MojTV lb : lbs) {
            if (lb.isInArea()) licznik++;
        }
        //bUpperLower.setText(Integer.toString(licznik)); //sledzenie
        return licznik;
    }


    @Override protected void onResume() {
        /* *************************************   */
        /* Aplikowanie zmian wprowadzonych w menu  */
        /* Bądż pierwsze uruchomienie (po splashu) */
        /* *************************************   */
        super.onResume();
        //Pokazujemy zupelnie nowe cwiczenie z paramatrami ustawionymi na Zmiennych Glob. (np. poprzez splashScreena Ustawienia):
        final boolean wszystkieRozne = ZmienneGlobalne.getInstance().WSZYSTKIE_ROZNE;
        final boolean roznicujObrazki = ZmienneGlobalne.getInstance().ROZNICUJ_OBRAZKI;
        tworzListyObrazkow(); //konieczne, bo moglo zmienic sie zrodlo obrazkow

//        dajNextObrazek();
//        setCurrentImage();
//        rozrzucWyraz();
    } //koniec Metody()

    private void tworzListyObrazkow() {
        //Tworzenie listy obrazków z Katalogu lub Assets:

        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG == true) {
            dirObrazkiNaSD = new File(ZmienneGlobalne.getInstance().WYBRANY_KATALOG);
            myObrazkiSD = findObrazki(dirObrazkiNaSD);
        }

        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG == false) {
            //Pobranie listy obrazkow z Assets:
            AssetManager mgr = getAssets();
            try {
                listaObrazkowAssets = mgr.list(katalog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } //koniec Metody()


    public static ArrayList<File> findObrazki(File katalog) {
        /* ******************************************************************************************************************* */
        /* Zwraca liste obrazkow (plikow graficznych .jpg .bmp .png) z katalogu katalog - uzywana tylko dla przypadku SD karty */
        /* ******************************************************************************************************************* */
        ArrayList<File> al = new ArrayList<File>(); //al znaczy "Array List"
        File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez nieporządane)
        if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych emulatorach...
            for (File singleFile : files) {
                if ((singleFile.getName().toUpperCase().endsWith(".JPG"))
                        || (singleFile.getName().toUpperCase().endsWith(".PNG"))
                        || (singleFile.getName().toUpperCase().endsWith(".BMP"))
                        || (singleFile.getName().toUpperCase().endsWith(".WEBP"))
                        || (singleFile.getName().toUpperCase().endsWith(".JPEG"))) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }  //koniec Matody()


    private int dajLosowyNumerObrazka() {
        int rob;
        int rozmiar_tab;
        if (ZmienneGlobalne.getInstance().ZRODLEM_JEST_KATALOG)
            rozmiar_tab = myObrazkiSD.size();
        else
            rozmiar_tab = listaObrazkowAssets.length;
        //Generujemy losowy numer, ale tak, zeby nie wypadl ten sam:
        if (rozmiar_tab !=1 ) { //przy tylko jednym obrazku kod ponizej jest petla nieskonczona, więc if
            do {
                rob = (int) (Math.random() * rozmiar_tab);
            } while (rob == currImage);
        }
        else
            rob = 0; //bo 0-to jest de facto numer obrazka

        return rob;
    } //koniec Metody()



    private void przypiszLabelsyAndListenery() {

        L00 = (MojTV) findViewById(R.id.L00);
        L01 = (MojTV) findViewById(R.id.L01);
        L02 = (MojTV) findViewById(R.id.L02);
        L03 = (MojTV) findViewById(R.id.L03);
        L04 = (MojTV) findViewById(R.id.L04);
        L05 = (MojTV) findViewById(R.id.L05);
        L06 = (MojTV) findViewById(R.id.L06);
        L07 = (MojTV) findViewById(R.id.L07);
        L08 = (MojTV) findViewById(R.id.L08);
        L09 = (MojTV) findViewById(R.id.L09);
        L10 = (MojTV) findViewById(R.id.L10);
        L11 = (MojTV) findViewById(R.id.L11);


        //ustawienie tablicy do operowania na ww. etykietach:
        lbs = new MojTV[] {L00, L01, L02, L03, L04, L05, L06, L07, L08, L09, L10, L11};

        //podpiecie listenerow:
        for (MojTV lb : lbs) {
            lb.setOnTouchListener(new ChoiceTouchListener());
        }
    } //koniec Metody()


    private void dostosujDoUrzadzen() {
        RelativeLayout.LayoutParams lPar;

        //Pobieram wymiary ekranu na potrzeby dostosowania wielkosci Obrazka i Prostokata/Obszaru 'gorącego' do ekranu urządzenia:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //przekazanie na zewnatrz:
        sizeW = displaymetrics.widthPixels;
        sizeH = displaymetrics.heightPixels;

        //sledzenie - pokazania wymiarow urządzenia i rozdzielczosci dpi
        tvInfo3.setText(Integer.toString(sizeW) + "x" + Integer.toString(sizeH)+" dpi="+Integer.toString(displaymetrics.densityDpi));

        //Obrazek - ustawiam w lewym górnym rogu:
        lPar = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        lPar.width = sizeW / 3;
        lPar.height = sizeH / 2;
        lPar.topMargin = 5;
        lPar.leftMargin = 10;
        imageView.setLayoutParams(lPar);

        //Obszar-Prostokat na ukladanie wyrazu:
        RelativeLayout.LayoutParams lPar1 = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();
        lPar1.topMargin = (int) (sizeH/1.6);
        //lPar1.leftMargin = 20;
        //lPar1.rightMargin = 20;
        lPar1.height = sizeH/4;
        lObszar.setLayoutParams(lPar1);

    } //koniec Metody()



    public static String getRemovedExtensionName(String name){
        /**
         * Pomocnicza, widoczna wszedzie metodka na pozbycie sie rozszerzenia z nazwy pliku - dostajemy "goly" wyraz
         */
        String baseName;
        if(name.lastIndexOf(".")==-1){
            baseName=name;
        }else{
            int index=name.lastIndexOf(".");
            baseName=name.substring(0,index);
        }
        return baseName;
    }  //koniec metody()

    public static String usunLastDigitIfAny(String name) {
        /**
         * Pomocnicza, widoczna wszedzie, usuwa ewentualna ostatnia cyfre w nazwie zdjecia (bo moze byc pies1.jpg, pies1.hjpg. pies2.jpg - rozne psy)
         * Zakladamy, ze dostajemy nazwe bez rozszerzenia i bez kropki na koncu
         */
        int koniec = name.length()-1;
        if (name.charAt(koniec)=='1'||name.charAt(koniec)=='2'||name.charAt(koniec)=='3'||name.charAt(koniec)=='4'||name.charAt(koniec)=='5'||
                name.charAt(koniec)=='6'||name.charAt(koniec)=='7'||name.charAt(koniec)=='8'||name.charAt(koniec)=='9'||name.charAt(koniec)=='0') {

            return name.substring(0,koniec);
        }
        else {

            return name;
        }
    } //koniec Metody()


    private boolean pokazModal() {
        //Pokazanie modalnego okienka.
        //Okienko realizowane jest jako Activity  o nazwie DialogModalny
        intModalDialog = new Intent(getApplicationContext(), DialogModalny.class);
        startActivity(intModalDialog);
        return true;
    }


    private void ustawLadnieEtykiety() {
        /* *************************************************************************************************** */
        /* Ustawiam Literki/Etykiety L0n wzgledem obrazka i wzgledem siebie - na lewo od obrazka               */
        /* Kazdy rząd (3 rzedy) ustawiam niejako osobno, poczynajac od 1-go elementu w rzedzie jako od wzorca. */
        /* *************************************************************************************************** */



        //wstawka dla duzych ekranow - powiekszam litery:
//        if (sizeW>1100) {
//            int litera_size = (int) getResources().getDimension(R.dimen.litera_size);
//            float wsp = 1.07f;
//            if (sizeW>1900) wsp = 1.2f;
//            for (MojTV lb : lbs) lb.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size);
//            tvShownWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size); //wyraz wyswietlany nie powinien roznic sie od liter
//        }


        final int odstepWpionie = (int) yLg/4; //od gory ekranu do Obszaru sa 3 wiersze etykiet, wiec 4 przerwy

        int od_obrazka = (int) getResources().getDimension(R.dimen.od_obrazka); //odstep 1-szej litery 1-go rzedu od obrazka

        RelativeLayout.LayoutParams lPar;
        //L00 (1-szy rząd):
        lPar = (RelativeLayout.LayoutParams) L00.getLayoutParams();

        //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
        int marginesTop = 1*odstepWpionie - (int) L00.getHeight()/2;  //*1 - bo 1-szy wiersz

        final int poprPion = 25;
        lPar.topMargin = marginesTop - poprPion;
        int poprPoziom = (int) ((rootLayout.getRight()-imageView.getRight())/12);
        //troche w prawo, jesli dobre urzadzenie:

        if (sizeW>1100) poprPoziom = (int) (1.24*poprPoziom);

        lPar.leftMargin = imageView.getRight()-L00.getPaddingLeft() + poprPoziom;

        L00.setLayoutParams(lPar);

        final int poprawka = (int) getResources().getDimension(R.dimen.poprawka);
        Toast.makeText(this,"poprawka: "+pxToDp(poprawka),Toast.LENGTH_SHORT).show();
        int lsize = (int) getResources().getDimension(R.dimen.litera_size);
        Toast.makeText(this,"litera_size: "+pxToSp(lsize),Toast.LENGTH_LONG).show();

        //L01:  //dalej trzeba uzywac Runnable - czekanie az obiekt L00 'usadowi' sie - inaczej wartosci nieustalobe, czyli ok. 0....
        L00.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi sie L01
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L01.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L00.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie  - (int) L00.getHeight()/2 - poprPion;
                lParX.topMargin = marginesTop;
                L01.setLayoutParams(lParX); //n
            }
        });

        //L02:
        L01.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi się L01
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L02.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L01.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie  - (int) L00.getHeight()/2 - poprPion;
                lParX.topMargin = marginesTop;
                L02.setLayoutParams(lParX); //n
            }
        });

        //L03:
        L02.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi się L02
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L03.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L02.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie - (int) L00.getHeight()/2 - poprPion;
                lParX.topMargin = marginesTop;
                L03.setLayoutParams(lParX); //n
            }
        });


        //L04: (2-gi rząd):
        lPar = (RelativeLayout.LayoutParams) L04.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width + ((int) (od_obrazka/4));
        //marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
        marginesTop = 2*odstepWpionie - (int) L00.getHeight()/2; //2- bo 2-gi wiersz
        lPar.topMargin = marginesTop;
        lPar.leftMargin = imageView.getRight() + poprPoziom/4;
        L04.setLayoutParams(lPar);

        //L05
        L04.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L05.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L04.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - (int) L00.getHeight()/2;
                lParX.topMargin = marginesTop;
                L05.setLayoutParams(lParX);
            }
        });

        //L06:
        L05.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L06.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L05.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - (int) L00.getHeight()/2;
                lParX.topMargin = marginesTop;
                L06.setLayoutParams(lParX); //n
            }
        });

        //L07:
        L06.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L07.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L06.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - (int) L00.getHeight()/2;
                lParX.topMargin = marginesTop;
                L07.setLayoutParams(lParX); //n
            }
        });


        //L08: (3-ci rząd):
        lPar = (RelativeLayout.LayoutParams) L08.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width + ((int) (od_obrazka/2));
        //marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
        marginesTop = 3*odstepWpionie - (int) L00.getHeight()/2; //3- bo 3-szy wiersz
        lPar.topMargin = marginesTop + poprPion;
        lPar.leftMargin = imageView.getRight()-L00.getPaddingLeft() + poprPoziom;
        L08.setLayoutParams(lPar);

        //L09:
        L08.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L09.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L08.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - (int) L00.getHeight()/2 + poprPion;
                lParX.topMargin = marginesTop;
                L09.setLayoutParams(lParX); //n
            }
        });

        //L10:
        L09.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L10.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L09.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - (int) L00.getHeight()/2 + poprPion;
                lParX.topMargin = marginesTop;
                L10.setLayoutParams(lParX); //n
            }
        });

        //L11:
        L10.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L11.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L10.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - (int) L00.getHeight()/2 + poprPion;
                lParX.topMargin = marginesTop;
                L11.setLayoutParams(lParX); //n
            }
        });


        //Dodatkowe przemieszanie wyzej-nizej po kazdej etykiecie:
        for (final MojTV lb : lbs) {
            lb.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams lParX =
                            (RelativeLayout.LayoutParams) lb.getLayoutParams();
                    Random rand = new Random();
                    int k = rand.nextInt(3);
                    if (k == 0) k =   0;
                    if (k == 1) k = +15;
                    if (k == 2) k = -15;

                    //Zmieniamy w 2-gim wierszu :
                    if (lb==lbs[4] || lb==lbs[5] || lb==lbs[6] || lb==lbs[7]) {
                        lParX.topMargin += k;
                    }
                    //Zmieniamy w 1-szym wierszu :
                    if (lb==lbs[0] || lb==lbs[1] || lb==lbs[2] || lb==lbs[3]) { //w 1-szym wierszu pozwalam tylko w dol
                        k = -Math.abs(k);
                        lParX.topMargin += k;
                    }
                    //Zmieniamy w 3-cim wierszu :
                    if (lb==lbs[8] || lb==lbs[9] || lb==lbs[10] || lb==lbs[11]) { //w 3-cim wierszu pozwalam tylko w gore
                        k = Math.abs(k);
                        lParX.topMargin -= k;
                    }

                    lb.setLayoutParams(lParX);
                }
            });
        }

    }  //koniec Metody()

    private void ustawWymiaryKlawiszy() {
    //Wymiarowuje klawisze bDalej, bPomin, bAgain
        //bDalej zajmuje przestrzen od gory do gornej krawedzi Obszaru:
        bDalej.getLayoutParams().height = yLg;
        bDalej.requestLayout();

        //cala przestrzen od dolnej krawedzi Obszaru do konca ekranu:
        bPomin.getLayoutParams().height = sizeH - yLd;
        bPomin.requestLayout();

        bUpperLower.getLayoutParams().height = sizeH - yLd;
        bUpperLower.requestLayout();

        bAgain.getLayoutParams().height = sizeH - yLd;
        bAgain.requestLayout();
        bAgain.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) 0.18*pxToSp((int)getResources().getDimension(R.dimen.litera_size)) );

    } //koniec metody()

    private void ukryjKlawiszeDodatkowe() {
    //Po Zwyciestwie ukrywa klawisze pod Obszarem, zeby dziecko nie moglo zrobic balaganu przed pojawieniem sie bDalej.
    //Nie ukrywa bUpperLower, zeby mozna bylo powiekszac/pomniejszac zwycieski currWord.
      bPomin.setVisibility(View.INVISIBLE);
      bAgain.setVisibility(View.INVISIBLE);
    }

    private void pokazKlawiszeDodatkowe() {
    //Pokazanie (ewentualne) klawiszy pod Obszarem"
        if (ZmienneGlobalne.getInstance().BPOMIN_ALL) bPomin.setVisibility(View.VISIBLE);
        if (ZmienneGlobalne.getInstance().BUPLOW_ALL) bUpperLower.setVisibility(View.VISIBLE);
        if (ZmienneGlobalne.getInstance().BAGAIN_ALL) bAgain.setVisibility(View.VISIBLE);
    }

    public int dpToPx(int dp) {
    //Convert dp to pixel:
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public int pxToDp(int px) {
    //Convert pixel to dp:
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToSp(int px) {
        //Convert pixel to sp:
        float scaledDensity = this.getResources().getDisplayMetrics().scaledDensity;
        return Math.round(px /scaledDensity);
    }


}

