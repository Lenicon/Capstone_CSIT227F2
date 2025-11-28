import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//Helpers: Date formatting wrapper for convenience
class SafeDateFormat {
    private final SimpleDateFormat fmt;
    SafeDateFormat(String pattern) { fmt = new SimpleDateFormat(pattern); fmt.setLenient(false); }
    public Date parse(String s) throws ParseException { return fmt.parse(s); }
    public String format(Date d) { return fmt.format(d); }
    // convenience silent parse; returns today if fail
    public Date parseQuiet(String s) {
        try { return parse(s); } catch (Exception e) { return new Date(); }
    }
}