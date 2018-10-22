package autyzmsoft.pl.literowiec;

/**
 * Created by developer on 2018-06-03.
 */

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import java.io.File;

/**
 * singleton na przechowywanie zmiennych globalnych.
 *
 * Szczegoly: patrz film z Educativo odc. 4-3 Application-Glowny-obiekt-aplikacji... Obiekt klasy dzieciczacej po klasie Application pozostaje zywy podczas calej sesji z apką. Obiekt ten tworzony jest
 * przez system, jest tylko JEDEN i nadaje sie do przechowywania zmiennych wspoldzielonych. Mozna nadpisywać jego onCreate() ! Mozna tam nawet powolywac nowe obiekty z klas wewnetrzych(!) W
 * manifest.xml TRZEBA go zadeklarowac w tagu 'applicatin', w atrybucie name jak w przykladzie: <application android:name=".ZmienneGlobalne"
 *
 * Odwolanie we wszystkich klasach apki w np. onCreate() poprzez (przyklad z mojego MainActivity) : ZmienneGlobalne mGlob;   //'m-member' na zmienne globalne - obiekt singleton klasy ZmienneGlobalne
 * mGlob = (ZmienneGlobalne) getApplication(); (zwroc uwage na rzutowanie!!!)
 *
 * W onCreate() tego obiektu najlepiej odwolywac sie do SharedPreferences... :)
 *
 * Obiekt ten ( getApplication() ) ma wszystkie zalety Singletona, ale jest NIEZNISZCZALNY!
 */

public class ZmienneGlobalne extends Application {


  public final boolean PELNA_WERSJA = true;        //czy Pelna czy Darmowa wersja aplikacji
  public final boolean nieGrajJestemW105 = true;  //robocza na czas developmentu

  //Poziomy trudnosci:
  public static final int LATWE = 1;
  public static final int SREDNIE = 2;
  public static final int TRUDNE = 3;
  public static final int WSZYSTKIE = 0;
  public final int MAX_OBR_LIMIT = 2;               //maksymalna liczba obrazkow w katalogu, gdy wersja DARMOWA
  public final boolean DLA_KRZYSKA = false;        //Czy dla Krzyska do testowania - jesli tak -> wylaczam logo i strone www
  public boolean ZRODLEM_JEST_KATALOG; //Co jest aktualnie źródlem obrazków - Asstes czy Katalog (np. katalogAssets na karcie SD)
  public String WYBRANY_KATALOG;      //katalogAssets (if any) wybrany przez usera jako zrodlo obrazkow (z external SD lub Urządzenia)
  public boolean ROZNICUJ_OBRAZKI;     //Czy za każdym razem pokazywany inny obrazek
  public boolean BEZ_OBRAZKOW;         //nie pokazywac obrazkow
  public boolean BEZ_DZWIEKU;          //nie odgrywać słów
  public int POZIOM;                   //poziom trudnosci: 0-wszystkie wyrazy; 1 - wyrazy o max. 4 literach; 2 - wyrazy od 5 do 7 liter; 3 - od 8 do 12 liter
  //Formy nagradzania:
  public boolean GLOS_OKLASKI;        //pochwala glosem a nastepnie oklaski
  public boolean TYLKO_GLOS;          //patrz wyżej
  public boolean TYLKO_OKLASKI;       //patrz wyżej
  public boolean BEZ_KOMENT;          //Bez Komentarza-Nagrody po wybraniu klawisza
  public boolean DEZAP;               //dezaprobata 'nie-e.m4a.' jesli zle ulozono wyraz

  public boolean Z_NAZWA;             //czy ma byc nazwa pod obrazkiem
  public boolean ZE_SPACING;          //czy w ulozonym wyrazie robic duze odstepy miedzy literami (API dependent)
  public boolean ODMOWA_DOST;         //na etapie instalacji/1-go uruchomienia user odmowil dostepu do kart(y); dotyczy androida 6 i więcej


