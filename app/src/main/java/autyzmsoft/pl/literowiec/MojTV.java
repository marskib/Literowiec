package autyzmsoft.pl.literowiec;



import android.content.Context;
import android.util.AttributeSet;

public class MojTV extends android.support.v7.widget.AppCompatTextView {

    private boolean inArea = false; //czy jest w Obszarze
    private String  origL  = "*";   //Litera z oryginalu; rozwiazuje problem Ola->OLA->Ola

    public MojTV(Context context) { super(context); }

    public MojTV(Context context, AttributeSet attrs) {super(context, attrs);
    } //potrzebny w xml'u

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
