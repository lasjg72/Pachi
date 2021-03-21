public class Main {
    public static void main(String[] args) {
        Spec_Toaru sp = new Spec_Toaru();
        sp.NB_init();
        sp.DB_init();
        sp.round_num();
        sp.Calc_dedama_per_kakuhen();
        System.out.println("ラウンドベース込みの時給: " + Values.Calc_money_per_hour(sp)*4);
    }
}