  public boolean BPOMIN_ALL;          //czy bPomin dozwolony (allowed)
  public boolean BAGAIN_ALL;          //czy bAgain dozwolony (allowed)
  public boolean BUPLOW_ALL;          //czy bUpperLower dozwolony (allowed)
  public boolean BHINT_ALL;           //czy bHint dozwolony (allowed) -> klawisz [ ? ]

  //Efekciarstwo (patrz odpowiedni layout w ustawienia.xml); onomasrtyla EF=EFFECT :
  public boolean IMG_TURN_EF;         //czy obrazek obraca sie po poprawnym ulozeniu
  public boolean WORD_SHAKE_EF;       //czy potrzasnac wyrazem (lub jego fragmentem) gdy niepoprawna kolejnosc
  public boolean LETTER_HOPP_EF;      //czy poprawnie polozona litera 'podskakuje' z radosci
  public boolean SND_ERROR_EF;        //czy dzwiek 'brr' gdy zle polozona litera )rowniez ostatnia)
  public boolean SND_LETTER_OK_EF;    //czy dzwiek PLUSK, gdy litera polozona poprawnie (nie dotyczy ostatniej)
  public boolean SND_VICTORY_EF;      //czy dzwiek 'ding' gdy poprawnie ulozono wyraz (przy ostatniej poprawnej literze)

  public boolean POKAZ_MODAL;        //czy pokazywac okienko modalne przy starcie (ergonomia developmentu, w produkcyjnej na true)

  public boolean PO_DIALOGU_MOD = false;  //na mechanizm zapewniajacy odegranie slowa po zamknieciu DialoguModalnego (patrz DialogModalny.onPause i MainAct.onResume)


  @Override
  public void onCreate() {
    super.onCreate();
    ustawParametryDefault();
    //Pobranie zapisanych ustawien i zaladowanie do -> ZmiennychGlobalnych, (if any) gdy startujemy aplikacje :
    pobierzSharedPreferences();
  }

  //ustawienia poczatkowe aplikacji:
  public void ustawParametryDefault() {

    ROZNICUJ_OBRAZKI = true;

    BEZ_OBRAZKOW = false;
    BEZ_DZWIEKU = false;
    Z_NAZWA = true;

    POZIOM = LATWE;//WSZYSTKIE;

    //Komentarze-Nagrody:
    GLOS_OKLASKI = true;
    BEZ_KOMENT = false;
    TYLKO_OKLASKI = false;
    TYLKO_GLOS = false;
    DEZAP = true;

    //Klawisze dodatkowe:
    BPOMIN_ALL = true;             //Onomastyka -> ALL = allowed
    BAGAIN_ALL = true;
    BUPLOW_ALL = true;
    BHINT_ALL = true;

    //Efekciarstwo:
    IMG_TURN_EF = true;            //Onomastyka -> EF = EFFECT
    WORD_SHAKE_EF = true;
    LETTER_HOPP_EF = true;
    SND_ERROR_EF = true;            //Onomastyka -> SND = sound
    SND_LETTER_OK_EF = true;
    SND_VICTORY_EF = true;

    ODMOWA_DOST = false;              //w wersji Androida <= 5 dostep jest automatyczny, wiec muszę to ustawic bo logika aplikacji by przeszkadzala...

    POKAZ_MODAL = true;

    ZRODLEM_JEST_KATALOG = false;        //startujemy ze zrodlem w Assets
    WYBRANY_KATALOG = "*^5%dummy";       //"nic jeszcze nie wybrano" - lepiej to niz null, bo z null'em problemy...

    ZE_SPACING = (Build.VERSION.SDK_INT >= 21);   //bo API dependent

  } //koniec Metody()


