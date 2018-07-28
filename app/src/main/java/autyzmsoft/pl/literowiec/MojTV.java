package autyzmsoft.pl.literowiec;

/**
 * Klasa do etykiet-liter do ukladania
 */

import android.content.Context;
import android.util.AttributeSet;

public class MojTV extends android.support.v7.widget.AppCompatTextView {

    private boolean inArea = false; //czy jest w Obszarze
    private String  origL  = "*";   //Litera z oryginalu; rozwiazuje problem Ola->OLA->Ola

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
}
