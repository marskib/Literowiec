package autyzmsoft.pl.literowiec;

/**
 * Created by developer on 2018-06-03.
 */

import android.app.Application;

/**
 singleton na przechowywanie zmiennych globalnych.

 Szczegoly: patrz film z Educativo odc. 4-3 Application-Glowny-obiekt-aplikacji...
 Obiekt klasy dzieciczacej po klasie Application pozostaje zywy podczas calej sesji z apką.
 Obiekt ten tworzony jest przez system, jest tylko JEDEN i nadaje sie do przechowywania zmiennych wspoldzielonych.
 Mozna nadpisywać jego onCreate() ! Mozna tam nawet powolywac nowe obiekty z klas wewnetrzych(!)
 W manifest.xml TRZEBA go zadeklarowac w tagu 'applicatin', w atrybucie name jak w przykladzie:
 <application android:name=".ZmienneGlobalne"

 Odwolanie we wszystkich klasach apki w np. onCreate() poprzez (przyklad z mojego MainActivity) :
 ZmienneGlobalne mGlob;   //'m-member' na zmienne globalne - obiekt singleton klasy ZmienneGlobalne
 mGlob = (ZmienneGlobalne) getApplication();
 (zwroc uwage na rzutowanie!!!)

 W onCreate() tego obiektu najlepiej odwolywac sie do SharedPreferences... :)

 Obiekt ten ( getApplication() ) ma wszystkie zalety Singletona, ale jest NIEZNISZCZALNY!
 */

public class ZmienneGlobalne extends Application {

    public boolean PELNA_WERSJA;
    public boolean ZRODLEM_JEST_KATALOG; //Co jest aktualnie źródlem obrazków - Asstes czy Katalog (np. katalog na karcie SD)
    public String  WYBRANY_KATALOG;      //katalog (if any) wybrany przez usera jako zrodlo obrazkow (z external SD lub Urządzenia)
    public boolean ZMIENIONO_ZRODLO;     //jakakolwiek zmiana zrodla obrazkow - Assets/Katalog JAK ROWNIEZ zmiana katalogu
    public boolean DLA_KRZYSKA;          //Czy dla Krzyska do testowania - jesli tak -> wylaczam logo i strone www
    public boolean ROZNICUJ_OBRAZKI;     //Za każdym razem pokazywany inny obrazek

    public boolean BEZ_OBRAZKOW;         //nie pokazywac obrazkow
    public boolean BEZ_DZWIEKU;          //nie odgrywać słów

    public boolean BEZ_KOMENT;          //Bez Komentarza-Nagrody po wybraniu klawisza
    public boolean TYLKO_OKLASKI;       //patrz wyżej
    public boolean TYLKO_GLOS;          //patrz wyżej
    public boolean CISZA;               //kompletna Cisza, bez nagrod i bez 'ding,'brrr' po kliknieciu klawisza

    public boolean Z_NAZWA;             //czy ma byc nazwa pod obrazkiem
    public boolean DELAYED;             //czy pokazywać klawisze z wyrazami z opóźnieniem (efekciarstwo ;))
    public boolean ODMOWA_DOST;         //na etapie instalacji/1-go uruchomienia user odmowil dostepu do kart(y); dotyczy androida 6 i więcej


    public boolean BPOMIN_ALL;          //czy bPomin dozwolony (allowed)
    public boolean BAGAIN_ALL;          //czy bAgain dozwolony (allowed)
    public boolean BUPLOW_ALL;          //czy bUpperLower dozwolony (allowed)
    public boolean BHINT_ALL;           //czy bHint dozwolony (allowed) -> klawisz [ ? ]

    public boolean POKAZ_MODAL;        //czy pokazywac okienko modalne przy starcie (ergonomia developmentu)


    public boolean nieGrajJestemW105;  //robocza na czas developmentu

    //Dopisane 2018.08 - zeby przeszla kompilacja:
    public int POZIOM = 1;





    @Override
    public void onCreate() {
        super.onCreate();
        ustawParametryDefault();
    }

    //konstruktor tego singletona + ustawienia poczatkowe aplikacji:
    private void ustawParametryDefault() {

        nieGrajJestemW105 = true; //wyrzucić po skonczonym developmencie

        PELNA_WERSJA = true;
        ROZNICUJ_OBRAZKI = true;

        BEZ_OBRAZKOW = false;
        BEZ_DZWIEKU  = false;
        Z_NAZWA      = true;

        BEZ_KOMENT    = false;
        TYLKO_OKLASKI = false;
        TYLKO_GLOS    = false;
        CISZA         = false;

        BPOMIN_ALL    = true;                //Onomastyka -> ALL = allowed:
        BAGAIN_ALL    = false;
        BUPLOW_ALL    = false;
        BHINT_ALL     = true;

        ODMOWA_DOST  = false;                //w wersji Androida <= 5 dostep jest automatyczny, wiec muszę to ustawic bo logika aplikacji by przeszkadzala...

        POKAZ_MODAL  = false;

        ZRODLEM_JEST_KATALOG = false;        //startujemy ze zrodlem w Assets
        ZMIENIONO_ZRODLO     = false;
        WYBRANY_KATALOG = "*^5%dummy";       //"nic jeszcze nie wybrano" - lepiej to niz null, bo z null'em problemy...

        DLA_KRZYSKA = false;
    } //konstruktor
}