  private void pobierzSharedPreferences() {
    /* ******************************************************** */
    /* Zapisane ustawienia wczytywane sa do ZmiennychGlobalnych */
    /* Gdy nie ma klucza (np. first run) - wartosci defaultowe, */
    /* takie jak ustawione przez niniejszą metode.              */
    /* ******************************************************** */

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //na zapisanie ustawien na next. sesję

    //Ponizej zapewniamy, ze apka obudzi sie zawsze z obrazkiem i dzwiekiem (inaczej user bylby zdezorientowany):
    BEZ_OBRAZKOW = false;
    BEZ_DZWIEKU = false;

    GLOS_OKLASKI = sharedPreferences.getBoolean("GLOS_OKLASKI", this.GLOS_OKLASKI);
    BEZ_KOMENT = sharedPreferences.getBoolean("BEZ_KOMENT", this.BEZ_KOMENT);
    TYLKO_OKLASKI = sharedPreferences.getBoolean("TYLKO_OKLASKI", this.TYLKO_OKLASKI);
    TYLKO_GLOS = sharedPreferences.getBoolean("TYLKO_GLOS", this.TYLKO_GLOS);
    DEZAP = sharedPreferences.getBoolean("DEZAP", this.DEZAP);

    Z_NAZWA = sharedPreferences.getBoolean("Z_NAZWA", this.Z_NAZWA);
    ZE_SPACING = sharedPreferences.getBoolean("ZE_SPACING", this.ZE_SPACING);
    ODMOWA_DOST = sharedPreferences.getBoolean("ODMOWA_DOST", this.ODMOWA_DOST);

    POZIOM = sharedPreferences.getInt("POZIOM", this.POZIOM);

    BHINT_ALL = sharedPreferences.getBoolean("BHINT_ALL", this.BHINT_ALL);
    BPOMIN_ALL = sharedPreferences.getBoolean("BPOMIN_ALL", this.BPOMIN_ALL);
    BUPLOW_ALL = sharedPreferences.getBoolean("BUPLOW_ALL", this.BUPLOW_ALL);
    BAGAIN_ALL = sharedPreferences.getBoolean("BAGAIN_ALL", this.BAGAIN_ALL);

    IMG_TURN_EF = sharedPreferences.getBoolean("IMG_TURN_EF", this.IMG_TURN_EF);
    WORD_SHAKE_EF = sharedPreferences.getBoolean("WORD_SHAKE_EF", this.WORD_SHAKE_EF);
    LETTER_HOPP_EF = sharedPreferences.getBoolean("LETTER_HOPP_EF", this.LETTER_HOPP_EF);
    SND_ERROR_EF = sharedPreferences.getBoolean("SND_ERROR_EF", this.SND_ERROR_EF);
    SND_LETTER_OK_EF = sharedPreferences.getBoolean("SND_OK_EF", this.SND_LETTER_OK_EF);
    SND_VICTORY_EF = sharedPreferences.getBoolean("SND_VICTORY_EF", this.SND_VICTORY_EF);

    ROZNICUJ_OBRAZKI = sharedPreferences.getBoolean("ROZNICUJ_OBRAZKI", this.ROZNICUJ_OBRAZKI);

    ZRODLEM_JEST_KATALOG = sharedPreferences.getBoolean("ZRODLEM_JEST_KATALOG", this.ZRODLEM_JEST_KATALOG);

    //Jesli zrodlem miałby byc katalogAssets, to potrzebne dotatkowe sprawdzenie,bo gdyby pomiedzy uruchomieniami
    //zlikwidowano wybrany katalogAssets to mamy problem, i wtedy przelaczamy sie na zrodlo z zasobow aplikacji:
    //Sprawdzam też, czy w wersji Demo user nie dorzucił >5 obrazków do ostatniego katalogu.
    if (ZRODLEM_JEST_KATALOG) {
      String katalog = sharedPreferences.getString("WYBRANY_KATALOG", "*^5%dummy");
      File file = new File(katalog);
      if (!file.exists()) {
        ZRODLEM_JEST_KATALOG = false;
      }
      //gdyby nie zlikwidowano katalogu, ale tylko 'wycieto' obrazki (lub dorzucono > 2) - przelaczenie na Zasoby applikacji:
      else {
        int lObr = MainActivity.findObrazki(new File(katalog)).length;   //liczba obrazkow
        if ((lObr == 0) || (!PELNA_WERSJA && (lObr > MAX_OBR_LIMIT))) {
          ZRODLEM_JEST_KATALOG = false;
        } else {
          WYBRANY_KATALOG = katalog;
        }
      }
    }
  } //koniec Metody()


}


