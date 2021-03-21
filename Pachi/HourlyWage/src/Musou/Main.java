package Musou;

public class Main {
    public static void main(String[] args) {
        Spec_Musou sp3 = new Spec_Musou();
        sp3.NB_init();
        sp3.DB_init();
        sp3.Calc_dedama_per_kakuhen();
        System.out.println("ラウンドベース込みの時給: " + Values.Calc_money_per_hour(sp3)*4);
    }
}