package org.revo.streamer.livepoll.codec.commons.rtp.d;

public class Rational {
    static final Rational $_1_000 = new Rational(1, 1000);
    static final Rational $_8_000 = new Rational(1, 8000);
    static final Rational $90_000 = new Rational(1, 90000);

    final private int num;
    final private int den;
    
    private Rational(int num, int den) {
        super();
        this.num = num;
        this.den = den;
    }
    

    static Rational valueOf(int den) {
        return switch (den) {
            case 1000 -> $_1_000;
            case 90000 -> $90_000;
            default -> new Rational(1, den);
        };
    }
    
    long convert(long value, Rational unit) {
        return value * unit.num * den / ((long) unit.den * num) ;
    }

    @Override
    public String toString() {
        return num + "/" + den;
    }
}
